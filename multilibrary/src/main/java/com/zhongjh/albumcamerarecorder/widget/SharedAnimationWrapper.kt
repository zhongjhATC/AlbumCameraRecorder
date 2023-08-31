package com.zhongjh.albumcamerarecorder.widget

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

/**
 * 封装共享View
 */
class SharedAnimationWrapper(private val viewWrapper: View) {

    private val params: ViewGroup.MarginLayoutParams =
        viewWrapper.layoutParams as ViewGroup.MarginLayoutParams

    init {
        if (params is LinearLayout.LayoutParams) {
            params.gravity = Gravity.START
        }
    }

}