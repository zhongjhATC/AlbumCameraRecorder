package com.zhongjh.cameraviewsoundrecorder.album.model;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import java.lang.ref.WeakReference;

/**
 * Created by zhongjh on 2018/8/30.
 */
public class AlbumCollection implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 1;
    private static final String STATE_CURRENT_SELECTION = "state_current_selection";
    private WeakReference<Context> mContext;    // 通过外部传入 Context，采用弱引用的方式防止内存泄露
    private LoaderManager mLoaderManager;   // 加载器的管理器
    private AlbumCallbacks mCallbacks;      // 回调
    private int mCurrentSelection;

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    public void onCreate(Fragment fragment, AlbumCallbacks callbacks) {
        mContext = new WeakReference<>(fragment.getContext());
        mLoaderManager = fragment.getLoaderManager();
        mCallbacks = callbacks;
    }

    /**
     * 因为其他因素销毁，重新启动
     * 获取当前选择的专辑索引值
     *
     * @param savedInstanceState 缓存容器
     */
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }

        mCurrentSelection = savedInstanceState.getInt(STATE_CURRENT_SELECTION);
    }

    /**
     * 因为其他因素销毁
     * 保存当前选择的专辑索引值
     *
     * @param outState 缓存容器
     */
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_CURRENT_SELECTION, mCurrentSelection);
    }

    /**
     * 获取所有专辑
     */
    public void loadAlbums() {
        mLoaderManager.initLoader(LOADER_ID, null, this);
    }

    public int getCurrentSelection() {
        return mCurrentSelection;
    }

    public void setCurrentSelection(int currentSelection) {
        this.mCurrentSelection = currentSelection;
    }

    public interface AlbumCallbacks {

        /**
         * 加载数据
         *
         * @param cursor
         */
        void onAlbumLoad(Cursor cursor);

        /**
         * 重置相册
         */
        void onAlbumReset();

    }

}
