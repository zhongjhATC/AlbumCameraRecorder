package com.zhongjh.common.entity

import android.content.Context
import android.widget.ImageView
import com.zhongjh.common.engine.ImageEngine

class OnLineMedia : BaseMedia {

    override fun isVideo(): Boolean {
        return false
    }

    override fun loadImage(context: Context, imageEngine: ImageEngine, imageView: ImageView) {
        imageEngine.loadUriImage(context, imageView, "")
    }


}