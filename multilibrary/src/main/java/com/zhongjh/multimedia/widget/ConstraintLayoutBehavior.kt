package com.zhongjh.multimedia.widget

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout

/**
 * 用于专门服务于app:layout_behavior的，通过自定义view的自定义事件，进行联动
 *
 * @author zhongjh
 * @date 2022/8/11
 */
class ConstraintLayoutBehavior : ConstraintLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    /**
     * 点击或长按监听结束后的 确认取消事件监控
     */
    var onListener: Listener? = null

    /**
     * 操作按钮的Listener
     */
    fun interface Listener {
        /**
         * 触发滑动事件
         *
         * @param translationY 设置Y轴距离
         */
        fun onDependentViewChanged(translationY: Float)
    }
}
