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
package com.zhongjh.albumcamerarecorder.album.engine;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

/**
 * 图片不同加载方式
 * Image loader interface. There are predefined {@link com.zhongjh.albumcamerarecorder.album.engine.impl.GlideEngine}
 * and {@link com.zhongjh.albumcamerarecorder.album.engine.impl.PicassoEngine}.
 * @author zhongjh
 */
public interface ImageEngine {

    /**
     * 加载静态图像资源的缩略图
     * 大部分场景用于相册
     *
     * @param context     上下文
     * @param resize      原始图像的所需大小
     * @param placeholder 尚未加载图像时可绘制的占位符
     * @param imageView   ImageView控件
     * @param uri         加载图像的URI
     */
    void loadThumbnail(Context context, int resize, Drawable placeholder, ImageView imageView, Uri uri);

    /**
     * 加载GIF图像资源的缩略图。如果只是一个缩略图，你不必加载动画gif
     * 场景仅用于相册
     *
     * @param context     上下文
     * @param resize      原始图像的所需大小
     * @param placeholder 尚未加载图像时可绘制的占位符
     * @param imageView   ImageView控件
     * @param uri         加载图像的URI
     */
    void loadGifThumbnail(Context context, int resize, Drawable placeholder, ImageView imageView, Uri uri);

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
    void loadImage(Context context, int resizeX, int resizeY, ImageView imageView, Uri uri);

    /**
     * 加载静态图像资源
     * 场景仅用于预览界面的网络图片
     *
     * @param context   上下文
     * @param imageView ImageView控件
     * @param url       加载图像的url
     */
    void loadUrlImage(Context context, ImageView imageView, String url);

    /**
     * 加载静态图像资源
     * 场景仅用于预览界面的图片
     *
     * @param context   上下文
     * @param imageView ImageView控件
     * @param uri       加载图像的uri
     */
    void loadUriImage(Context context, ImageView imageView, Uri uri);

    /**
     * 加载静态图像资源
     * 场景仅用于预览界面的资源id图片
     *
     * @param context    上下文
     * @param imageView  ImageView控件
     * @param resourceId 资源id图片
     */
    void loadDrawableImage(Context context, ImageView imageView ,Integer resourceId);

    /**
     * 加载GIF图像资源。
     *
     * @param context   Context 上下文
     * @param resizeX   原始图像所需的X尺寸
     * @param resizeY   原始图像的所需Y尺寸
     * @param imageView ImageView控件
     * @param uri       加载图像的uri
     */
    void loadGifImage(Context context, int resizeX, int resizeY, ImageView imageView, Uri uri);

    /**
     * 此实现是否支持动态GIF
     * 只需了解它，方便用户使用
     *
     * @return true支持动画gif，false不支持动画gif。
     */
    boolean supportAnimatedGif();
}
