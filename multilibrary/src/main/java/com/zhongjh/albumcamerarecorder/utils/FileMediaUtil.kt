package com.zhongjh.albumcamerarecorder.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.text.TextUtils
import androidx.core.content.FileProvider
import com.zhongjh.albumcamerarecorder.constants.DirType
import com.zhongjh.common.enums.MediaType
import com.zhongjh.common.enums.MediaType.TYPE_AUDIO
import com.zhongjh.common.enums.MediaType.TYPE_PICTURE
import com.zhongjh.common.enums.MediaType.TYPE_VIDEO
import com.zhongjh.common.entity.LocalMedia
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 专门服务Media的文件工具类
 */
object FileMediaUtil {

    private const val JPEG = ".jpeg"
    private const val MP4 = ".mp4"
    private const val AAC = ".aac"
    private const val CAMERA = "Camera"

    fun createFile(context: Context, @DirType dirType: String, fileName: String): File {
        val externalFilesDir: File? = context.getExternalFilesDir("")
        val dirFile = File(externalFilesDir!!.absolutePath, dirType)
        if (!dirFile.exists()) {
            dirFile.mkdirs()
        }
        return File(dirFile.absolutePath + File.separator + fileName)
    }

    /**
     * 创建一个缓存路径
     *
     * @param context 上下文
     * @param type 文件类型
     * @return
     */
    fun createCacheFile(context: Context, @MediaType type: Int): File {
        return createFile(context, type, DirType.CACHE, null)
    }

    /**
     * 基于需要压缩的文件地址创建一个压缩路径
     * 命名规范：pathName_CMP.jpg
     *
     * @param context 上下文
     * @param path 文件地址
     *
     * @return file
     */
    fun createCompressFile(context: Context, path: String): File {
        val externalFilesDir: File? = context.getExternalFilesDir("")
        val tempCameraFile = File(externalFilesDir!!.absolutePath, DirType.COMPRESS)
        if (!tempCameraFile.exists()) {
            tempCameraFile.mkdirs()
        }
        // 获取文件名称
        var newFileName: String = path.substring(path.lastIndexOf(File.separator))
        val newFileNames = newFileName.split(".").toTypedArray()
        // 设置压缩后的照片名称，id_CMP
        if (newFileNames.size > 1) {
            // 设置后缀名
            newFileName = newFileNames[0] + "_CMP" + "." + newFileNames[1]
        }
        return File(tempCameraFile.absolutePath, newFileName)
    }

    /**
     * 基于相册的图片/视频，创建一个压缩路径
     * 命名规范：id_CMP.jpg id_CMP.mp4
     *
     * @param context 上下文
     * @param localMedia 文件实体
     *
     * @return file
     */
    fun createCompressFile(context: Context, localMedia: LocalMedia): File {
        val externalFilesDir: File? = context.getExternalFilesDir("")
        val tempCameraFile = File(externalFilesDir!!.absolutePath, DirType.COMPRESS)
        if (!tempCameraFile.exists()) {
            tempCameraFile.mkdirs()
        }
        val newFileNames = localMedia.absolutePath.split(".").toTypedArray()
        // 设置压缩后的照片名称，id_CMP
        var newFileName = localMedia.id.toString() + "_CMP"
        if (newFileNames.size > 1) {
            // 设置后缀名
            newFileName = newFileName + "." + newFileNames[newFileNames.size - 1]
        }
        return File(tempCameraFile.absolutePath, newFileName)
    }

    /**
     * 服务于Camera
     *
     * @param context 上下文
     * @param fileName 文件名称
     * @param type 文件类型
     * 获取输出路径
     */
    fun getOutFile(context: Context, fileName: String, @MediaType type: Int): File {
        val rootDir: File?
        val folderDir: File
        if (TextUtils.equals(Environment.MEDIA_MOUNTED, Environment.getExternalStorageState())) {
            rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            folderDir = File(rootDir.absolutePath + File.separator + CAMERA + File.separator)
        } else {
            rootDir = getRootDirFile(context, type)
            folderDir = File(rootDir.absolutePath + File.separator)
        }
        if (rootDir?.exists() != true) {
            rootDir?.mkdirs()
        }
        if (!folderDir.exists()) {
            folderDir.mkdirs()
        }
        return File(folderDir.absolutePath, fileName)
    }

    /**
     * 获取文件夹
     */
    fun getDir(outPutDir: String?): String {
        return outPutDir ?: DirType.TEMP
    }

    /**
     * 获取uri
     */
    fun getUri(context: Context, path: String): Uri {
        val authority = context.packageName + ".zhongjhProvider"
        return FileProvider.getUriForFile(context, authority, File(path))
    }

    /**
     * 创建一个临时路径
     *
     * @param context 上下文
     * @param type 文件类型
     * @return
     */
    private fun createTempFile(context: Context, @MediaType type: Int): File? {
        return createFile(context, type, DirType.TEMP, null)
    }

    /**
     * 创建一个路径
     *
     * @param context 上下文
     * @param mediaType 文件类型
     * @param dirType 文件夹类型
     * @param suffix 后缀名
     * @return
     */
    private fun createFile(
        context: Context,
        @MediaType mediaType: Int,
        @DirType dirType: String,
        suffix: String?
    ): File {
        val externalFilesDir: File? = context.getExternalFilesDir("")
        val dirFile = File(externalFilesDir!!.absolutePath, dirType)
        if (!dirFile.exists()) {
            dirFile.mkdirs()
        }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault()).format(Date())
        val fileName = when (mediaType) {
            TYPE_PICTURE -> "IMAGE_" + timeStamp + (suffix ?: JPEG)
            TYPE_VIDEO -> "VIDEO_" + timeStamp + (suffix ?: MP4)
            TYPE_AUDIO -> "AUDIO_" + timeStamp + (suffix ?: AAC)
            else -> throw RuntimeException("The type must be 2-0.")
        }
        return File(dirFile.absolutePath, fileName)
    }

    /**
     * 文件根目录
     *
     * @param context 上下文
     * @param type 文件类型：图片、视频、音频
     * @return 文件根目录
     */
    private fun getRootDirFile(context: Context, type: Int): File {
        return when (type) {
            TYPE_PICTURE -> context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
            else -> context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)!!
        }
    }

}
