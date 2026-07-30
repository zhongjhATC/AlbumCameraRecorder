package com.zhongjh.multimedia.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DiffUtil
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.multimedia.album.entity.Album
import com.zhongjh.multimedia.album.entity.RefreshMediaData
import com.zhongjh.multimedia.album.loader.MediaLoader
import com.zhongjh.multimedia.album.repository.MediaRepository
import com.zhongjh.multimedia.album.ui.mediaselection.adapter.LocalMediaCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Main的ViewModel，缓存相关数据给它的子Fragment共同使用
 *
 * @author zhongjh
 * @date 2025/11/18
 */
class MainModel(application: Application) : AndroidViewModel(application) {

    /**
     * 数据仓库类，统一管理数据加载
     */
    private val mediaRepository: MediaRepository = MediaRepository(MediaLoader(application))

    /**
     * 专辑列表状态流（不可变数据 + 状态封装）
     * - 私有可变流：_albums（仅内部修改）
     * - 公开只读流：albums（外部仅观察）
     */
    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    /**
     * 分页媒体数据状态流（密封类统一管理多状态）
     * - 整合加载中/成功/空/错误状态
     * - 替代原 LiveData：_refreshMediaData、_addAllPageMediaData、_onFail
     */
    private val _mediaPageState = MutableSharedFlow<MediaPageState>(
        replay = 0,
        extraBufferCapacity = 3,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val mediaPageState: SharedFlow<MediaPageState> = _mediaPageState

    /**
     * 原图状态流（替代原 _originalEnable LiveData）
     */
    private val _originalEnable = MutableStateFlow(false)
    val originalEnable: StateFlow<Boolean> = _originalEnable.asStateFlow()

    /**
     * 预览界面滑动位置流（替代原 _onViewPageSelected LiveData）
     */
    private val _onViewPageSelected = MutableSharedFlow<Int>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val onViewPageSelected: SharedFlow<Int> = _onViewPageSelected.asSharedFlow()

    /**
     * 相册列表滑动完成流（替代原 _onScrollToPositionComplete LiveData）
     */
    private val _onScrollToPositionComplete = MutableStateFlow(0)
    val onScrollToPositionComplete: StateFlow<Int> = _onScrollToPositionComplete.asStateFlow()

    /**
     * 事件1：专辑列表首次加载完成（页面初始化动作）
     */
    private val _albumLoadFinishEvent = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)
    val albumLoadFinishEvent: Flow<Unit> = _albumLoadFinishEvent.asSharedFlow()

    /**
     * 事件2：主动切换专辑（手动选择/初始化切换，触发清空选中、加载媒体）
     */
    private val _albumChangeEvent = MutableSharedFlow<Album>(replay = 0, extraBufferCapacity = 1)
    val albumChangeEvent: Flow<Album> = _albumChangeEvent.asSharedFlow()

    /**
     * 媒体数据缓存
     * */
    private val localMedias = ArrayList<LocalMedia>()

    /**
     * 提供 localMedias 的只读访问接口（返回不可变副本）
     */
    fun getLocalMedias(): List<LocalMedia> = localMedias.toList()

    /** 分页页码（内部状态管理） */
    private var page = 1

    /** 当前选择的专辑索引 */
    var currentSelection = 0

    /** 当前预览图片索引 */
    var previewPosition = -1


    /**
     * 加载所有专辑（通过 Repository 获取数据 + 错误处理）
     */
    fun loadAlbums() {
        viewModelScope.launch {
            // 从 Repository 收集数据流（自动在 IO 线程执行）
            mediaRepository.loadAlbums().collect { repoAlbums ->
                // 更新专辑列表（转为不可变 List，确保数据安全）
                _albums.value = repoAlbums.toList()
                // 发送【一次性初始化事件】
                _albumLoadFinishEvent.emit(Unit)
            }
        }
    }

    /**
     * 对外暴露：切换专辑（入口统一，不暴露内部逻辑）
     * */
    fun changeAlbum(targetAlbum: Album) {
        viewModelScope.launch {
            // 发送【一次性切换事件】
            _albumChangeEvent.emit(targetAlbum)
        }
    }

    /**
     * 重新加载媒体数据（分页第一页 + DiffUtil 差异计算）
     *
     * @param bucketId 专辑id，-1是全部
     * @param pageSize 每页数量
     */
    fun reloadPageMediaData(bucketId: Long, pageSize: Int) {
        viewModelScope.launch {
            try {
                // 从 Repository 获取第一页数据
                val newMedias = mediaRepository.loadMediaPage(bucketId, page = 1, pageSize).first()
                // 计算数据差异（IO 线程执行耗时操作）
                val diffResult = withContext(Dispatchers.IO) {
                    DiffUtil.calculateDiff(LocalMediaCallback(this@MainModel.localMedias, newMedias))
                }
                // 更新缓存
                this@MainModel.localMedias.clear()
                this@MainModel.localMedias.addAll(newMedias)
                // 重置页码
                page = 1

                // 根据数据状态发送对应状态
                if (newMedias.isEmpty()) {
                    _mediaPageState.tryEmit(MediaPageState.Empty("暂无媒体文件"))
                } else {
                    // 发送 DiffResult 用于列表刷新
                    val refreshMediaData = RefreshMediaData()
                    refreshMediaData.data = this@MainModel.localMedias.toList()
                    refreshMediaData.diffResult = diffResult
                    _mediaPageState.tryEmit(MediaPageState.RefreshSuccess(bucketId, refreshMediaData))
                }
            } catch (e: Exception) {
                // 发送错误状态
                _mediaPageState.tryEmit(
                    MediaPageState.Error(
                    userMessage = "加载媒体数据失败",
                    cause = e
                    )
                )
            }
        }
    }

    /**
     * 加载更多媒体数据（分页加载）
     */
    fun addAllPageMediaData(bucketId: Long, pageSize: Int) {
        viewModelScope.launch {
            try {
                // 页码自增
                page += 1
                val newMedias = mediaRepository.loadMediaPage(bucketId, page, pageSize).first()

                if (newMedias.isNotEmpty()) {
                    // 更新缓存（保留原逻辑）
                    val oldSize = localMedias.size
                    localMedias.addAll(newMedias)
                    // 发送加载更多成功状态（用于局部刷新）
                    _mediaPageState.tryEmit(
                        MediaPageState.LoadMoreSuccess(
                        data = localMedias.toList(),
                        startPosition = oldSize,
                        itemCount = newMedias.size
                        )
                    )
                }
            } catch (e: Exception) {
                // 加载失败回滚页码
                page -= 1
                _mediaPageState.tryEmit(
                    MediaPageState.Error(
                    userMessage = "加载更多失败",
                    cause = e
                    )
                )
            }
        }
    }

    /**
     * 更新原图状态
     */
    fun setOriginalEnable(enable: Boolean) {
        _originalEnable.value = enable
    }

    /**
     * 获取原图状态
     */
    fun getOriginalEnable(): Boolean = _originalEnable.value

    /**
     * 预览界面滑动事件
     */
    fun onViewPageSelected(position: Int) {
        _onViewPageSelected.tryEmit(position)
    }

    /**
     * 列表滑动完成事件
     */
    fun onScrollToPositionComplete(position: Int) {
        _onScrollToPositionComplete.value = position
    }

    /**
     * 分页状态密封类（统一管理加载/成功/空/错误状态）
     * - 数据均为不可变类型（List 而非 MutableList）
     * - 包含错误信息（替代原 onFail LiveData）
     */
    sealed class MediaPageState {

        /**
         * 刷新事件 也用于初次加载
         * @param bucketId 专辑id,-1是全部
         * @param refreshMediaData 刷新数据
         */
        data class RefreshSuccess(val bucketId: Long, val refreshMediaData: RefreshMediaData) : MediaPageState()

        /**
         * 加载更多局部刷新事件（复用原 addAllPageMediaData 逻辑）
         * @param startPosition 刷新起始位置
         * @param itemCount 新增项数量
         */
        data class LoadMoreSuccess(val data: List<LocalMedia>, val startPosition: Int, val itemCount: Int) : MediaPageState()

        /**
         * 空数据（自定义提示消息）
         * @param message 提示消息
         */
        data class Empty(val message: String = "暂无媒体文件") : MediaPageState()

        /**
         * 错误状态（用户可见消息+异常详情）
         * @param userMessage 用户可见消息
         * @param cause 异常详情（用于调试）
         */
        data class Error(val userMessage: String = "加载失败", val cause: Throwable) : MediaPageState()

    }

}