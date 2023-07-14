package com.zhongjh.albumcamerarecorder.album.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.zhongjh.albumcamerarecorder.album.entity.Album2;
import com.zhongjh.albumcamerarecorder.album.listener.OnQueryDataListener;
import com.zhongjh.albumcamerarecorder.album.loader.MediaLoader;

/**
 * Main的ViewModel，缓存相关数据给它的子Fragment共同使用
 * https://juejin.cn/post/7031125139227951112
 *
 * @author zhongjh
 * @date 2022/9/7
 */
public class MainModel extends AndroidViewModel {

    MediaLoader mMediaLoader;

    public MainModel(@NonNull Application application) {
        super(application);
        mMediaLoader = new MediaLoader(application);
    }

    /**
     * 获取所有专辑
     */
    public void loadAllAlbum(OnQueryDataListener<Album2> onQueryDataListener) {
        mMediaLoader.loadAllMedia(onQueryDataListener);
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
}
