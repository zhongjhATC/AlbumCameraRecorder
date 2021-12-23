package com.zhongjh.albumcamerarecorder.camera.util;

import android.content.Context;

import com.zhongjh.common.utils.DisplayMetricsUtils;

/**
 * 获取手机分辨率的宽高，先从缓存获取，如果缓存都没有，则重新计算，并且保存
 *
 * @author zhongjh
 * @date 2017/10/25
 */
public class DisplayMetricsSpUtils {

    private static final String CAMERAVIEWSOUNDRECORDERSHAREDPREFERENCES = "cameraviewsoundrecorderSharedPreferences";

    /**
     * 屏幕宽度
     */
    public static final String SCREEN_WIDTH = "ScreenWidth";
    /**
     * 屏幕高度
     */
    public static final String SCREEN_HEIGHT = "ScreenHeight";

    /**
     * 获取屏幕分辨率- 宽
     *
     * @param context 上下文
     * @return 宽
     */
    public static int getScreenWidth(Context context) {
        SharedPreferencesUtil sharedPreferencesUtil = new SharedPreferencesUtil(context, CAMERAVIEWSOUNDRECORDERSHAREDPREFERENCES);
        if (sharedPreferencesUtil.getInt(SCREEN_WIDTH, 0) == 0) {
            sharedPreferencesUtil.putInt(SCREEN_WIDTH, DisplayMetricsUtils.getScreenWidth(context));
            sharedPreferencesUtil.putInt(SCREEN_HEIGHT, DisplayMetricsUtils.getScreenHeight(context));
        }
        return sharedPreferencesUtil.getInt(SCREEN_WIDTH, 0);
    }

    /**
     * 获取屏幕分辨率- 高
     *
     * @param context 上下文
     * @return 高
     */
    public static int getScreenHeight(Context context) {
        SharedPreferencesUtil sharedPreferencesUtil = new SharedPreferencesUtil(context, CAMERAVIEWSOUNDRECORDERSHAREDPREFERENCES);
        if (sharedPreferencesUtil.getInt(SCREEN_HEIGHT, 0) == 0) {
            sharedPreferencesUtil.putInt(SCREEN_WIDTH, DisplayMetricsUtils.getScreenWidth(context));
            sharedPreferencesUtil.putInt(SCREEN_HEIGHT, DisplayMetricsUtils.getScreenHeight(context));
        }
        return sharedPreferencesUtil.getInt(SCREEN_HEIGHT, 0);
    }


}
