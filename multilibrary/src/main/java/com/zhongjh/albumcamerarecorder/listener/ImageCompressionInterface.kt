package com.zhongjh.albumcamerarecorder.listener

import android.content.Context
import java.io.File
import java.io.IOException

/**
 * 压缩图片接口
 *
 * @author zhongjh
 * @date 2021/9/26
 */
interface ImageCompressionInterface {
    /**
     * 压缩图片
     *
     * @param context 上下文
     * @param file    需要压缩的文件
     * @return 压缩后的文件
     * @throws IOException 文件流异常
     */
    @Throws(IOException::class)
    fun compressionFile(context: Context, file: File): File
}