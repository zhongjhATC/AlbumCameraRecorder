/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhongjh.cameraapp.configuration;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.zhongjh.albumcamerarecorder.album.engine.ImageEngine;
import com.zhongjh.cameraapp.R;

import org.jetbrains.annotations.NotNull;


/**
 * {@link ImageEngine} implementation using Glide.
 * @author zhongjh
 */
public class Glide4Engine implements ImageEngine {

    @Override
    public void loadThumbnail(@NotNull Context context, int resize, @NotNull Drawable placeholder, @NotNull ImageView imageView, @NotNull Uri uri) {
        Glide.with(context.getApplicationContext())
                .asBitmap() // some .jpeg files are actually gif
                .load(uri)
                .apply(new RequestOptions()
                        .override(resize, resize)
                        .placeholder(placeholder)
                        .centerCrop())
                .into(imageView);
    }

    @Override
    public void loadGifThumbnail(@NotNull Context context, int resize, @NotNull Drawable placeholder, @NotNull ImageView imageView,
                                 @NotNull Uri uri) {
        Glide.with(context)
                .asBitmap() // some .jpeg files are actually gif
                .load(uri)
                .apply(new RequestOptions()
                        .override(resize, resize)
                        .placeholder(placeholder)
                        .centerCrop())
                .into(imageView);
    }

    @Override
    public void loadImage(@NotNull Context context, int resizeX, int resizeY, @NotNull ImageView imageView, @NotNull Uri uri) {
        Glide.with(context)
                .load(uri)
                .apply(new RequestOptions()
                        .override(resizeX, resizeY)
                        .priority(Priority.HIGH)
                        .error(R.drawable.ic_failed)
                        .fitCenter())
                .into(imageView);
    }

    @Override
    public void loadUrlImage(@NotNull Context context, @NotNull ImageView imageView, @NotNull String url) {
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions()
                        .priority(Priority.HIGH)
                        .error(R.drawable.ic_failed)
                        .fitCenter())
                .into(imageView);
    }

    @Override
    public void loadUriImage(@NotNull Context context, @NotNull ImageView imageView, @NotNull Uri uri) {
        Glide.with(context)
                .load(uri)
                .apply(new RequestOptions()
                        .priority(Priority.HIGH)
                        .error(R.drawable.ic_failed)
                        .fitCenter())
                .into(imageView);
    }

    @Override
    public void loadDrawableImage(@NotNull Context context, @NotNull ImageView imageView, int resourceId) {
        Glide.with(context)
                .load(resourceId)
                .apply(new RequestOptions()
                        .priority(Priority.HIGH)
                        .error(R.drawable.ic_failed)
                        .fitCenter())
                .into(imageView);
    }

    @Override
    public void loadGifImage(@NotNull Context context, int resizeX, int resizeY, @NotNull ImageView imageView, @NotNull Uri uri) {
        Glide.with(context)
                .asGif()
                .load(uri)
                .apply(new RequestOptions()
                        .override(resizeX, resizeY)
                        .priority(Priority.HIGH)
                        .fitCenter())
                .into(imageView);
    }

    @Override
    public boolean supportAnimatedGif() {
        return true;
    }

}
