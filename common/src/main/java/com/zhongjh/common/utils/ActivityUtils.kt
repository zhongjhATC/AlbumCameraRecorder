package com.zhongjh.common.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * 跟Activity相关的辅助类
 * @author zhongjh
 * @date 2023/11/17
 */
object ActivityUtils {

    /**
     * 当前上下文是否有效
     */
    fun assertValidRequest(context: Context?): Boolean {
        if (context is Activity) {
            return !isDestroy(context)
        } else if (context is ContextWrapper) {
            if (context.baseContext is Activity) {
                val activity = context.baseContext as Activity
                return !isDestroy(activity)
            }
        }
        return true
    }

    /**
     * Activity是否被销毁
     */
    private fun isDestroy(activity: Activity?): Boolean {
        activity?.let {
            return activity.isFinishing || activity.isDestroyed
        } ?: let {
            return true
        }
    }

}