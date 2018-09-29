package com.zhongjh.cameraviewsoundrecorder.album.model;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.zhongjh.cameraviewsoundrecorder.album.loader.AlbumLoader;

import java.lang.ref.WeakReference;

/**
 * 每个mLoaderManager都要跑onCreateLoader 初始化的方法，不然会是null
 * Created by zhongjh on 2018/8/30.
 */
public class AlbumCollection implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 1;
    private static final String STATE_CURRENT_SELECTION = "state_current_selection";
    private WeakReference<Context> mContext;    // 通过外部传入 Context，采用弱引用的方式防止内存泄露
    private LoaderManager mLoaderManager;   // 加载器的管理器
    private AlbumCallbacks mCallbacks;      // 回调
    private int mCurrentSelection;
    private boolean mLoadFinished;

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Context context = mContext.get();
        if (context == null) {
            return null;
        }
        mLoadFinished = false;
        return AlbumLoader.newInstance(context);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        Context context = mContext.get();
        if (context == null) {
            return;
        }

        if (!mLoadFinished) {
            mLoadFinished = true;
            mCallbacks.onAlbumLoadFinished(data);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        Context context = mContext.get();
        if (context == null) {
            return;
        }

        mCallbacks.onAlbumReset();
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
     * 进行销毁
     */
    public void onDestroy() {
        if (mLoaderManager != null) {
            mLoaderManager.destroyLoader(LOADER_ID);
        }
        mCallbacks = null;
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

    public void setStateCurrentSelection(int currentSelection) {
        this.mCurrentSelection = currentSelection;
    }

    public interface AlbumCallbacks {

        /**
         * 加载数据完数据后
         *
         * @param cursor
         */
        void onAlbumLoadFinished(Cursor cursor);

        /**
         * 重置相册
         */
        void onAlbumReset();

    }

}
