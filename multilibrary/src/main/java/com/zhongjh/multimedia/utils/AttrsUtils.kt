package com.zhongjh.multimedia.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat

/**
 * 动态获取attrs
 *
 * @author zhongjh
 */
object AttrsUtils {
    /**
     * 通过resId获取资源系列，再获取它里面的某个资源
     * attrs drawable
     *
     * @param context 上下文
     * @param resId   资源系列一套
     * @param attr    需要获取资源的id
     * @return 字体大小
     */
    fun getTypeValueSizeForInt(context: Context, @StyleRes resId: Int, attr: Int): Int {
        val attribute = intArrayOf(attr)
        val array = context.theme.obtainStyledAttributes(resId, attribute)
        val textSize = array.getDimensionPixelSize(0, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            array.close()
        } else {
            array.recycle()
        }
        return textSize
    }

    /**
     * 通过resId获取资源系列，再获取它里面的某个资源
     * attrs drawable
     *
     * @param context 上下文
     * @param resId   资源系列一套
     * @param attr    需要获取资源的id
     * @return 颜色id
     */
    fun getTypeValueColor(context: Context, @StyleRes resId: Int, attr: Int): Int {
        val attribute = intArrayOf(attr)
        val array = context.theme.obtainStyledAttributes(resId, attribute)
        val color = array.getColor(0, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            array.close()
        } else {
            array.recycle()
        }
        return color
    }

    /**
     * 通过resId获取资源系列，再获取它里面的某个资源
     * attrs drawable
     *
     * @param context      上下文
     * @param resId        资源系列一套
     * @param attr         需要获取资源的id
     * @param defaultResId 默认图片id
     * @return 图片id
     */
    fun getTypeValueDrawable(context: Context, @StyleRes resId: Int, attr: Int, defaultResId: Int): Drawable? {
        val attribute = intArrayOf(attr)
        val array = context.theme.obtainStyledAttributes(resId, attribute)
        val drawable = array.getDrawable(0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            array.close()
        } else {
            array.recycle()
        }
        return drawable ?: ContextCompat.getDrawable(context, defaultResId)
    }
}
