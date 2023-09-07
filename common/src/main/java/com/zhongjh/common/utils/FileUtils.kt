package com.zhongjh.common.utils

import java.io.Closeable
import java.io.File
import java.io.IOException

/**
 * @author Blankj
 * blog  : http://blankj.com
 * time  : 2016/08/16
 * desc  : utils about string
 */
object FileUtils {
    /**
     * Return the file by path.
     *
     * @param filePath The path of file.
     * @return the file
     */
    @JvmStatic
    fun getFileByPath(filePath: String): File? {
        return if (StringUtils.isSpace(filePath)) {
            null
        } else {
            File(filePath)
        }
    }

    /**
     * Create a file if it doesn't exist, otherwise do nothing.
     *
     * @param file The file.
     * @return `true`: exists or creates successfully<br></br>`false`: otherwise
     */
    @JvmStatic
    fun createOrExistsFile(file: File?): Boolean {
        if (file == null) {
            return false
        }
        if (file.exists()) {
            return file.isFile
        }
        return if (!createOrExistsDir(file.parentFile)) {
            false
        } else try {
            file.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 用于关闭inputStream
     * @param c Closeable是属于inputStream的接口
     */
    fun close(c: Closeable?) {
        if (c is Closeable) {
            try {
                c.close()
            } catch (e: Exception) {
                // silence
            }
        }
    }

    /**
     * Create a directory if it doesn't exist, otherwise do nothing.
     *
     * @param file The file.
     * @return `true`: exists or creates successfully<br></br>`false`: otherwise
     */
    private fun createOrExistsDir(file: File?): Boolean {
        return file != null && if (file.exists()) file.isDirectory else file.mkdirs()
    }
}