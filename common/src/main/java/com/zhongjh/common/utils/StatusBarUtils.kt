package com.zhongjh.common.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowManager

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
        // 设置状态栏为透明并且为全屏模式
        val flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity.window
            val attributes = window.attributes
            window.attributes = attributes
            activity.window.statusBarColor = Color.TRANSPARENT
        } else {
            val window = activity.window
            val attributes = window.attributes
            attributes.flags = attributes.flags or flagTranslucentStatus
            window.attributes = attributes
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    /**
     * 获取状态栏高度
     */
    @JvmStatic
    fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return context.resources.getDimensionPixelSize(resourceId)
    }


}