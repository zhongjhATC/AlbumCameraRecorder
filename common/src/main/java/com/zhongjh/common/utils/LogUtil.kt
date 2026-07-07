package com.zhongjh.common.utils

import android.util.Log
import com.squareup.picasso.BuildConfig

/**
 * 日志工具
 *
 * @author zhongjh
 */
object LogUtil {
    /**
     * 调试日志总开关，发布版本改成 false 即可屏蔽所有Log
     */
    private const val DEBUG_LOG = true
    private const val DEFAULT_TAG = "zhongjh"

    fun i(tag: String, msg: String) {
        if (BuildConfig.DEBUG && DEBUG_LOG) {
            Log.i(tag, msg)
        }
    }

    fun v(tag: String, msg: String) {
        if (BuildConfig.DEBUG && DEBUG_LOG) {
            Log.v(tag, msg)
        }
    }

    fun d(tag: String, msg: String) {
        if (BuildConfig.DEBUG && DEBUG_LOG) {
            Log.d(tag, msg)
        }
    }

    fun e(tag: String, msg: String) {
        if (BuildConfig.DEBUG && DEBUG_LOG) {
            Log.e(tag, msg)
        }
    }

    fun e(tag: String, msg: String, tr: Throwable?) {
        if (BuildConfig.DEBUG && DEBUG_LOG) {
            Log.e(tag, msg, tr)
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
