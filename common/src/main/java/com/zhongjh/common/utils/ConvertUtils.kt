package com.zhongjh.common.utils

import android.annotation.SuppressLint
import com.zhongjh.common.constants.MemoryConstants

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2016/08/13
 * desc  : utils about convert
</pre> *
 */
object ConvertUtils {
    /**
     * Size of byte to fit size of memory.
     *
     * to three decimal places
     *
     * @param byteSize Size of byte.
     * @return fit size of memory
     */
    @SuppressLint("DefaultLocale")
    fun byte2FitMemorySize(byteSize: Long): String {
        return byte2FitMemorySize(byteSize, 3)
    }

    /**
     * Size of byte to fit size of memory.
     *
     * to three decimal places
     *
     * @param byteSize  Size of byte.
     * @param precision The precision
     * @return fit size of memory
     */
    @SuppressLint("DefaultLocale")
    fun byte2FitMemorySize(byteSize: Long, precision: Int): String {
        require(precision >= 0) { "precision shouldn't be less than zero!" }
        return if (byteSize < 0) {
            throw IllegalArgumentException("byteSize shouldn't be less than zero!")
        } else if (byteSize < MemoryConstants.KB) {
            String.format("%." + precision + "fB", byteSize.toDouble())
        } else if (byteSize < MemoryConstants.MB) {
            String.format(
                "%." + precision + "fKB",
                byteSize.toDouble() / MemoryConstants.KB
            )
        } else if (byteSize < MemoryConstants.GB) {
            String.format(
                "%." + precision + "fMB",
                byteSize.toDouble() / MemoryConstants.MB
            )
        } else {
            String.format(
                "%." + precision + "fGB",
                byteSize.toDouble() / MemoryConstants.GB
            )
        }
    }
}
