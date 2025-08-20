package com.zhongjh.common.utils

import android.util.Log
import com.zhongjh.common.utils.FileUtils.createOrExistsFile
import com.zhongjh.common.utils.FileUtils.getFileByPath
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * 文件流操作
 */
object FileInputOutputUtils {
    private const val TAG = "FileInputOutputUtils"

    /**
     * Write file from input stream.
     *
     * @param filePath The path of file.
     * @param is       The input stream.
     * @return `true`: success<br></br>`false`: fail
     * @noinspection UnusedReturnValue
     */
    @JvmStatic
    fun writeFileFromInputStream(filePath: String?, `is`: InputStream?): Boolean {
        return writeFileFromInputStream(getFileByPath(filePath!!), `is`, false, null)
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
    @JvmStatic
    fun writeFileFromInputStream(file: File?, `is`: InputStream?, append: Boolean, listener: OnProgressUpdateListener?): Boolean {
        if (`is` == null || !createOrExistsFile(file)) {
            Log.e("FileIOUtils", "create file <$file> failed.")
            return false
        }
        var os: OutputStream? = null
        try {
            val sBufferSize = 524288
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
                listener.onProgressUpdate(0.0)
                val data = ByteArray(sBufferSize)
                var len: Int
                while ((`is`.read(data).also { len = it }) != -1) {
                    os.write(data, 0, len)
                    curSize += len
                    listener.onProgressUpdate(curSize / totalSize)
                }
            }
            return true
        } catch (e: IOException) {
            Log.e(TAG, "writeFileFromInputStream" + e.message)
            return false
        } finally {
            try {
                `is`.close()
            } catch (e: IOException) {
                Log.e(TAG, "writeFileFromInputStream" + e.message)
            }
            try {
                os?.close()
            } catch (e: IOException) {
                Log.e(TAG, "writeFileFromInputStream" + e.message)
            }
        }
    }

    interface OnProgressUpdateListener {
        /**
         * 流进度
         *
         * @param progress 进度
         */
        fun onProgressUpdate(progress: Double)
    }
}
