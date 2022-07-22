package com.zhongjh.albumcamerarecorder.album.utils

import android.content.Context
import android.os.Build
import android.util.Log
import com.zhongjh.albumcamerarecorder.camera.util.FileUtil
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec
import com.zhongjh.common.entity.LocalFile
import com.zhongjh.common.listener.VideoEditListener
import com.zhongjh.common.utils.MediaStoreCompat
import com.zhongjh.common.utils.UriUtils
import java.io.File
import java.io.IOException
import java.util.*

/**
 * 这是相册界面和预览界面共用的一个异步线程逻辑
 * @param globalSpec 公共配置
 * @param pictureMediaStoreCompat  图片文件配置路径
 * @param videoMediaStoreCompat 录像文件配置路径
 *
 * @author zhongjh
 * @date 2022/2/9
 */
class AlbumCompressFileTask(
    private val context: Context, private val tag: String, private val clsKey: Class<*>,
    private val globalSpec: GlobalSpec,
    private val pictureMediaStoreCompat: MediaStoreCompat,
    private val videoMediaStoreCompat: MediaStoreCompat
) {
    fun compressFileTaskDoInBackground(localFiles: ArrayList<LocalFile>): ArrayList<LocalFile> {
        // 将 缓存文件 拷贝到 配置目录
        val newLocalFiles = ArrayList<LocalFile>()
        for (item in localFiles) {
            // 判断是否需要压缩
            val isCompressItem = isCompress(item)
            if (isCompressItem != null) {
                newLocalFiles.add(isCompressItem)
                continue
            }

            // 开始压缩逻辑，获取真实路径
            val path = getPath(item)
            if (path != null) {
                val newFileName = getNewFileName(item, path)
                val newFile = getNewFile(item, path, newFileName)
                if (newFile.exists()) {
                    val localFile: LocalFile =
                        if (item.isImage()) {
                            LocalFile(context, pictureMediaStoreCompat, item, newFile, true)
                        } else {
                            LocalFile(context, videoMediaStoreCompat, item, newFile, true)
                        }
                    newLocalFiles.add(localFile)
                    Log.d(tag, "存在直接使用")
                } else {
                    if (item.isImage()) {
                        // 处理是否压缩图片
                        val compressionFile = handleImage(path)
                        // 移动到新的文件夹
                        FileUtil.copy(compressionFile, newFile)
                        newLocalFiles.add(
                            LocalFile(
                                context,
                                pictureMediaStoreCompat,
                                item,
                                newFile,
                                true
                            )
                        )
                        Log.d(tag, "不存在新建文件")
                    } else if (item.isVideo()) {
                        if (globalSpec.isCompressEnable) {
                            // 压缩视频
                            globalSpec.videoCompressCoordinator?.setVideoCompressListener(
                                clsKey,
                                object : VideoEditListener {
                                    override fun onFinish() {
                                        val localFile = LocalFile(
                                            context,
                                            videoMediaStoreCompat,
                                            item,
                                            newFile,
                                            true
                                        )
                                        newLocalFiles.add(localFile)
                                        Log.d(tag, "不存在新建文件")
                                    }

                                    override fun onProgress(progress: Int, progressTime: Long) {}
                                    override fun onCancel() {}
                                    override fun onError(message: String) {}
                                })
                            globalSpec.videoCompressCoordinator?.compressAsync(
                                clsKey,
                                path,
                                newFile.path
                            )
                        }
                    }
                }
            }
        }
        return newLocalFiles
    }

    /**
     * 处理图片
     *
     * @param path 图片真实路径
     * @return 压缩后的文件
     */
    fun handleImage(path: String): File {
        val oldFile = File(path)
        // 根据类型压缩
        val compressionFile: File =
            if (globalSpec.imageCompressionInterface != null) {
                // 压缩图片
                globalSpec.imageCompressionInterface!!.compressionFile(context, oldFile)
            } else {
                oldFile
            }
        return compressionFile
    }

    /**
     * 判断是否需要压缩
     *
     * @return 返回对象为null就需要压缩，否则不需要压缩
     */
    fun isCompress(item: LocalFile): LocalFile? {
        // 判断是否需要压缩
        return if (item.isVideo() && globalSpec.videoCompressCoordinator == null) {
            item
        } else if (item.isGif()) {
            item
        } else if (item.isImage() && globalSpec.imageCompressionInterface == null) {
            item
        } else {
            null
        }
    }

    /**
     * 返回当前处理的LocalFile的真实路径
     *
     * @param item 当前处理的LocalFile
     * @return 真实路径,有可能因为转不成uri转不成file返回null
     */
    fun getPath(item: LocalFile): String? {
        var path: String? = null
        if (item.path == null) {
            val file = UriUtils.uriToFile(context, item.uri)
            if (file != null) {
                path = file.absolutePath
            }
        } else {
            path = item.path
        }
        if (path != null) {
            // 判断是否Android 29
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // 29以上的版本都必须是私有的或者公共目录
                var cacheFile: File? = null
                if (item.isImage()) {
                    cacheFile = pictureMediaStoreCompat.createFile(0, true, getNameSuffix(path))
                } else if (item.isVideo()) {
                    cacheFile = videoMediaStoreCompat.createFile(1, true, getNameSuffix(path))
                }
                // >=29 的需要通过uri获取公共目录的文件，并且拷贝到私有目录
                if (cacheFile != null) {
                    FileUtil.copy(context, item.uri, cacheFile)
                    path = cacheFile.absolutePath
                }
            }
        }
        return path
    }

    /**
     * 返回迁移后的file的名称
     *
     * @param item 当前处理的LocalFile
     * @param path 真实路径
     * @return 返回迁移后的file的名称
     */
    fun getNewFileName(item: LocalFile, path: String): String {
        // 移动文件,获取文件名称
        var newFileName = path.substring(path.lastIndexOf(File.separator))
        val newFileNames = newFileName.split(".").toTypedArray()
        // 设置压缩后的照片名称，id_CMP
        newFileName = item.id.toString() + "_CMP"
        if (newFileNames.size > 1) {
            // 设置后缀名
            newFileName = newFileName + "." + newFileNames[newFileNames.size - 1]
        }
        return newFileName
    }

    /**
     * @return 获取后缀名
     */
    fun getNameSuffix(path: String): String {
        // 获取文件名称
        val newFileName = path.substring(path.lastIndexOf(File.separator))
        val newFileNames = newFileName.split(".").toTypedArray()
        return if (newFileNames.size > 1) {
            // 返回后缀名
            newFileNames[newFileNames.size - 1]
        } else ""
    }

    /**
     * 返回迁移后的file
     *
     * @param item        当前处理的LocalFile
     * @param path        真实路径
     * @param newFileName 迁移后的file的名称
     * @return 返回迁移后的file
     */
    fun getNewFile(item: LocalFile, path: String, newFileName: String): File {
        val newFile: File = when {
            item.isImage() -> {
                pictureMediaStoreCompat.fineFile(newFileName, 0, false)
            }
            item.isVideo() -> {
                videoMediaStoreCompat.fineFile(newFileName, 1, false)
            }
            else -> {
                File(path)
            }
        }
        return newFile
    }
}