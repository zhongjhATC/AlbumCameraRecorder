package com.zhongjh.common.listener

import java.io.File

fun interface OnProgressUpdateListener {
    fun onProgressUpdate(progress: Double, file: File?)
}