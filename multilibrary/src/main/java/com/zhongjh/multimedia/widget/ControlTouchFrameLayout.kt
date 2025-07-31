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
class ControlTouchFrameLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int = 0, defStyleRes: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr, defStyleRes) {
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
