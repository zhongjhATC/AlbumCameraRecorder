package com.zhongjh.common.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.text.TextUtils
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
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

    @JvmStatic
    fun deleteFile(path: String?): Boolean {
        val file = File(path)
        return deleteFile(file)
    }

    /**
     * Delete the file.
     *
     * @param file The file.
     * @return `true`: success<br></br>`false`: fail
     */
    @JvmStatic
    fun deleteFile(file: File?): Boolean {
        return file != null && (!file.exists() || file.isFile && file.delete())
    }

    /**
     * Copy the directory or file.
     *
     * @param src      The source.
     * @param dest     The destination.
     * @param listener The replace listener.
     * @return `true`: success<br></br>`false`: fail
     */
    /**
     * Copy the directory or file.
     *
     * @param src  The source.
     * @param dest The destination.
     * @return `true`: success<br></br>`false`: fail
     */
    @JvmOverloads @JvmStatic
    fun copy(
        src: File?,
        dest: File,
        listener: OnReplaceListener? = null,
        onProgressUpdateListener: FileIOUtils.OnProgressUpdateListener? = null
    ): Boolean {
        if (src == null) {
            return false
        }
        if (src.isDirectory) {
            return copyDir(src, dest, listener, onProgressUpdateListener)
        }
        return copyFile(src, dest, listener, onProgressUpdateListener)
    }

    /**
     * Copy the directory or file.
     *
     * @param src      The source.
     * @param dest     The destination.
     * @param listener The replace listener.
     * @return `true`: success<br></br>`false`: fail
     */
    /**
     * Copy the directory or file.
     *
     * @param src  The source.
     * @param dest The destination.
     * @return `true`: success<br></br>`false`: fail
     */
    @JvmOverloads
    fun copy(
        context: Context,
        src: Uri?,
        dest: File,
        listener: OnReplaceListener? = null,
        onProgressUpdateListener: FileIOUtils.OnProgressUpdateListener? = null
    ): Boolean {
        if (src == null) {
            return false
        }
        return copyFile(context, src, dest, listener, onProgressUpdateListener)
    }

    /**
     * Move the directory or file.
     *
     * @param src      The source.
     * @param dest     The destination.
     * @param listener The replace listener.
     * @return `true`: success<br></br>`false`: fail
     */
    /**
     * Move the directory or file.
     *
     * @param src  The source.
     * @param dest The destination.
     * @return `true`: success<br></br>`false`: fail
     */
    @JvmOverloads
    fun move(
        src: File?,
        dest: File?,
        listener: OnReplaceListener? = null,
        onProgressUpdateListener: FileIOUtils.OnProgressUpdateListener? = null
    ): Boolean {
        if (src == null) {
            return false
        }
        if (src.isDirectory) {
            return moveDir(src, dest, listener, onProgressUpdateListener)
        }
        return moveFile(src, dest, listener, onProgressUpdateListener)
    }

    /**
     * Copy the directory.
     *
     * @param srcDir   The source directory.
     * @param destDir  The destination directory.
     * @param listener The replace listener.
     * @return `true`: success<br></br>`false`: fail
     */
    private fun copyDir(
        srcDir: File,
        destDir: File,
        listener: OnReplaceListener?,
        onProgressUpdateListener: FileIOUtils.OnProgressUpdateListener?
    ): Boolean {
        return copyOrMoveDir(srcDir, destDir, listener, onProgressUpdateListener, false)
    }

    /**
     * Copy the file.
     *
     * @param srcFile  The source file.
     * @param destFile The destination file.
     * @param listener The replace listener.
     * @return `true`: success<br></br>`false`: fail
     */
    private fun copyFile(
        srcFile: File,
        destFile: File,
        listener: OnReplaceListener?,
        onProgressUpdateListener: FileIOUtils.OnProgressUpdateListener?
    ): Boolean {
        return copyOrMoveFile(srcFile, destFile, listener, onProgressUpdateListener, false)
    }

    /**
     * Copy the file.
     *
     * @param srcFile  The source file.
     * @param destFile The destination file.
     * @param listener The replace listener.
     * @return `true`: success<br></br>`false`: fail
     */
    private fun copyFile(
        context: Context,
        srcFile: Uri,
        destFile: File,
        listener: OnReplaceListener?,
        onProgressUpdateListener: FileIOUtils.OnProgressUpdateListener?
    ): Boolean {
        return copyOrMoveFile(context, srcFile, destFile, listener, onProgressUpdateListener, false)
    }

    /**
     * Move the directory.
     *
     * @param srcDir   The source directory.
     * @param destDir  The destination directory.
     * @param listener The replace listener.
     * @return `true`: success<br></br>`false`: fail
     */
    fun moveDir(
        srcDir: File?,
        destDir: File?,
        listener: OnReplaceListener?,
        onProgressUpdateListener: FileIOUtils.OnProgressUpdateListener?
    ): Boolean {
        return copyOrMoveDir(srcDir, destDir, listener, onProgressUpdateListener, true)
    }

    /**
     * Move the file.
     *
     * @param srcFile  The source file.
     * @param destFile The destination file.
     * @param listener The replace listener.
     * @return `true`: success<br></br>`false`: fail
     */
    fun moveFile(
        srcFile: File?,
        destFile: File?,
        listener: OnReplaceListener?,
        onProgressUpdateListener: FileIOUtils.OnProgressUpdateListener?
    ): Boolean {
        return copyOrMoveFile(srcFile, destFile, listener, onProgressUpdateListener, true)
    }

    private fun copyOrMoveDir(
        srcDir: File?,
        destDir: File?,
        listener: OnReplaceListener?,
        onProgressUpdateListener: FileIOUtils.OnProgressUpdateListener?,
        isMove: Boolean
    ): Boolean {
        if (srcDir == null || destDir == null) {
            return false
        }
        // destDir's path locate in srcDir's path then return false
        val srcPath = srcDir.path + File.separator
        val destPath = destDir.path + File.separator
        if (destPath.contains(srcPath)) {
            return false
        }
        if (!srcDir.exists() || !srcDir.isDirectory) {
            return false
        }
        if (!createOrExistsDir(destDir)) {
            return false
        }
        val files = srcDir.listFiles()
        if (files != null && files.size > 0) {
            for (file in files) {
                val oneDestFile = File(destPath + file.name)
                if (file.isFile) {
                    if (!copyOrMoveFile(file, oneDestFile, listener, onProgressUpdateListener, isMove)) {
                        return false
                    }
                } else if (file.isDirectory) {
                    if (!copyOrMoveDir(file, oneDestFile, listener, onProgressUpdateListener, isMove)) {
                        return false
                    }
                }
            }
        }
        return !isMove || deleteDir(srcDir)
    }

    private fun copyOrMoveFile(
        srcFile: File?,
        destFile: File?,
        listener: OnReplaceListener?,
        onProgressUpdateListener: FileIOUtils.OnProgressUpdateListener?,
        isMove: Boolean
    ): Boolean {
        if (srcFile == null || destFile == null) {
            return false
        }
        // srcFile equals destFile then return false
        if (srcFile == destFile) {
            return false
        }
        // srcFile doesn't exist or isn't a file then return false
        if (!srcFile.exists() || !srcFile.isFile) {
            return false
        }
        if (destFile.exists()) {
            // require delete the old file
            if (listener == null || listener.onReplace(srcFile, destFile)) {
                // unsuccessfully delete then return false
                if (!destFile.delete()) {
                    return false
                }
            } else {
                return true
            }
        }
        if (!createOrExistsDir(destFile.parentFile)) {
            return false
        }
        try {
            return (FileIOUtils.writeFileFromIS(destFile, FileInputStream(srcFile), false, onProgressUpdateListener)
                    && !(isMove && !deleteFile(srcFile)))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return false
        }
    }

    private fun copyOrMoveFile(
        context: Context,
        srcFile: Uri?,
        destFile: File?,
        listener: OnReplaceListener?,
        onProgressUpdateListener: FileIOUtils.OnProgressUpdateListener?,
        isMove: Boolean
    ): Boolean {
        if (srcFile == null || destFile == null) {
            return false
        }
        if (destFile.exists()) {
            // require delete the old file
            if (listener == null) {
                // unsuccessfully delete then return false
                if (!destFile.delete()) {
                    return false
                }
            } else {
                return true
            }
        }
        if (!createOrExistsDir(destFile.parentFile)) {
            return false
        }
        try {
            val os = context.contentResolver.openInputStream(srcFile)
            return (FileIOUtils.writeFileFromIS(destFile, os, false, onProgressUpdateListener)
                    && !isMove)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Create a directory if it doesn't exist, otherwise do nothing.
     *
     * @param file The file.
     * @return `true`: exists or creates successfully<br></br>`false`: otherwise
     */
    fun createOrExistsDir(file: File?): Boolean {
        return file != null && (if (file.exists()) file.isDirectory else file.mkdirs())
    }

    /**
     * Delete the directory.
     *
     * @param dir The directory.
     * @return `true`: success<br></br>`false`: fail
     */
    @JvmStatic
    fun deleteDir(dir: File?): Boolean {
        if (dir == null) {
            return false
        }
        // dir doesn't exist then return true
        if (!dir.exists()) {
            return true
        }
        // dir isn't a directory then return false
        if (!dir.isDirectory) {
            return false
        }
        val files = dir.listFiles()
        if (files != null && files.size > 0) {
            for (file in files) {
                if (file.isFile) {
                    if (!file.delete()) {
                        return false
                    }
                } else if (file.isDirectory) {
                    if (!deleteDir(file)) {
                        return false
                    }
                }
            }
        }
        return dir.delete()
    }

    /**
     * Create a file if it doesn't exist, otherwise do nothing.
     *
     * @param filePath The path of file.
     * @return `true`: exists or creates successfully<br></br>`false`: otherwise
     */
    fun createOrExistsFile(filePath: String): Boolean {
        return createOrExistsFile(getFileByPath(filePath))
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
        if (!createOrExistsDir(file.parentFile)) {
            return false
        }
        try {
            return file.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Return the size.
     *
     * @param file The directory.
     * @return the size
     */
    @JvmStatic
    fun getSize(file: File?): String {
        if (file == null) {
            return ""
        }
        if (file.isDirectory) {
            return getDirSize(file)
        }
        return getFileSize(file)
    }

    /**
     * Return the size of file.
     *
     * @param file The file.
     * @return the length of file
     */
    private fun getFileSize(file: File): String {
        val len = getFileLength(file)
        return if (len == -1L) "" else ConvertUtils.byte2FitMemorySize(len)
    }

    /**
     * Return the length of file.
     *
     * @param file The file.
     * @return the length of file
     */
    private fun getFileLength(file: File): Long {
        if (!isFile(file)) {
            return -1
        }
        return file.length()
    }

    /**
     * Return the size of directory.
     *
     * @param dir The directory.
     * @return the size of directory
     */
    private fun getDirSize(dir: File): String {
        val len = getDirLength(dir)
        return if (len == -1L) "" else ConvertUtils.byte2FitMemorySize(len)
    }

    /**
     * Return the length of directory.
     *
     * @param dir The directory.
     * @return the length of directory
     */
    private fun getDirLength(dir: File): Long {
        if (!isDir(dir)) {
            return 0
        }
        var len: Long = 0
        val files = dir.listFiles()
        if (files != null && files.size > 0) {
            for (file in files) {
                len += if (file.isDirectory) {
                    getDirLength(file)
                } else {
                    file.length()
                }
            }
        }
        return len
    }

    /**
     * Return whether it is a directory.
     *
     * @param file The file.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isDir(file: File?): Boolean {
        return file != null && file.exists() && file.isDirectory
    }

    /**
     * Return whether it is a file.
     *
     * @param file The file.
     * @return `true`: yes<br></br>`false`: no
     */
    fun isFile(file: File?): Boolean {
        return file != null && file.exists() && file.isFile
    }

    interface OnReplaceListener {
        fun onReplace(srcFile: File?, destFile: File?): Boolean
    }
}