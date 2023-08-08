package com.zhongjh.albumcamerarecorder.album.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.zhongjh.albumcamerarecorder.album.entity.Album2;
import com.zhongjh.albumcamerarecorder.album.listener.OnLoadAllAlbumListener;
import com.zhongjh.albumcamerarecorder.album.listener.OnLoadPageMediaDataListener;
import com.zhongjh.albumcamerarecorder.album.loader.MediaLoader;
import com.zhongjh.albumcamerarecorder.album.loader.MediaPageLoader;

import java.util.List;

/**
 * Main的ViewModel，缓存相关数据给它的子Fragment共同使用
 * https://juejin.cn/post/7031125139227951112
 *
 * @author zhongjh
 * @date 2022/9/7
 */
public class MainModel extends AndroidViewModel {

    /**
     * 数据库操作类
     */
    MediaLoader mMediaLoader;
    /**
     * 数据库操作类
     */
    MediaPageLoader mMediaPageLoader;
    /**
     * 文件夹数据列表
     */
    private MutableLiveData<List<Album2>> albums;
    /**
     * 当前所选择的文件夹
     */
    private int mCurrentSelection;

    public LiveData<List<Album2>> getAlbums() {
        if (albums == null) {
            albums = new MutableLiveData<>();
            loadAllAlbum();
        }
        return albums;
    }

    public MainModel(@NonNull Application application) {
        super(application);
        mMediaLoader = new MediaLoader(application);
        mMediaPageLoader = new MediaPageLoader(application);
    }

    public int getCurrentSelection() {
        return mCurrentSelection;
    }

    public void setStateCurrentSelection(int currentSelection) {
        this.mCurrentSelection = currentSelection;
    }

    /**
     * 获取所有专辑
     */
    private void loadAllAlbum() {
        mMediaLoader.loadAllMedia(data -> albums.postValue(data));
    }

    /**
     * 获取所有数据
     *
     * @param bucketId 专辑id
     * @param page     当前页码
     * @param pageSize 每页多少个
     * @param listener 回调事件
     */
    public void loadPageMediaData(long bucketId, int page, int pageSize,
                                  OnLoadPageMediaDataListener listener) {
        mMediaPageLoader.loadPageMediaData(bucketId, page, pageSize, pageSize, listener);
    }

    /**
     * 由于屏幕旋转导致的Activity重建，该方法不会被调用
     * <p>
     * 只有ViewModel已经没有任何Activity与之有关联，系统则会调用该方法，你可以在此清理资源
     */
    @Override
    protected void onCleared() {
        super.onCleared();
    }

    /**
     * 获取相同数据的索引
     *
     * @param items 数据列表
     * @param item  当前数据
     * @return 索引
     */
    @JvmStatic
    fun checkedNumOf(items: List<LocalMedia>, item: LocalMedia): Int {
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
