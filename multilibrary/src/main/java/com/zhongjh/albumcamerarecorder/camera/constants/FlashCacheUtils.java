package com.zhongjh.albumcamerarecorder.camera.constants;

import android.content.Context;

import androidx.camera.core.ImageCapture;

import com.zhongjh.albumcamerarecorder.camera.util.SharedPreferencesUtil;

/**
 * 记忆模式下缓存闪光灯
 *
 * @author zhongjh
 * @date 2021/12/23
 */
public class FlashCacheUtils {

    private static final String CAMERAVIEWSOUNDRECORDERSHAREDPREFERENCESFLASHCACHE = "CAMERAVIEWSOUNDRECORDERSHAREDPREFERENCESFLASHCACHE";

    /**
     * 记忆模式下的闪光灯模式
     */
    public static final String FLASH_MODE = "FlashMode";

    /**
     * 获取记忆模式下的闪光灯模式
     *
     * @param context 上下文
     * @return 闪光灯模式
     */
    public static int getFlashModel(Context context) {
        SharedPreferencesUtil sharedPreferencesUtil = new SharedPreferencesUtil(context, CAMERAVIEWSOUNDRECORDERSHAREDPREFERENCESFLASHCACHE);
        return sharedPreferencesUtil.getInt(FLASH_MODE, ImageCapture.FLASH_MODE_OFF);
    }

    /**
     * 存储记忆模式下的闪光灯模式
     *
     * @param context 上下文
     * @param flashModel 闪光灯模式
     */
    public static void saveFlashModel(Context context, int flashModel) {
        SharedPreferencesUtil sharedPreferencesUtil = new SharedPreferencesUtil(context, CAMERAVIEWSOUNDRECORDERSHAREDPREFERENCESFLASHCACHE);
        sharedPreferencesUtil.putInt(FLASH_MODE, flashModel);
    }

}
