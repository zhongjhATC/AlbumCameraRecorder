package com.zhongjh.common.utils

import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build

/**
 * @author zhongjh
 * @date 2021/4/29
 */
object ColorFilterUtil {
    /**
     * 为了兼容处理图片赋值颜色信息
     * @param drawable 图片
     * @param color 颜色
     */
    @JvmStatic
    fun setColorFilterSrcIn(drawable: Drawable, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            drawable.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_IN)
        } else {
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
    }
}