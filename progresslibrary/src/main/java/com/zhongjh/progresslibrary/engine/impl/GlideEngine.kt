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
package com.zhongjh.progresslibrary.engine.impl

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.zhongjh.progresslibrary.engine.ImageEngine

/**
 * [ImageEngine] implementation using Glide.
 * @author zhongjh
 */
class GlideEngine : ImageEngine {
    override fun loadThumbnail(context: Context, resize: Int, placeholder: Drawable, imageView: ImageView, uri: Uri) {
        Glide.with(context)
                .load(uri)
                .asBitmap() // some .jpeg files are actually gif
                .placeholder(placeholder)
                .override(resize, resize)
                .centerCrop()
                .into(imageView)
    }

    override fun loadUrlThumbnail(context: Context, resize: Int, placeholder: Drawable, imageView: ImageView, url: String) {
        Glide.with(context)
                .load(url)
                .asBitmap() // some .jpeg files are actually gif
                .placeholder(placeholder)
                .override(resize, resize)
                .centerCrop()
                .into(imageView)
    }

    override fun loadGifThumbnail(context: Context, resize: Int, placeholder: Drawable?, imageView: ImageView,
                                  uri: Uri) {
        Glide.with(context)
                .load(uri)
                .asBitmap()
                .placeholder(placeholder)
                .override(resize, resize)
                .centerCrop()
                .into(imageView)
    }

    override fun loadImage(context: Context, resizeX: Int, resizeY: Int, imageView: ImageView, uri: Uri) {
        Glide.with(context)
                .load(uri)
                .override(resizeX, resizeY)
                .priority(Priority.HIGH)
                .fitCenter()
                .into(imageView)
    }

    override fun loadGifImage(context: Context, resizeX: Int, resizeY: Int, imageView: ImageView, uri: Uri) {
        Glide.with(context)
                .load(uri)
                .asGif()
                .override(resizeX, resizeY)
                .priority(Priority.HIGH)
                .into(imageView)
    }

    override fun supportAnimatedGif(): Boolean {
        return true
    }
}