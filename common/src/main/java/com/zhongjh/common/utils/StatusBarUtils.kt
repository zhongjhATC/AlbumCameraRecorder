package com.zhongjh.common.utils

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

/**
 * 状态栏
 * @author zhongjh
 */
object StatusBarUtils {

    /**
     * 初始化状态栏
     */
    @JvmStatic
    fun initStatusBar(activity: Activity) {
        val window = activity.window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // 设置状态栏透明
        window.statusBarColor = Color.TRANSPARENT
        // 必须设置高度
        activity.findViewById<FrameLayout>(android.R.id.content).apply {
            post {
                setPadding(0, 0, 0, getNavigationBarsHeight(activity))
            }
        }
    }

    /**
     * 初始化状态栏
     */
    @JvmStatic
    fun initStatusBar(activity: Activity, constraintLayout: ConstraintLayout) {
        val window = activity.window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // 设置状态栏透明
        window.statusBarColor = Color.TRANSPARENT
        // 必须设置高度
        val layoutParams = constraintLayout.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.apply {
            // 补全顶部约束（解决 XML 中 MissingConstraints 警告，同时让 topMargin 生效）
            topToTop = ConstraintLayout.LayoutParams.PARENT_ID // 绑定到父布局顶部
            endToEnd =
                ConstraintLayout.LayoutParams.PARENT_ID // 对应 XML 中 android:layout_alignParentEnd="true"
            startToStart = ConstraintLayout.LayoutParams.PARENT_ID // 补全左侧约束，保证布局居中
            // 设置顶部间距（20dp 对应的像素值）
            topMargin = getNavigationBarsHeight(activity)
        }
    }

    /**
     * 导航栏高度
     */
    private fun getNavigationBarsHeight(activity: Activity): Int {
        val windowInsetsCompat =
            ViewCompat.getRootWindowInsets(activity.findViewById(android.R.id.content)) ?: return 0
        return windowInsetsCompat.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
    }

    /**
     * 获取状态栏高度
     */
    @JvmStatic
    fun getStatusBarHeight(activity: Activity): Int {
        val windowInsetsCompat =
            ViewCompat.getRootWindowInsets(activity.findViewById(android.R.id.content)) ?: return 0
        return windowInsetsCompat.getInsets(WindowInsetsCompat.Type.statusBars()).top
    }


}