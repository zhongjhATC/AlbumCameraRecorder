package com.zhongjh.albumcamerarecorder.model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zhongjh.albumcamerarecorder.album.entity.Album2
import com.zhongjh.albumcamerarecorder.album.entity.MediaData
import com.zhongjh.albumcamerarecorder.album.loader.MediaLoader
import com.zhongjh.common.entity.LocalMedia
import kotlinx.coroutines.launch

/**
 * Main的ViewModel，缓存相关数据给它的子Fragment共同使用
 * https://juejin.cn/post/7031125139227951112
 *
 * @author zhongjh
 * @date 2022/9/7
 */
class MainModel(application: Application) : AndroidViewModel(application) {

    private val tag: String = this@MainModel.javaClass.simpleName

    /**
     * 数据库操作文件类
     */
    private var mediaLoader: MediaLoader

    /**
     * 输送失败信息
     */
    private val _onFail = MutableLiveData<Throwable>()
    val onFail: LiveData<Throwable> get() = _onFail

    /**
     *  文件夹数据集
     */
    private val _albums = MutableLiveData<MutableList<Album2>>()
    val albums: LiveData<MutableList<Album2>> get() = _albums

    /**
     * 多媒体文件数据集,只存在于一页的,主要用于通知ui增加新的一页数据
     */
    private val _localMediaPages = MutableLiveData<MutableList<LocalMedia>>()
    val localMediaPages: LiveData<MutableList<LocalMedia>> get() = _localMediaPages

    /**
     * 多媒体文件数据集的缓存，相册和预览都会用到
     */
    val localMedias = ArrayList<LocalMedia>()

    /**
     * 分页相册的当前页码
     */
    var page = 1

    /**
     * 当前所选择的文件夹
     */
    var currentSelection = 0

    /**
     * 当前预览的图片的索引,默认第一个
     */
    var previewPosition = 0

    init {
        mediaLoader = MediaLoader(application)
    }

    /**
     * 重新获取数据
     *
     * @param bucketId 专辑id
     * @param pageSize 每页多少个
     */
    fun reloadPageMediaData(bucketId: Long, pageSize: Int) {
        viewModelScope.launch {
            page = 1
            loadPageMediaData(bucketId, page, pageSize)
        }
    }

    /**
     * 下拉加载数据
     *
     * @param bucketId 专辑id
     * @param pageSize 每页多少个
     */
    fun addAllPageMediaData(bucketId: Long, pageSize: Int) {
        viewModelScope.launch {
            page += 1
            loadPageMediaData(bucketId, page, pageSize)
        }
    }

    /**
     * 获取所有专辑
     */
    fun loadAllAlbum() {
        viewModelScope.launch {
            try {
                this@MainModel._albums.postValue(mediaLoader.loadMediaAlbum())
            } catch (ex: Exception) {
                this@MainModel._onFail.postValue(ex)
            }
        }
    }

    /**
     * 根据页码获取数据
     *
     * @param bucketId 专辑id
     * @param page     当前页码
     * @param pageSize 每页多少个
     */
    private suspend fun loadPageMediaData(bucketId: Long, page: Int, pageSize: Int) {
        Log.d(tag, "bucketId : $bucketId")
        viewModelScope.launch {
            try {
                val localMediaMutableList = mediaLoader.loadMediaMore(bucketId, page, pageSize)
                // 通知UI有新的数据
                this@MainModel._localMediaPages.postValue(localMediaMutableList)
                // 添加进缓存数据
                localMedias.addAll(localMediaMutableList)
            } catch (ex: Exception) {
                this@MainModel._onFail.postValue(ex)
            }
        }
    }

    /**
     * 由于屏幕旋转导致的Activity重建，该方法不会被调用
     *
     *
     * 只有ViewModel已经没有任何Activity与之有关联，系统则会调用该方法，你可以在此清理资源
     */
    override fun onCleared() {
        super.onCleared()
    }

}