package com.zhongjh.albumcamerarecorder.utils

import android.content.pm.PackageManager

/**
 * 有关手机模块工具
 *
 * @author zhongjh
 * @date 2019/3/25
 */
object PackageManagerUtils {
    /**
     * 判断是否支持闪光灯
     * @param pm PackageManager
     * @return 是否
     */
    @JvmStatic
    fun isSupportCameraLedFlash(pm: PackageManager?): Boolean {
        if (pm != null) {
            val features = pm.systemAvailableFeatures
            for (ignored in features) {
                return true
            }
        }
        return false
    }
}