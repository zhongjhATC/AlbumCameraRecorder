package com.zhongjh.multimedia.camera.util

import android.util.Log
import com.github.chrisbanes.photoview.BuildConfig

/**
 * 日志工具
 *
 * @author zhongjh
 */
object LogUtil {
    private const val DEFAULT_TAG = "zhongjh"

    fun i(tag: String, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, msg)
        }
    }

    fun v(tag: String, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.v(tag, msg)
        }
    }

    fun d(tag: String, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg)
        }
    }

    fun e(tag: String, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg)
        }
    }

    fun i(msg: String) {
        i(DEFAULT_TAG, msg)
    }

    fun v(msg: String) {
        v(DEFAULT_TAG, msg)
    }

    fun d(msg: String) {
        d(DEFAULT_TAG, msg)
    }

    fun e(msg: String) {
        e(DEFAULT_TAG, msg)
    }
}
