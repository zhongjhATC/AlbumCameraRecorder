package com.zhongjh.albumcamerarecorder.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import androidx.core.content.FileProvider
import com.zhongjh.albumcamerarecorder.constants.MediaTypes
import com.zhongjh.albumcamerarecorder.constants.MediaTypes.TYPE_AUDIO
import com.zhongjh.albumcamerarecorder.constants.MediaTypes.TYPE_PICTURE
import com.zhongjh.albumcamerarecorder.constants.MediaTypes.TYPE_VIDEO
import com.zhongjh.albumcamerarecorder.settings.CameraSpec
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 专门服务Media的文件工具类
 */
object FileMediaUtil {

    private const val JPEG = ".jpeg"
    private const val MP4 = ".mp4"
    private const val AAC = ".aac"
    private const val TEMP_DIR = "AlbumCameraRecorderTemp"

    private val sf = SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US)

    /**
     * 创建一个缓存路径
     *
     * @param context 上下文
     * @param type 文件类型
     * @return
     */
    fun createCacheFile(context: Context, @MediaTypes type: Int): File? {
        val externalFilesDir: File? = context.getExternalFilesDir("")
        externalFilesDir?.let {
            val tempCameraFile = File(externalFilesDir.absolutePath, ".AlbumCameraRecorderCache")
            if (!tempCameraFile.exists()) {
                tempCameraFile.mkdirs()
            }
            var fileName: String = System.currentTimeMillis().toString()
            when (type) {
                TYPE_PICTURE -> fileName += JPEG
                TYPE_VIDEO -> fileName += MP4
                TYPE_AUDIO -> fileName += AAC
            }
            return File(tempCameraFile.absolutePath, fileName)
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
    fun getOutFile(context: Context, cameraSpec: CameraSpec, @MediaTypes type: Int): File? {
        val isSaveExternal = isSaveExternal(cameraSpec.outPutCameraDir)
        val outFile: File? =
            if (isSaveExternal) {
                // 创建内部文件夹
                createTempFile(context, type)
            } else {
                // 创建自定义路径下的文件夹
                createOutFile(
                    context,
                    type,
                    cameraSpec.outPutCameraFileName,
                    cameraSpec.imageFormat,
                    cameraSpec.outPutCameraDir
                )
            }
        return outFile
    }

    /**
     * 获取文件夹
     */
    fun getDir(outPutDir: String?): String {
        return outPutDir ?: TEMP_DIR
    }

    /**
     * 获取uri
     */
    fun getUri(context: Context, outPutCameraDir: String): Uri {
        return FileProvider.getUriForFile(
            context,
            "com.zhongjh.albumcamerarecorder.AlbumCameraRecorderFileProvider",
            File(outPutCameraDir)
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
    private fun createTempFile(context: Context, @MediaTypes type: Int): File? {
        val externalFilesDir: File? = context.getExternalFilesDir("")
        externalFilesDir?.let {
            val tempCameraFile = File(externalFilesDir.absolutePath, TEMP_DIR)
            if (!tempCameraFile.exists()) {
                tempCameraFile.mkdirs()
            }
            var fileName: String = System.currentTimeMillis().toString()
            when (type) {
                TYPE_PICTURE -> fileName += JPEG
                TYPE_VIDEO -> fileName += MP4
                TYPE_AUDIO -> fileName += AAC
            }
            return File(tempCameraFile.absolutePath, fileName)
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
        context: Context,
        @MediaTypes type: Int,
        fileName: String?,
        format: String?,
        outCameraDirectory: String?
    ): File? {
        val applicationContext: Context = context.applicationContext
        val folderDir: File? = createFolderDir(applicationContext, outCameraDirectory, type)
        if (folderDir?.exists() != true) {
            folderDir?.mkdirs()
        }
        val isOutFileNameEmpty = TextUtils.isEmpty(fileName)
        when (type) {
            TYPE_VIDEO -> {
                val newFileName =
                    if (isOutFileNameEmpty) getCreateFileName(TYPE_VIDEO.toString()) + MP4 else fileName
                return newFileName?.let { File(folderDir, it) }
            }

            TYPE_AUDIO -> {
                val newFileName =
                    if (isOutFileNameEmpty) getCreateFileName(TYPE_AUDIO.toString()) + AAC else fileName
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
    private fun createFolderDir(context: Context, outCameraDirectory: String?, @MediaTypes type: Int): File? {
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

    /**
     * 根据时间戳创建文件名
     *
     * @param prefix 前缀名
     * @return
     */
    private fun getCreateFileName(prefix: String): String {
        val millis = System.currentTimeMillis()
        return prefix + sf.format(millis)
    }

}
