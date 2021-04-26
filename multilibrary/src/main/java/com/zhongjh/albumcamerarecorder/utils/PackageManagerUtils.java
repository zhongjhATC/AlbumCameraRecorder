package com.zhongjh.albumcamerarecorder.utils;

import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;

/**
 * 有关手机模块工具
 * Created by zhongjh on 2019/3/25.
 */
public class PackageManagerUtils {

    /**
     * 判断是否支持闪光灯
     * @param pm PackageManager
     * @return 是否
     */
    public static boolean isSupportCameraLedFlash(PackageManager pm) {
        if (pm != null) {
            FeatureInfo[] features = pm.getSystemAvailableFeatures();
            if (features != null) {
                for (FeatureInfo f : features) {
                    if (f != null && PackageManager.FEATURE_CAMERA_FLASH.equals(f.name)) {
                        ;
                    }
                    return true;
                }
            }
        }
        return false;
    }

}
