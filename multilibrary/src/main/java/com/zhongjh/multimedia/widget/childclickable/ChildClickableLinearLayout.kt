package com.zhongjh.multimedia.widget.childclickable

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout

/**
 * 是否允许子控件可以点击
 * 继承于[LinearLayout]
 * @author zhongjh
 * @date 2018/12/27
 */
class ChildClickableLinearLayout : LinearLayout, IChildClickableLayout {
    /**
     * 子控件是否可以接受点击事件
     */
    private var childClickable = true

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // 返回true则拦截子控件所有点击事件，如果childClickable为true，则需返回false
        return !childClickable
    }

    override fun setChildClickable(clickable: Boolean) {
        childClickable = clickable
    }
}
