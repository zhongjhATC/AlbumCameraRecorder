package com.zhongjh.albumcamerarecorder.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import androidx.core.content.FileProvider
import com.zhongjh.albumcamerarecorder.constants.DirType
import com.zhongjh.albumcamerarecorder.constants.MediaType
import com.zhongjh.albumcamerarecorder.constants.MediaType.TYPE_AUDIO
import com.zhongjh.albumcamerarecorder.constants.MediaType.TYPE_PICTURE
import com.zhongjh.albumcamerarecorder.constants.MediaType.TYPE_VIDEO
import com.zhongjh.albumcamerarecorder.settings.CameraSpec
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.utils.DateUtils.getCreateFileName
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

    /**
     * 创建一个缓存路径
     *
     * @param context 上下文
     * @param type 文件类型
     * @return
     */
    fun createCacheFile(context: Context, @MediaType type: Int): File? {
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
    fun createCompressFile(context: Context, path: String): File? {
        val externalFilesDir: File? = context.getExternalFilesDir("")
        externalFilesDir?.let {
            val tempCameraFile = File(externalFilesDir.absolutePath, DirType.COMPRESS)
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
        } ?: let {
            return null
        }
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
    fun createCompressFile(context: Context, localMedia: LocalMedia): File? {
        val externalFilesDir: File? = context.getExternalFilesDir("")
        externalFilesDir?.let {
            val tempCameraFile = File(externalFilesDir.absolutePath, DirType.COMPRESS)
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
        } ?: let {
            return null
        }
    }

    /**
     * @param context 上下文
     * @param cameraSpec 录制配置
     * @param type 文件类型
     * 获取输出路径
     */
    fun getOutFile(context: Context, cameraSpec: CameraSpec, @MediaType type: Int): File? {
        val isSaveExternal = isSaveExternal(cameraSpec.outPutCameraDir)
        val outFile: File? = if (isSaveExternal) {
            // 创建内部文件夹
            createTempFile(context, type)
        } else {
            // 创建自定义路径下的文件夹
            createOutFile(
                context, type, cameraSpec.outPutCameraFileName, cameraSpec.imageFormat, cameraSpec.outPutCameraDir
            )
        }
        return outFile
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
        return FileProvider.getUriForFile(
            context, "com.zhongjh.albumcamerarecorder.AlbumCameraRecorderFileProvider", File(path)
        )
    }

    /**
     * 判断是否外部输出路径
     */
    private fun isSaveExternal(outPutCameraDirStr: String?): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && TextUtils.isEmpty(outPutCameraDirStr)
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
    ): File? {
        val externalFilesDir: File? = context.getExternalFilesDir("")
        externalFilesDir?.let {
            val dirFile = File(externalFilesDir.absolutePath, dirType)
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
        } ?: let {
            return null
        }
    }

    /**
     * 创建文件
     *
     * @param context            上下文
     * @param type               文件类型：图片、视频、音频
     * @param fileName           文件名
     * @param format             文件格式
     * @param outCameraDirectory 输出目录
     * @return 文件
     */
    private fun createOutFile(
        context: Context, @MediaType type: Int, fileName: String?, format: String?, outCameraDirectory: String?
    ): File? {
        val applicationContext: Context = context.applicationContext
        val folderDir: File? = createFolderDir(applicationContext, outCameraDirectory, type)
        if (folderDir?.exists() != true) {
            folderDir?.mkdirs()
        }
        val isOutFileNameEmpty = TextUtils.isEmpty(fileName)
        when (type) {
            TYPE_VIDEO -> {
                val newFileName = if (isOutFileNameEmpty) getCreateFileName(TYPE_VIDEO.toString()) + MP4 else fileName
                return newFileName?.let { File(folderDir, it) }
            }

            TYPE_AUDIO -> {
                val newFileName = if (isOutFileNameEmpty) getCreateFileName(TYPE_AUDIO.toString()) + AAC else fileName
                return newFileName?.let { File(folderDir, it) }
            }

            else -> {
                val suffix = if (TextUtils.isEmpty(format)) JPEG else format
                val newFileName =
                    if (isOutFileNameEmpty) getCreateFileName(TYPE_PICTURE.toString()) + suffix else fileName
                return newFileName?.let { File(folderDir, it) }
            }
        }
    }

    /**
     * 创建文件夹目录
     * @return 文件夹目录 File
     */
    private fun createFolderDir(context: Context, outCameraDirectory: String?, @MediaType type: Int): File? {
        var folderDir: File? = null
        outCameraDirectory?.let {
            // 自定义存储路径
            folderDir = File(outCameraDirectory)
            if (folderDir?.parentFile?.exists() == false) {
                folderDir?.parentFile?.mkdirs()
            }
        } ?: let {
            // 外部没有自定义拍照存储路径使用默认
            val rootDir: File?
            if (TextUtils.equals(Environment.MEDIA_MOUNTED, Environment.getExternalStorageState())) {
                rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                var folderStr = ""
                when (type) {
                    TYPE_PICTURE -> folderStr += TYPE_PICTURE.toString()
                    TYPE_VIDEO -> folderStr += TYPE_VIDEO.toString()
                    TYPE_AUDIO -> folderStr += TYPE_AUDIO.toString()
                }
                folderDir = File(rootDir.absolutePath + File.separator + folderStr + File.separator)
            } else {
                rootDir = getRootDirFile(context, type)
                rootDir?.let {
                    folderDir = File(rootDir.absolutePath + File.separator)
                }
            }
            rootDir?.let {
                if (!rootDir.exists()) {
                    rootDir.mkdirs()
                }
            }
        }
        return folderDir
    }

    /**
     * 文件根目录
     *
     * @param context 上下文
     * @param type 文件类型：图片、视频、音频
     * @return 文件根目录
     */
    private fun getRootDirFile(context: Context, type: Int): File? {
        return when (type) {
            TYPE_PICTURE -> context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            else -> context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        }
    }

}
