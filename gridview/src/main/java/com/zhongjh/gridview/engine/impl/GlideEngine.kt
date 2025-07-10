package com.zhongjh.gridview.engine.impl

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.zhongjh.gridview.engine.ImageEngine

/**
 * [ImageEngine] implementation using Glide.
 * @author zhongjh
 */
class GlideEngine : ImageEngine {
    override fun loadPath(context: Context, resize: Int, placeholder: Drawable, imageView: ImageView, path: String) {
        Glide.with(context)
            .load(path)
            .placeholder(placeholder)
            .override(resize, resize)
            .centerCrop()
            .into(imageView)
    }

    override fun loadUrl(context: Context, resize: Int, placeholder: Drawable, imageView: ImageView, url: String) {
        Glide.with(context)
            .load(url)
            .placeholder(placeholder)
            .override(resize, resize)
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
        Glide.with(context)
            .load(resourceId)
            .placeholder(placeholder)
            .override(resize, resize)
            .centerCrop()
            .into(imageView)
    }

    override fun loadUri(context: Context, resizeX: Int, resizeY: Int, imageView: ImageView, uri: Uri) {
        Glide.with(context)
            .load(uri)
            .override(resizeX, resizeY)
            .priority(Priority.HIGH)
            .fitCenter()
            .into(imageView)
    }
}