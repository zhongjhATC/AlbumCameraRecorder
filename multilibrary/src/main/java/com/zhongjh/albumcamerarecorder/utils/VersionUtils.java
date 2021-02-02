package com.zhongjh.albumcamerarecorder.utils;

import android.os.Build;

/**
 * 版本比较
 * Created by zhongjh on 2018/8/28.
 */
public class VersionUtils {

    /**
     * 是否>=19
     */
    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

}
