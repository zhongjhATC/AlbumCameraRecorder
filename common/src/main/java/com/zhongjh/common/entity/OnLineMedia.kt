package com.zhongjh.common.entity

import android.content.Context
import android.widget.ImageView
import com.zhongjh.common.engine.ImageEngine

class OnLineMedia : BaseMedia {

    var id: Long = 0

    /**
     * 在线网址
     */
    var url: String? = null

    /**
     * 是否进行上传动作
     */
    var isUploading = false

    override fun isVideo(): Boolean {
        return false
    }

    override fun loadImage(context: Context, imageEngine: ImageEngine, imageView: ImageView) {
        imageEngine.loadUriImage(context, imageView, "")
    }


}