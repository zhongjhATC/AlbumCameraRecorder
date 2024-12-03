package com.zhongjh.common.constants

import androidx.annotation.IntDef

/**
 * 文件大小值
 *
 * @author zhongjh
 * @date 2024/11/26
 */
object MemoryConstants {
    const val BYTE = 1
    const val KB = 1024
    const val MB = 1048576
    const val GB = 1073741824

    @IntDef(BYTE, KB, MB, GB)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Unit
}