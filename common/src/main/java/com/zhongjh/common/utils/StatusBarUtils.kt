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
        constraintLayout.setPadding(
            constraintLayout.paddingLeft,  // 保留原有左 padding
            getNavigationBarsHeight(activity),                          // 顶部 padding 设为
            constraintLayout.paddingRight, // 保留原有右 padding
            constraintLayout.paddingBottom // 保留原有底 padding
        )
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