package com.zhongjh.multimedia.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout

/**
 * 让高度跟宽度一样，正方形布局
 */
class SquareRelativeLayout : RelativeLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}