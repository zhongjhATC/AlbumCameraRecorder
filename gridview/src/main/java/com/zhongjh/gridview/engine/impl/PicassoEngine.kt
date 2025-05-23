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
package com.zhongjh.gridview.engine.impl

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.zhongjh.gridview.engine.ImageEngine

/**
 * [ImageEngine] implementation using Picasso.
 * @author zhongjh
 */
class PicassoEngine : ImageEngine {
    override fun loadPath(context: Context, resize: Int, placeholder: Drawable, imageView: ImageView, path: String) {
        Picasso.with(context).load(path).placeholder(placeholder)
            .resize(resize, resize)
            .centerCrop()
            .into(imageView)
    }

    override fun loadUrl(context: Context, resize: Int, placeholder: Drawable, imageView: ImageView, url: String) {
        Picasso.with(context).load(url).placeholder(placeholder)
            .resize(resize, resize)
            .centerCrop()
            .into(imageView)
    }

    override fun loadResourceId(
        context: Context,
        resize: Int,
        placeholder: Drawable,
        imageView: ImageView,
        resourceId: Int
    ) {
        Picasso.with(context).load(resourceId).placeholder(placeholder)
            .resize(resize, resize)
            .centerCrop()
            .into(imageView)
    }

    override fun loadUri(context: Context, resizeX: Int, resizeY: Int, imageView: ImageView, uri: Uri) {
        Picasso.with(context).load(uri).resize(resizeX, resizeY).priority(Picasso.Priority.HIGH)
            .centerInside().into(imageView)
    }
}