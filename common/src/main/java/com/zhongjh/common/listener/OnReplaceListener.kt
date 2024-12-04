package com.zhongjh.common.listener

import java.io.File

interface OnReplaceListener {
    fun onReplace(srcFile: File?, destFile: File?): Boolean
}