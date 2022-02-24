package com.zhongjh.common.utils

import android.text.TextUtils

/**
 * 类型工具类
 *
 * @author zhongjh
 * @date 2022/2/8
 */
object MimeTypeUtils {
    /**
     * is content://
     *
     * @param uri uri
     * @return 判断uri是否content类型
     */
    fun isContent(uri: String): Boolean {
        return if (TextUtils.isEmpty(uri)) {
            false
        } else {
            uri.startsWith("content://")
        }
    }
}