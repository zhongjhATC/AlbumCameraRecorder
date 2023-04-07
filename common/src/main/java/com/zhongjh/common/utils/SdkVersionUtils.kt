package com.zhongjh.common.utils

import android.os.Build

/**
 * Sdk版本判断
 *
 * @author zhongjh
 * @date 2023/4/7
 */
object SdkVersionUtils {
    /**
     * 判断是否是Android Q版本
     *
     * @return 是否是Android Q版本
     */
    @JvmStatic
    val isQ: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    /**
     * 判断是否是Android R版本
     *
     * @return 是否是Android R版本
     */
    @JvmStatic
    val isR: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
}