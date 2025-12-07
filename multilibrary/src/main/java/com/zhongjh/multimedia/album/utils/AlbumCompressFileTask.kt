package com.zhongjh.multimedia.album.utils

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.net.toUri
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.listener.VideoEditListener
import com.zhongjh.common.utils.FileUtils
import com.zhongjh.multimedia.settings.GlobalSpec
import com.zhongjh.multimedia.utils.FileMediaUtil
import java.io.File
import java.lang.ref.WeakReference

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
    context: Context,
    private val tag: String,
    private val clsKey: Class<*>,
    private val globalSpec: GlobalSpec
) {

    // 用弱引用持有Context，避免内存泄漏
    private val contextRef = WeakReference(context.applicationContext)

    /**
     * @param localFiles 数据源
     * @param isOnlyCompressEditPicture 是否只压缩编辑的图片
     */
    fun compressFileTaskDoInBackground(
        localFiles: ArrayList<LocalMedia>,
        isOnlyCompressEditPicture: Boolean
    ): ArrayList<LocalMedia> {
        // 将 缓存文件 拷贝到 配置目录
        val newLocalFiles = ArrayList<LocalMedia>()
        // 检查Context是否已释放
        val context = contextRef.get() ?: run {
            return newLocalFiles
        }
        for (item in localFiles) {
            // 如果有编辑的图片,则压缩编辑的图片,否则压缩原图
            val absolutePath = item.editorPath ?: this.prepareCompressFile(
                context,
                item.uri,
                File(item.absolutePath)
            ).absolutePath

            val isCompressItem = isCompress(item, isOnlyCompressEditPicture)
            if (isCompressItem != null) {
                newLocalFiles.add(isCompressItem)
                continue
            }

            // 开始压缩逻辑，获取真实路径
            val newFileNames = absolutePath.split(".").toTypedArray()
            // 设置压缩后的照片名称，id_CMP
            var newFileName = item.fileId.toString() + "_CMP"
            if (newFileNames.size > 1) {
                // 设置后缀名
                newFileName = newFileName + "." + newFileNames[newFileNames.size - 1]
            }
            val newFile = FileMediaUtil.createTempFile(context, newFileName)
            if (newFile.exists()) {
                val localFile = LocalMedia(context, item, newFile, true)
                newLocalFiles.add(localFile)
                Log.d(tag, "存在直接使用")
            } else {
                if (item.isImage()) {
                    // 处理是否压缩图片
                    val compressionFile = handleImage(absolutePath)
                    // 移动到新的文件夹
                    FileUtils.copy(compressionFile, newFile)
                    newLocalFiles.add(LocalMedia(context, item, newFile, true))
                    Log.d(tag, "不存在新建文件")
                } else if (item.isVideo()) {
                    // 压缩视频
                    globalSpec.videoCompressCoordinator?.let { videoCompressCoordinator ->
                        videoCompressCoordinator.setVideoCompressListener(
                            clsKey,
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
                        videoCompressCoordinator.compressAsync(clsKey, absolutePath, newFile.path)
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
        // 检查Context是否已释放
        val context = contextRef.get() ?: run {
            return oldFile
        }
        // 根据类型压缩
        val compressionFile: File =
            globalSpec.onImageCompressionListener?.compressionFile(context, oldFile) ?: let {
                oldFile
            }
        return compressionFile
    }

    /**
     * 判断是否需要压缩
     *
     * @param item 数据
     * @param isOnlyCompressEditPicture 是否只压缩编辑的图片
     *
     * @return 返回对象为null就需要压缩，否则不需要压缩
     */
    private fun isCompress(item: LocalMedia, isOnlyCompressEditPicture: Boolean): LocalMedia? {
        // 判断是否需要压缩
        return if (item.isVideo() && globalSpec.videoCompressCoordinator == null) {
            item
        } else if (item.isGif()) {
            item
        } else if (item.isImage() && globalSpec.onImageCompressionListener == null) {
            if (isOnlyCompressEditPicture) {
                if (isOnlyCompressEditPicture(item)) {
                    item
                } else {
                    null
                }
            } else {
                item
            }
        } else {
            null
        }
    }

    /**
     * 编辑图片地址有值 同时 压缩地址没值，才需要进行压缩
     */
    private fun isOnlyCompressEditPicture(item: LocalMedia): Boolean {
        return null != item.editorPath && null == item.compressPath
    }

    /**
     * 检查文件是否可直接操作，不可则复制到应用私有目录（安全区域）
     * @param context 上下文
     * @param sourceFile 原始文件（外部存储公共目录）
     * @return 安全区域的文件
     */
    fun prepareCompressFile(context: Context, uriStr: String, sourceFile: File): File {
        // 1. 低版本（API < 29）：直接返回原文件
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return sourceFile
        }

        // 目标文件（私有目录下同名文件）
        val newFile = FileMediaUtil.createTempAPI29File(context, sourceFile.name)
        val uri = uriStr.toUri()
        // 移动到新的文件夹
        FileUtils.copy(context, uri, newFile)
        return newFile
    }
}