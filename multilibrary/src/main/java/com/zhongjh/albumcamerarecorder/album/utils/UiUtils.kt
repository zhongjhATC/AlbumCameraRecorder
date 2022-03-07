package com.zhongjh.albumcamerarecorder.album.utils

import android.content.Context
import kotlin.math.roundToInt

/**
 * @author zhongjh
 */
object UiUtils {

    @JvmStatic
    fun spanCount(context: Context, gridExpectedSize: Int): Int {
        val screenWidth = context.resources.displayMetrics.widthPixels
        val expected = screenWidth.toFloat() / gridExpectedSize.toFloat()
        var spanCount = expected.roundToInt()
        if (spanCount == 0) {
            spanCount = 1
        }
        return spanCount
    }

}