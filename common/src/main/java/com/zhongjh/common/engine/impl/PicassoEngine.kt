package com.zhongjh.common.engine.impl

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.zhongjh.common.engine.ImageEngine

/**
 * [ImageEngine] implementation using Picasso.
 * @author zhongjh
 */
class PicassoEngine : ImageEngine {

    override fun loadThumbnail(
        context: Context,
        resize: Int,
        placeholder: Drawable,
        imageView: ImageView,
        path: String
    ) {
        Picasso.with(context).load(path).placeholder(placeholder)
            .resize(resize, resize)
            .centerCrop()
            .into(imageView)
    }

    override fun loadImage(
        context: Context,
        resizeX: Int,
        resizeY: Int,
        imageView: ImageView,
        uri: Uri
    ) {
        Picasso.with(context).load(uri).resize(resizeX, resizeY).priority(Picasso.Priority.HIGH)
            .centerInside().into(imageView)
    }

    override fun loadUrlImage(
        context: Context,
        resizeX: Int,
        resizeY: Int,
        imageView: ImageView,
        url: String
    ) {
        Picasso.with(context).load(url).resize(resizeX, resizeY).priority(Picasso.Priority.HIGH)
            .centerInside().into(imageView)
    }

    override fun loadUriImage(context: Context, imageView: ImageView, path: String) {
        Picasso.with(context).load(path).priority(Picasso.Priority.HIGH)
            .centerInside().into(imageView)
    }

    override fun loadDrawableImage(context: Context, imageView: ImageView, resourceId: Int) {
        Picasso.with(context).load(resourceId).priority(Picasso.Priority.HIGH)
            .centerInside().into(imageView)
    }

    override fun loadGifImage(
        context: Context,
        resizeX: Int,
        resizeY: Int,
        imageView: ImageView,
        uri: Uri
    ) {
        loadImage(context, resizeX, resizeY, imageView, uri)
    }

    override fun supportAnimatedGif(): Boolean {
        return false
    }

    override fun pauseRequests(context: Context) {
    }

    override fun resumeRequests(context: Context) {
    }
}