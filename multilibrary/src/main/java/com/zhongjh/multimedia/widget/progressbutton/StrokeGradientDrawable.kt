package com.zhongjh.multimedia.widget.progressbutton

import android.graphics.drawable.GradientDrawable

class StrokeGradientDrawable(val gradientDrawable: GradientDrawable) {
    private var mStrokeWidth = 0
    private var mStrokeColor = 0

    var strokeWidth: Int
        get() = mStrokeWidth
        set(strokeWidth) {
            mStrokeWidth = strokeWidth
            gradientDrawable.setStroke(strokeWidth, strokeColor)
        }

    var strokeColor: Int
        get() = mStrokeColor
        set(strokeColor) {
            mStrokeColor = strokeColor
            gradientDrawable.setStroke(strokeWidth, strokeColor)
        }
}
