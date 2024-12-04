package com.zhongjh.common.listener

import java.io.File

interface OnProgressUpdateListener {
    fun onProgressUpdate(progress: Double, file: File?)
}