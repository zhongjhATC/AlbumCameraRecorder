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
        Picasso.get().load(path).placeholder(placeholder)
            .resize(resize, resize)
            .centerCrop()
            .into(imageView)
    }

    override fun loadUrl(context: Context, resize: Int, placeholder: Drawable, imageView: ImageView, url: String) {
        Picasso.get().load(url).placeholder(placeholder)
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
        Picasso.get().load(resourceId).placeholder(placeholder)
            .resize(resize, resize)
            .centerCrop()
            .into(imageView)
    }

    override fun loadUri(context: Context, resizeX: Int, resizeY: Int, imageView: ImageView, uri: Uri) {
        Picasso.get().load(uri).resize(resizeX, resizeY).priority(Picasso.Priority.HIGH)
            .centerInside().into(imageView)
    }
}