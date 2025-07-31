package com.zhongjh.multimedia.widget

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import com.zhongjh.common.utils.StatusBarUtils.getStatusBarHeight
import kotlin.math.abs

/**
 * 自定义的一个Behavior，滑动控件时自动隐藏底部控件
 * 为了性能效率考虑，只服务于FrameLayoutBehavior
 *
 * @author zhongjh
 * @date 2022/8/10
 */
class BottomBarBehavior(context: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<ConstraintLayoutBehavior>(context, attrs) {
    /**
     * 状态栏高度
     */
    private var statusBarHeight: Int = 0

    init {
        if (context is Activity) {
            statusBarHeight = getStatusBarHeight(context)
        }
    }

    override fun layoutDependsOn(parent: CoordinatorLayout, child: ConstraintLayoutBehavior, dependency: View): Boolean {
        // 说明子控件依赖AppBarLayout
        return dependency is AppBarLayout
    }


    override fun onDependentViewChanged(parent: CoordinatorLayout, child: ConstraintLayoutBehavior, dependency: View): Boolean {
        // 顶部的AppBarLayout是paddingTop状态栏高度的
        child.translationY = abs((dependency.top - statusBarHeight).toDouble()).toFloat()
        child.onListener?.onDependentViewChanged(abs((dependency.top - statusBarHeight).toDouble()).toFloat())
        return true
    }
}
