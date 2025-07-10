package com.zhongjh.gridview.engine

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView

/**
 * 图片不同加载方式
 * Image loader interface. There are predefined [com.zhongjh.gridview.engine.impl.GlideEngine]
 * and [com.zhongjh.gridview.engine.impl.PicassoEngine].
 * @author zhongjh
 */
interface ImageEngine {
    /**
     * Load thumbnail of a static image resource.
     *
     * @param context     Context
     * @param resize      原产地图像的期望尺寸
     * @param placeholder Placeholder drawable when image is not loaded yet
     * @param imageView   ImageView widget
     * @param path        path of the loaded image
     */
    fun loadPath(context: Context, resize: Int, placeholder: Drawable, imageView: ImageView, path: String)

    /**
     * Load thumbnail of a static image resource.
     *
     * @param context     Context
     * @param resize      原产地图像的期望尺寸
     * @param placeholder Placeholder drawable when image is not loaded yet
     * @param imageView   ImageView widget
     * @param url         url of the loaded image
     */
    fun loadUrl(context: Context, resize: Int, placeholder: Drawable, imageView: ImageView, url: String)

    /**
     * Load a static image resource.
     *
     * @param context   Context
     * @param resizeX   Desired x-size of the origin image
     * @param resizeY   Desired y-size of the origin image
     * @param imageView ImageView widget
     * @param uri       Uri of the loaded image
     */
    fun loadUri(context: Context, resizeX: Int, resizeY: Int, imageView: ImageView, uri: Uri)

    /**
     * Load thumbnail of a static image resource.
     *
     * @param context     Context
     * @param resize      原产地图像的期望尺寸
     * @param placeholder Placeholder drawable when image is not loaded yet
     * @param imageView   ImageView widget
     * @param resourceId  resourceId of the loaded image
     */
    fun loadResourceId(context: Context, resize: Int, placeholder: Drawable, imageView: ImageView, resourceId: Int)
}