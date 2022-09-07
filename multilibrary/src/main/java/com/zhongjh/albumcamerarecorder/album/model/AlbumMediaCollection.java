/*
 * Copyright (C) 2014 nohana, Inc.
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhongjh.albumcamerarecorder.album.model;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.zhongjh.albumcamerarecorder.album.entity.Album;
import com.zhongjh.albumcamerarecorder.album.loader.AlbumMediaLoader;

import java.lang.ref.WeakReference;

/**
 * 多媒体数据源
 *
 * @author zhongjh
 */
public class AlbumMediaCollection implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final int LOADER_MEDIA_ID = 2;
    public static final int LOADER_PREVIEW_ID = 3;
    private static final String ARGS_ALBUM = "args_album";
    private WeakReference<Context> mContext;
    private LoaderManager mLoaderManager;
    private AlbumMediaCallbacks mCallbacks;
    private int mLoaderId = 2;

    @NonNull
    @Override
    @SuppressWarnings("all")
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Context context = mContext.get();
        Album album = null;
        if (args != null) {
            album = args.getParcelable(ARGS_ALBUM);
        }

        // 根据专辑返回图片数据源
        return AlbumMediaLoader.newInstance(context, album);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        Context context = mContext.get();
        if (context == null) {
            return;
        }

        mCallbacks.onAlbumMediaLoad(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        Context context = mContext.get();
        if (context == null) {
            return;
        }

        mCallbacks.onAlbumMediaReset();
    }

    public void onCreate(@NonNull FragmentActivity context, @NonNull AlbumMediaCallbacks callbacks) {
        mContext = new WeakReference<>(context);
        mLoaderManager = LoaderManager.getInstance(context);
        mCallbacks = callbacks;
    }

    public void onDestroy() {
        if (mLoaderManager != null) {
            mLoaderManager.destroyLoader(mLoaderId);
        }
        mCallbacks = null;
    }

    /**
     * 加载图片
     *
     * @param target   专辑
     * @param loaderId 因为两个Fragment共存的原因，所以要区分id
     */
    public void load(@Nullable Album target, int loaderId) {
        mLoaderId = loaderId;
        Bundle args = new Bundle();
        args.putParcelable(ARGS_ALBUM, target);
        mLoaderManager.initLoader(loaderId, args, this);
    }

    /**
     * 获取当前数据源
     */
    public void getLoader() {
        mLoaderManager.getLoader(mLoaderId);
    }

    /**
     * 重新加载图片
     *
     * @param target 专辑
     */
    public void restartLoader(@Nullable Album target) {
        Bundle args = new Bundle();
        args.putParcelable(ARGS_ALBUM, target);
        mLoaderManager.restartLoader(mLoaderId, args, this);
    }

    public interface AlbumMediaCallbacks {

        /**
         * 加载数据完毕
         *
         * @param cursor 光标数据
         */
        void onAlbumMediaLoad(Cursor cursor);

        /**
         * 当一个已创建的加载器被重置从而使其数据无效时，此方法被调用
         */
        void onAlbumMediaReset();
    }
}
