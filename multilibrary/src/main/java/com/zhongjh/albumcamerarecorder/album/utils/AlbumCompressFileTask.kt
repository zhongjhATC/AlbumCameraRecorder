package com.zhongjh.albumcamerarecorder.album.utils

import android.content.Context
import android.util.Log
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec
import com.zhongjh.albumcamerarecorder.utils.FileMediaUtil
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.listener.VideoEditListener
import com.zhongjh.common.utils.FileUtils
import java.io.File
import java.util.*

/**
 * 这是相册界面和预览界面共用的一个异步线程逻辑
 *
 * 确定当前选择的文件列表，根据是否压缩配置决定重新返回新的文件列表
 *
 * @param globalSpec 公共配置
 *
 * @author zhongjh
 * @date 2022/2/9
 */
class AlbumCompressFileTask(
    private val context: Context,
    private val tag: String,
    private val clsKey: Class<*>,
    private val globalSpec: GlobalSpec
) {
    fun compressFileTaskDoInBackground(localFiles: ArrayList<LocalMedia>): ArrayList<LocalMedia> {
        // 将 缓存文件 拷贝到 配置目录
        val newLocalFiles = ArrayList<LocalMedia>()
        for (item in localFiles) {
            // 设置沙盒路径
            item.sandboxPath = FileMediaUtil.getUri(context, item.path).toString()

            // 判断是否需要压缩
            val isCompressItem = isCompress(item)
            if (isCompressItem != null) {
                newLocalFiles.add(isCompressItem)
                continue
            }

            // 开始压缩逻辑，获取真实路径
            val newFileNames = item.absolutePath.split(".").toTypedArray()
            // 设置压缩后的照片名称，id_CMP
            var newFileName = item.id.toString() + "_CMP"
            if (newFileNames.size > 1) {
                // 设置后缀名
                newFileName = newFileName + "." + newFileNames[newFileNames.size - 1]
            }
            val newFile = FileMediaUtil.createTempFile(context, newFileName)
            if (newFile.exists()) {
                val localFile: LocalMedia = if (item.isImage()) {
                    LocalMedia(context, item, newFile, true)
                } else {
                    LocalMedia(context, item, newFile, true)
                }
                newLocalFiles.add(localFile)
                Log.d(tag, "存在直接使用")
            } else {
                if (item.isImage()) {
                    // 处理是否压缩图片
                    val compressionFile = handleImage(item.absolutePath)
                    // 移动到新的文件夹
                    FileUtils.copy(compressionFile, newFile)
                    newLocalFiles.add(
                        LocalMedia(context, item, newFile, true)
                    )
                    Log.d(tag, "不存在新建文件")
                } else if (item.isVideo()) {
                    if (globalSpec.isCompressEnable) {
                        // 压缩视频
                        globalSpec.videoCompressCoordinator?.setVideoCompressListener(clsKey,
                            object : VideoEditListener {
                                override fun onFinish() {
                                    val localFile = LocalMedia(
                                        context, item, newFile, true
                                    )
                                    newLocalFiles.add(localFile)
                                    Log.d(tag, "不存在新建文件")
                                }

                                override fun onProgress(progress: Int, progressTime: Long) {}
                                override fun onCancel() {}
                                override fun onError(message: String) {}
                            })
                        globalSpec.videoCompressCoordinator?.compressAsync(
                            clsKey, item.absolutePath, newFile.path
                        )
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
    private fun handleImage(path: String): File {
        val oldFile = File(path)
        // 根据类型压缩
        val compressionFile: File = if (globalSpec.onImageCompressionListener != null) {
            // 压缩图片
            globalSpec.onImageCompressionListener!!.compressionFile(context, oldFile)
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
    private fun isCompress(item: LocalMedia): LocalMedia? {
        // 判断是否需要压缩
        return if (item.isVideo() && globalSpec.videoCompressCoordinator == null) {
            item
        } else if (item.isGif()) {
            item
        } else if (item.isImage() && globalSpec.onImageCompressionListener == null) {
            item
        } else {
            null
        }
    }

    /**
     * 返回迁移后的file的名称
     *
     * @param item 当前处理的LocalFile
     * @param path 真实路径
     * @return 返回迁移后的file的名称
     */
    private fun getNewFileName(item: LocalMedia, path: String): String {
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
}