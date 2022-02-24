package com.zhongjh.common.utils

import android.content.Context

/**
 *
 * 跟App相关的辅助类
 * @author zhongjh
 * @date 2021/9/10
 */
object AppUtils {
    /**
     * 获取应用程序名称
     */
    @JvmStatic
    fun getAppName(context: Context): String {
        val packageManager = context.packageManager
        val packageInfo = packageManager.getPackageInfo(
                context.packageName, 0)
        val labelRes = packageInfo.applicationInfo.labelRes
        return context.resources.getString(labelRes)
    }
}