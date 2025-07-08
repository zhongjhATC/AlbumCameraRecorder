package com.zhongjh.common.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowInsets
import android.view.WindowManager

/**
 * 获取屏幕分辨率
 * @author zhongjh
 * @date 2021/11/18
 */
object DisplayMetricsUtils {

    /**
     * 获取屏幕分辨率-宽
     *
     * @param context 上下文
     * @return 宽
     */
    @JvmStatic
    fun getScreenWidth(context: Context): Int {
        val localDisplayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(localDisplayMetrics)
        return localDisplayMetrics.widthPixels
    }

    /**
     * 获取屏幕分辨率-高
     *
     * @param context 上下文
     * @return 高
     */
    @JvmStatic
    fun getScreenHeight(context: Context): Int {
        val localDisplayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(localDisplayMetrics)
        return localDisplayMetrics.heightPixels
    }

    /**
     * 获取屏幕真实宽度
     *
     * @param context
     * @return
     */
    fun getRealScreenWidth(context: Context): Int {
        val wm = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        wm.defaultDisplay.getRealSize(point)
        return point.x
    }

    /**
     * 获取屏幕真实高度
     *
     * @param context
     */
    fun getRealScreenHeight(context: Context): Int {
        val wm = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        wm.defaultDisplay.getRealSize(point)
        return point.y
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     *
     * @param dpValue 值
     * @return 转换结果
     */
    @JvmStatic
    fun dip2px(dpValue: Float): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

}