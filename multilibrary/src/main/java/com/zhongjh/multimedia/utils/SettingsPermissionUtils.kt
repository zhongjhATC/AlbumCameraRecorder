package com.zhongjh.multimedia.utils

import android.content.Intent
import android.net.Uri
import android.provider.Settings

/**
 * 权限相关工具类（封装应用设置页面跳转与回调）
 */
object SettingsPermissionUtils {
    /**
     * 创建打开应用设置页面的 Intent
     * @param packageName 当前应用包名
     */
    fun createAppSettingsIntent(packageName: String): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
    }

}