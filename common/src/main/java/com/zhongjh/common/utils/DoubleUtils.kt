package com.zhongjh.common.utils

import android.os.SystemClock

/**
 * 防止抖动点击
 * @author zhongjh
 * @date 2023/09/08
 */
object DoubleUtils {
    private const val TIME: Long = 600

    private var lastClickTime: Long = 0

    @JvmStatic
    fun isFastDoubleClick(): Boolean {
        val time = SystemClock.elapsedRealtime()
        if (time - lastClickTime < TIME) {
            return true
        }
        lastClickTime = time
        return false
    }
}