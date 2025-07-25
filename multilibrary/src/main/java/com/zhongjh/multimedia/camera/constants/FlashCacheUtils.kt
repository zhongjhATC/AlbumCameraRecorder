package com.zhongjh.multimedia.camera.constants

import android.content.Context
import androidx.camera.core.ImageCapture
import com.zhongjh.multimedia.camera.util.SharedPreferencesUtil

/**
 * 记忆模式下缓存闪光灯
 *
 * @author zhongjh
 * @date 2021/12/23
 */
object FlashCacheUtils {
    private const val CAMERAVIEWSOUNDRECORDERSHAREDPREFERENCESFLASHCACHE = "CAMERAVIEWSOUNDRECORDERSHAREDPREFERENCESFLASHCACHE"

    /**
     * 记忆模式下的闪光灯模式
     */
    private const val FLASH_MODE: String = "FlashMode"

    /**
     * 获取记忆模式下的闪光灯模式
     *
     * @param context 上下文
     * @return 闪光灯模式
     */
    @JvmStatic
    fun getFlashModel(context: Context): Int {
        val sharedPreferencesUtil = SharedPreferencesUtil(context, CAMERAVIEWSOUNDRECORDERSHAREDPREFERENCESFLASHCACHE)
        return sharedPreferencesUtil.getInt(FLASH_MODE, ImageCapture.FLASH_MODE_OFF)
    }

    /**
     * 存储记忆模式下的闪光灯模式
     *
     * @param context 上下文
     * @param flashModel 闪光灯模式
     */
    @JvmStatic
    fun saveFlashModel(context: Context, flashModel: Int) {
        val sharedPreferencesUtil = SharedPreferencesUtil(context, CAMERAVIEWSOUNDRECORDERSHAREDPREFERENCESFLASHCACHE)
        sharedPreferencesUtil.putInt(FLASH_MODE, flashModel)
    }
}
