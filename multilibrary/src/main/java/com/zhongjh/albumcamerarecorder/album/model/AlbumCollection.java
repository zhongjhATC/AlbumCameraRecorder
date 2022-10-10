package com.zhongjh.albumcamerarecorder.album.model;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.zhongjh.albumcamerarecorder.album.loader.AlbumLoader;

import java.lang.ref.WeakReference;

/**
 * 每个mLoaderManager都要跑onCreateLoader 初始化的方法，不然会是null
 *
 * @author zhongjh
 * @date 2018/8/30
 */
public class AlbumCollection implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 1;
    private static final String STATE_CURRENT_SELECTION = "state_current_selection";
    /**
     * 通过外部传入 Context，采用弱引用的方式防止内存泄露
     */
    private WeakReference<Context> mContext;
    /**
     * 加载器的管理器
     */
    private LoaderManager mLoaderManager;
    /**
     * 回调
     */
    private AlbumCallbacks mCallbacks;
    private int mCurrentSelection;
    public boolean mLoadFinished;

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Context context = mContext.get();
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

    public void onCreate(FragmentActivity activity, AlbumCallbacks callbacks) {
        mContext = new WeakReference<>(activity);
        mLoaderManager = LoaderManager.getInstance(activity);
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
     * 因为其他因素（例如内存不足）销毁
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

    /**
     * 获取所有专辑
     */
    public void restartLoadAlbums() {
        mLoaderManager.restartLoader(LOADER_ID, null, this);
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
         * @param cursor 数据源
         */
        void onAlbumLoadFinished(Cursor cursor);

        /**
         * 重置相册
         */
        void onAlbumReset();

    }

}
