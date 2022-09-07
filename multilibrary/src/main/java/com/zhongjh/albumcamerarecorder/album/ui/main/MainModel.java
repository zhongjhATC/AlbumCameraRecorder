package com.zhongjh.albumcamerarecorder.album.ui.main;

import static com.zhongjh.albumcamerarecorder.album.model.AlbumMediaCollection.LOADER_MEDIA_ID;

import android.app.Application;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.zhongjh.albumcamerarecorder.album.model.AlbumCollection;
import com.zhongjh.albumcamerarecorder.album.model.AlbumMediaCollection;

/**
 * Main的ViewModel，缓存相关数据给它的子Fragment共同使用
 * https://juejin.cn/post/7031125139227951112
 *
 * @author zhongjh
 * @date 2022/9/7
 */
public class MainModel extends AndroidViewModel implements AlbumCollection.AlbumCallbacks {

    /**
     * 专辑下拉数据源
     */
    private final AlbumCollection mAlbumCollection = new AlbumCollection();
    /**
     * 相册下拉数据源
     */
    private final AlbumMediaCollection mAlbumMediaCollection = new AlbumMediaCollection();
    /**
     * 当前选择的专辑索引
     */
    private int currentSelection;

    public MainModel(@NonNull Application application) {
        super(application);
        mAlbumCollection.onCreate(this,this);
        mAlbumCollection.loadAlbums();
    }

    @Override
    public void onAlbumLoadFinished(Cursor cursor) {
        // 加载完专辑数据后,也加载相应的图片数据
        mAlbumMediaCollection.onCreate(this,this);
        mAlbumMediaCollection.load(album, LOADER_MEDIA_ID);
    }

    @Override
    public void onAlbumReset() {

    }

    public int getCurrentSelection() {
        return currentSelection;
    }

    public void setStateCurrentSelection(int currentSelection) {
        this.currentSelection = currentSelection;
    }

}
