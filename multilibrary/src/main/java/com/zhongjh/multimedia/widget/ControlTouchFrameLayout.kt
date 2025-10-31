package com.zhongjh.multimedia.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

/**
 * 控制不能触发子View的FrameLayout
 *
 * @author zhongjh
 * @date 2022/1/21
 */
class ControlTouchFrameLayout : FrameLayout {

    /**
     * @param context 上下文对象
     * @param attrs XML属性集合
     * @param defStyleAttr 默认样式属性
     */
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null, 0)


    /**
     * 是否启用触摸事件传递给子View
     */
    private var enabled = true

    override fun isEnabled(): Boolean {
        return enabled
    }

    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return if (enabled) {
            super.dispatchTouchEvent(ev)
        } else {
            true
        }
    }
}
