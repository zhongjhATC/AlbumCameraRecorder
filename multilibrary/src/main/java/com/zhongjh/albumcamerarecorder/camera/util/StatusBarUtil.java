package com.zhongjh.albumcamerarecorder.camera.util;

import android.content.Context;

/**
 * 状态栏工具类
 */
public class StatusBarUtil {

    /**
     * 获取状态栏高度
     */
    public static int getStatusBarHeight(Context context){
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return context.getResources().getDimensionPixelSize(resourceId);
    }

}