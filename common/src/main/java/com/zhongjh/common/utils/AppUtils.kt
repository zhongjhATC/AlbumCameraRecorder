package com.zhongjh.common.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

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
        val appInfo: ApplicationInfo
        try {
            appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            return appInfo.loadLabel(context.packageManager).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return "AlbumCameraRecorder"
    }

}