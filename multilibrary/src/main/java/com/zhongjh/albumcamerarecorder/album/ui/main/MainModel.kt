package com.zhongjh.albumcamerarecorder.album.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zhongjh.albumcamerarecorder.album.entity.Album2
import com.zhongjh.albumcamerarecorder.album.listener.OnLoadPageMediaDataListener
import com.zhongjh.albumcamerarecorder.album.loader.MediaLoader
import com.zhongjh.albumcamerarecorder.album.loader.MediaPageLoader
import com.zhongjh.common.entity.LocalMedia

/**
 * Main的ViewModel，缓存相关数据给它的子Fragment共同使用
 * https://juejin.cn/post/7031125139227951112
 *
 * @author zhongjh
 * @date 2022/9/7
 */
class MainModel(application: Application) : AndroidViewModel(application) {

    /**
     * 数据库操作类
     */
    var mMediaLoader: MediaLoader

    /**
     * 数据库操作类
     */
    var mMediaPageLoader: MediaPageLoader

    /**
     * 文件夹数据列表
     */
    private val albums: MutableLiveData<List<Album2>> by lazy {
        MutableLiveData<List<Album2>>().also {
            loadAllAlbum()
        }
    }

    /**
     * 多媒体文件数据
     */
    private val localMedias: MutableLiveData<List<LocalMedia>>? = null

    /**
     * 当前所选择的文件夹
     */
    var currentSelection = 0
        private set

    fun getAlbums(): LiveData<List<Album2>> {
        return albums
    }

    init {
        mMediaLoader = MediaLoader(application)
        mMediaPageLoader = MediaPageLoader(application)
    }

    fun setStateCurrentSelection(currentSelection: Int) {
        this.currentSelection = currentSelection
    }

    /**
     * 获取所有专辑
     */
    private fun loadAllAlbum() {
        mMediaLoader.loadAllMedia { data: List<Album2> -> this.albums.postValue(data) }
    }

    /**
     * 获取所有数据
     *
     * @param bucketId 专辑id
     * @param page     当前页码
     * @param pageSize 每页多少个
     */
    fun loadPageMediaData(bucketId: Long, page: Int, pageSize: Int) {
        mMediaPageLoader.loadPageMediaData(
            bucketId, page, pageSize, pageSize
        ) { data, currentPage, isHasMore ->
            localMedias.postValue(data)
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

    /**
     * 获取相同数据的索引
     *
     * @param items 数据列表
     * @param item  当前数据
     * @return 索引
     */
    fun checkedNumOf(item: LocalMedia): Int {
        var index = -1
        // 一般用于相册数据的获取索引
        for (i in items.indices) {
            if (items[i].path == item.path
                && items[i].id == item.id
            ) {
                index = i
                break
            }
        }
        // 如果选择的为 -1 就是未选状态，否则选择基础数量+1
        return if (index == -1) {
            Int.MIN_VALUE
        } else {
            index + 1
        }
    }

}