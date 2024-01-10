package com.zhongjh.common.entity

import android.content.Context
import android.widget.ImageView
import com.zhongjh.common.engine.ImageEngine

/**
 * 多媒体基类，主要用于预览界面共用
 * 多媒体的公共方法
 */
interface BaseMedia {

    /**
     * 是否视频
     */
    fun isVideo(): Boolean

    /**
     * 加载图片
     */
    fun loadImage(context: Context, imageEngine: ImageEngine, imageView: ImageView)

}