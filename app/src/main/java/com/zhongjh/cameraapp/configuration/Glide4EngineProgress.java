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

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.zhongjh.progresslibrary.engine.ImageEngine;


/**
 * 这是配合展示数据九宫格view使用的 com.zhongjh.progresslibrary.widget.MaskProgressLayout
 * {@link ImageEngine} implementation using Glide.
 * @author zhongjh
 */

public class Glide4EngineProgress implements ImageEngine {

    @Override
    public void loadThumbnail(@NonNull Context context, int resize, @NonNull Drawable placeholder, @NonNull ImageView imageView, @NonNull Uri uri) {
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
    public void loadUrlThumbnail(@NonNull Context context, int resize, @NonNull Drawable placeholder, @NonNull ImageView imageView, @NonNull String url) {
        Glide.with(context)
                .asBitmap() // some .jpeg files are actually gif
                .load(url)
                .apply(new RequestOptions()
                        .override(resize, resize)
                        .placeholder(placeholder)
                        .centerCrop())
                .into(imageView);
    }

    @Override
    public void loadGifThumbnail(@NonNull Context context, int resize, Drawable placeholder, @NonNull ImageView imageView,
                                 @NonNull Uri uri) {
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
    public void loadImage(@NonNull Context context, int resizeX, int resizeY, @NonNull ImageView imageView, @NonNull Uri uri) {
        Glide.with(context)
                .load(uri)
                .apply(new RequestOptions()
                        .override(resizeX, resizeY)
                        .priority(Priority.HIGH)
                        .fitCenter())
                .into(imageView);
    }

    @Override
    public void loadGifImage(@NonNull Context context, int resizeX, int resizeY, @NonNull ImageView imageView, @NonNull Uri uri) {
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
