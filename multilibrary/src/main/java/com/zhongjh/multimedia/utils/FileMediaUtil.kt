package com.zhongjh.multimedia.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.text.TextUtils
import androidx.core.content.FileProvider
import com.zhongjh.common.enums.MediaType
import com.zhongjh.common.enums.MediaType.TYPE_AUDIO
import com.zhongjh.common.enums.MediaType.TYPE_PICTURE
import com.zhongjh.common.enums.MediaType.TYPE_VIDEO
import com.zhongjh.multimedia.constants.DirType
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

    /**
     * 创建一个缓存路径
     *
     * @param context 上下文
     * @param type 文件类型
     * @return
     */
    fun createCacheFile(context: Context, @MediaType type: Int): File {
        val appContext = context.applicationContext
        return createFile(appContext, type, DirType.CACHE)
    }


    /**
     * 根据参数文件名称，在temp文件夹创建文件
     *
     * @param context 上下文
     * @param fileName 文件名称
     *
     * @return file
     */
    fun createTempFile(context: Context, fileName: String): File {
        val appContext = context.applicationContext
        val externalFilesDir: File? = appContext.getExternalFilesDir("")
        val tempCameraFile = File(externalFilesDir!!.absolutePath, DirType.COMPRESS)
        if (!tempCameraFile.exists()) {
            tempCameraFile.mkdirs()
        }
        return File(tempCameraFile.absolutePath, fileName)
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
        val appContext = context.applicationContext
        val externalFilesDir: File? = appContext.getExternalFilesDir("")
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
     * 在Camera建立一样的文件
     *
     * @param context 上下文
     * @param fileName 文件名称
     * @param type 文件类型
     * 获取输出路径
     */
    fun getOutFile(context: Context, fileName: String, @MediaType type: Int): File {
        val appContext = context.applicationContext
        val rootDir: File?
        val folderDir: File
        if (TextUtils.equals(Environment.MEDIA_MOUNTED, Environment.getExternalStorageState())) {
            rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            folderDir = File(rootDir.absolutePath + File.separator + CAMERA + File.separator)
        } else {
            rootDir = getRootDirFile(appContext, type)
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
     * 获取uri
     */
    fun getUri(context: Context, path: String): Uri {
        val appContext = context.applicationContext
        val authority = appContext.packageName + ".zhongjhProvider"
        return FileProvider.getUriForFile(appContext, authority, File(path))
    }

    /**
     * 创建一个路径
     *
     * @param context 上下文
     * @param mediaType 文件类型
     * @param dirType 文件夹类型
     * @return
     */
    private fun createFile(context: Context,@MediaType mediaType: Int,@DirType dirType: String): File {
        val appContext = context.applicationContext
        val externalFilesDir: File? = appContext.getExternalFilesDir("")
        val dirFile = File(externalFilesDir!!.absolutePath, dirType)
        if (!dirFile.exists()) {
            dirFile.mkdirs()
        }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault()).format(Date())
        val fileName = when (mediaType) {
            TYPE_PICTURE -> "IMAGE_$timeStamp$JPEG"
            TYPE_VIDEO -> "VIDEO_$timeStamp$MP4"
            TYPE_AUDIO -> "AUDIO_$timeStamp$AAC"
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
        val appContext = context.applicationContext
        return when (type) {
            TYPE_PICTURE -> appContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
            else -> appContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES)!!
        }
    }

}
