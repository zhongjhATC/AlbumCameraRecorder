package com.zhongjh.common.engine

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView

/**
 * 图片不同加载方式
 * Image loader interface. There are predefined [com.zhongjh.common.engine.impl.GlideEngine]
 * and [com.zhongjh.common.engine.impl.PicassoEngine].
 * @author zhongjh
 */
interface ImageEngine {
    /**
     * 加载静态图像资源的缩略图
     * 大部分场景用于相册
     *
     * @param context     上下文
     * @param resize      原始图像的所需大小
     * @param placeholder 尚未加载图像时可绘制的占位符
     * @param imageView   ImageView控件
     * @param path        加载图像的path,API 29版本以上的是uri
     */
    fun loadThumbnail(context: Context, resize: Int, placeholder: Drawable, imageView: ImageView, path: String)

    /**
     * 加载静态图像资源
     * 场景仅用于预览图片，用于高清大图
     *
     * @param context   上下文
     * @param resizeX   原始图像所需的X尺寸
     * @param resizeY   原始图像的所需Y尺寸
     * @param imageView ImageView控件
     * @param uri       加载图像的URI
     */
    fun loadImage(context: Context, resizeX: Int, resizeY: Int, imageView: ImageView, uri: Uri)

    /**
     * 加载静态图像资源
     * 场景仅用于预览界面的网络图片
     *
     * @param context   上下文
     * @param resizeX   原始图像所需的X尺寸
     * @param resizeY   原始图像的所需Y尺寸
     * @param imageView ImageView控件
     * @param url       加载图像的url
     */
    fun loadUrlImage(context: Context, resizeX: Int, resizeY: Int, imageView: ImageView, url: String)

    /**
     * 加载图像资源
     * 场景仅用于预览界面的图片
     *
     * @param context   上下文
     * @param imageView ImageView控件
     * @param path       加载图像的path
     */
    fun loadUriImage(context: Context, imageView: ImageView, path: String)

    /**
     * 加载静态图像资源
     * 场景仅用于预览界面的资源id图片
     *
     * @param context    上下文
     * @param imageView  ImageView控件
     * @param resourceId 资源id图片
     */
    fun loadDrawableImage(context: Context, imageView: ImageView, resourceId: Int)

    /**
     * 加载GIF图像资源。
     *
     * @param context   Context 上下文
     * @param resizeX   原始图像所需的X尺寸
     * @param resizeY   原始图像的所需Y尺寸
     * @param imageView ImageView控件
     * @param uri       加载图像的uri
     */
    fun loadGifImage(
        context: Context, resizeX: Int, resizeY: Int, imageView: ImageView, uri: Uri
    )

    /**
     * 此实现是否支持动态GIF
     * 只需了解它，方便用户使用
     *
     * @return true支持动画gif，false不支持动画gif。
     */
    fun supportAnimatedGif(): Boolean

    /**
     * When the recyclerview slides quickly, the callback can be used to pause the loading of resources
     *
     * @param context
     */
    fun pauseRequests(context: Context)

    /**
     * When the recyclerview is slow or stops sliding, the callback can do some operations to restore resource loading
     *
     * @param context
     */
    fun resumeRequests(context: Context)
}