package com.zhongjh.common.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity

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
        //设置专栏栏和导航栏的底色，透明
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.navigationBarDividerColor = Color.TRANSPARENT
        }
        activity.findViewById<FrameLayout>(android.R.id.content).apply {
            post {
                setPadding(0, 0, 0, getNavigationBarsHeight(activity))
            }
        }
    }

    /**
     * 导航栏高度
     */
    private fun getNavigationBarsHeight(activity: Activity): Int {
        val windowInsetsCompat = ViewCompat.getRootWindowInsets(activity.findViewById(android.R.id.content)) ?: return 0
        return windowInsetsCompat.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
    }

    /**
     * 获取状态栏高度
     */
    @JvmStatic
    fun getStatusBarHeight(activity: Activity): Int {
        val windowInsetsCompat = ViewCompat.getRootWindowInsets(activity.findViewById(android.R.id.content)) ?: return 0
        return windowInsetsCompat.getInsets(WindowInsetsCompat.Type.statusBars()).top
    }


}