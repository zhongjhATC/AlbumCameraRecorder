package com.zhongjh.common.utils

import android.util.Log
import com.zhongjh.common.listener.OnProgressUpdateListener
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * 文件流工具类
 *
 * @author zhongjh
 * @date 2021/9/18
 */
object FileIOUtils {
    private const val sBufferSize = 524288

    /**
     * Write file from input stream.
     *
     * @param filePath The path of file.
     * @param is       The input stream.
     * @return `true`: success<br></br>`false`: fail
     */
    fun writeFileFromIS(filePath: String, `is`: InputStream?): Boolean {
        val file = FileUtils.getFileByPath(filePath)
        file?.let {
            return writeFileFromIS(it, `is`, false, null)
        } ?: let {
            return false
        }
    }

    /**
     * Write file from input stream.
     *
     * @param file     The file.
     * @param is       The input stream.
     * @param append   True to append, false otherwise.
     * @param listener The progress update listener.
     * @return `true`: success<br></br>`false`: fail
     */
    fun writeFileFromIS(
        file: File,
        `is`: InputStream?,
        append: Boolean,
        listener: OnProgressUpdateListener?
    ): Boolean {
        if (`is` == null || !FileUtils.createOrExistsFile(file)) {
            Log.e("FileIOUtils", "create file <$file> failed.")
            return false
        }
        var os: OutputStream? = null
        try {
            os = BufferedOutputStream(FileOutputStream(file, append), sBufferSize)
            if (listener == null) {
                val data = ByteArray(sBufferSize)
                var len: Int
                while ((`is`.read(data).also { len = it }) != -1) {
                    os.write(data, 0, len)
                }
            } else {
                val totalSize = `is`.available().toDouble()
                var curSize = 0
                listener.onProgressUpdate(0.0, file)
                val data = ByteArray(sBufferSize)
                var len: Int
                while ((`is`.read(data).also { len = it }) != -1) {
                    os.write(data, 0, len)
                    curSize += len
                    listener.onProgressUpdate(curSize / totalSize, file)
                }
            }
            Log.e("FileIOUtils", "return true")
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } finally {
            try {
                `is`.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                os?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
