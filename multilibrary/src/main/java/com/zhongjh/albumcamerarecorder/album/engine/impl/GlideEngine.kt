package com.zhongjh.albumcamerarecorder.album.engine.impl

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.zhongjh.albumcamerarecorder.album.engine.ImageEngine

/**
 * [ImageEngine] implementation using Glide.
 * @author zhongjh
 */
class GlideEngine : ImageEngine {
    override fun loadThumbnail(
        context: Context,
        resize: Int,
        placeholder: Drawable,
        imageView: ImageView,
        uri: Uri
    ) {
        Glide.with(context)
            .load(uri)
            .asBitmap() // some .jpeg files are actually gif
            .placeholder(placeholder)
            .override(resize, resize)
            .centerCrop()
            .into(imageView)
    }

    override fun loadGifThumbnail(
        context: Context, resize: Int, placeholder: Drawable, imageView: ImageView,
        uri: Uri
    ) {
        Glide.with(context)
            .load(uri)
            .asBitmap()
            .placeholder(placeholder)
            .override(resize, resize)
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
        Glide.with(context)
            .load(uri)
            .override(resizeX, resizeY)
            .priority(Priority.HIGH)
            .fitCenter()
            .into(imageView)
    }

    override fun loadUrlImage(context: Context, imageView: ImageView, url: String) {
        Glide.with(context)
            .load(url)
            .priority(Priority.HIGH)
            .fitCenter()
            .into(imageView)
    }

    override fun loadUriImage(context: Context, imageView: ImageView, uri: Uri) {
        Glide.with(context)
            .load(uri)
            .priority(Priority.HIGH)
            .fitCenter()
            .into(imageView)
    }

    override fun loadDrawableImage(context: Context, imageView: ImageView, resourceId: Int) {
        Glide.with(context)
            .load(resourceId)
            .priority(Priority.HIGH)
            .fitCenter()
            .into(imageView)
    }

    override fun loadGifImage(
        context: Context,
        resizeX: Int,
        resizeY: Int,
        imageView: ImageView,
        uri: Uri
    ) {
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