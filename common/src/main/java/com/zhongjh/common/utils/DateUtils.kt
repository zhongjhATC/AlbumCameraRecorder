package com.zhongjh.common.utils

import java.text.SimpleDateFormat
import java.util.Locale

/**
 * @author zhongjh
 * @describe 返时间戳的文件名称
 */
object DateUtils {
    private val sf = SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US)

    /**
     * 根据时间戳创建文件名
     *
     * @param prefix 前缀名
     * @return
     */
    fun getCreateFileName(prefix: String): String {
        val millis = System.currentTimeMillis()
        return prefix + sf.format(millis)
    }
}
