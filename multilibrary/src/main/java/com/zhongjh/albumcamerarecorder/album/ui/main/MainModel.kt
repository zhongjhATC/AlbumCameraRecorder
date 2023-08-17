package com.zhongjh.albumcamerarecorder.album.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zhongjh.albumcamerarecorder.album.entity.Album2
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

    private val tag: String = this@MainModel.javaClass.simpleName

    /**
     * 数据库操作文件夹类
     */
    private var mediaLoader: MediaLoader

    /**
     * 数据库操作文件类
     */
    var mediaPageLoader: MediaPageLoader

    /**
     * 当前选择的数据操作文件类
     */
    var selectedData: SelectedData

    /**
     * 文件夹数据集
     */
    private val albums: MutableLiveData<List<Album2>> by lazy {
        MutableLiveData<List<Album2>>().also {
            loadAllAlbum()
        }
    }

    /**
     * 多媒体文件数据集
     */
    private var localMedias = MutableLiveData<List<LocalMedia>>()

    /**
     * 当前所选择的文件夹
     */
    var currentSelection = 0
        private set

    fun getAlbums(): LiveData<List<Album2>> {
        return albums
    }

    fun getLocalMedias(): LiveData<List<LocalMedia>> {
        return localMedias
    }

    init {
        mediaLoader = MediaLoader(application)
        mediaPageLoader = MediaPageLoader(application)
        selectedData = SelectedData(application)
    }

    fun setStateCurrentSelection(currentSelection: Int) {
        this.currentSelection = currentSelection
    }

    /**
     * 获取所有专辑
     */
    private fun loadAllAlbum() {
        mediaLoader.loadAllMedia { data: List<Album2> -> this.albums.postValue(data) }
    }

    /**
     * 获取所有数据
     *
     * @param bucketId 专辑id
     * @param page     当前页码
     * @param pageSize 每页多少个
     */
    fun loadPageMediaData(bucketId: Long, page: Int, pageSize: Int) {
        Log.d(tag, "bucketId : $bucketId")
        mediaPageLoader.loadPageMediaData(
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

}