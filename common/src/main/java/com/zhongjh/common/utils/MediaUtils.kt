package com.zhongjh.common.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import androidx.exifinterface.media.ExifInterface
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.entity.MediaExtraInfo
import com.zhongjh.common.enums.MediaType
import com.zhongjh.common.enums.MimeType
import com.zhongjh.common.enums.MimeType.Companion.isContent
import java.io.InputStream

/**
 * 多媒体的工具类，获取宽高、视频音频长度等
 *
 * @author zhongjh
 * @date 2022/2/8
 */
object MediaUtils {

    private val TAG: String = this@MediaUtils.javaClass.simpleName

    /**
     * 90度
     */
    private const val ORIENTATION_ROTATE_90 = "90"

    /**
     * 270度
     */
    private const val ORIENTATION_ROTATE_270 = "270"

    /**
     * 获取视频的宽高、时长
     *
     * @param context 上下文
     * @param path    path
     * @return 根据视频path获取相关参数
     */
    @JvmStatic
    fun getVideoSize(context: Context, path: String): MediaExtraInfo {
        val mediaExtraInfo = MediaExtraInfo()
        val retriever = MediaMetadataRetriever()
        try {
            if (MimeType.isContent(path)) {
                retriever.setDataSource(context, Uri.parse(path))
            } else {
                retriever.setDataSource(path)
            }
            val orientation =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
            val width: Int
            val height: Int
            if (TextUtils.equals(ORIENTATION_ROTATE_90, orientation) || TextUtils.equals(
                    ORIENTATION_ROTATE_270,
                    orientation
                )
            ) {
                height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                    ?.toInt() ?: 0
                width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                    ?.toInt() ?: 0
            } else {
                width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                    ?.toInt() ?: 0
                height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                    ?.toInt() ?: 0
            }
            mediaExtraInfo.width = width
            mediaExtraInfo.height = height
            mediaExtraInfo.duration =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLong() ?: 0
            mediaExtraInfo.mimeType =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            retriever.release()
        }
        return mediaExtraInfo
    }

    /**
     * 获取音频的时长
     *
     * @param context 上下文
     * @param path    path
     * @return 根据音频path获取相关参数
     */
    fun getAudioSize(context: Context, path: String): MediaExtraInfo {
        val mediaExtraInfo = MediaExtraInfo()
        val retriever = MediaMetadataRetriever()
        try {
            if (MimeType.isContent(path)) {
                retriever.setDataSource(context, Uri.parse(path))
            } else {
                retriever.setDataSource(path)
            }
            mediaExtraInfo.duration =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLong() ?: 0
            mediaExtraInfo.mimeType =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            retriever.release()
        }
        return mediaExtraInfo
    }

    /**
     * 获取图片的宽和高度
     *
     * @param pathName 图片文件地址
     * @return 宽高合成的数组
     */
    fun getImageWidthAndHeight(pathName: String): IntArray {
        val opts = BitmapFactory.Options()
        // 只请求图片宽高，不解析图片像素(请求图片属性但不申请内存，解析bitmap对象，该对象不占内存)
        opts.inJustDecodeBounds = true
        BitmapFactory.decodeFile(pathName, opts)
        return intArrayOf(opts.outWidth, opts.outHeight)
    }

    /**
     * 判断是否长图，当
     * @param width 宽
     * @param height 高
     */
    fun isLongImage(width: Int, height: Int): Boolean {
        return if (width <= 0 || height <= 0) {
            false
        } else {
            height > width * 3
        }
    }

    /**
     * 获取文件信息,视频大的话会耗时过大
     */
    fun getMediaInfo(context: Context, @MediaType mediaType: Int, path: String): LocalMedia {
        val localMedia = LocalMedia()
        // 如果是图片
        var inputStream: InputStream? = null

        when (mediaType) {
            MediaType.TYPE_PICTURE -> {
                // 实例化ExifInterface,作用获取图片的属性
                var exif: ExifInterface? = null
                if (isContent(path)) {
                    inputStream = context.contentResolver.openInputStream(Uri.parse(path))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && inputStream != null) {
                        exif = ExifInterface(inputStream)
                    }
                } else {
                    exif = ExifInterface(path)
                }
                // 开始获取图片的相关属性
                exif?.apply {
                    // 获取方向
                    val orientation = this.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    // 获取宽高
                    val width =
                        this.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0)
                    val height =
                        this.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0)

                    // 判断如果是非正常角度的，图片属性取相反的宽高
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90
                        || orientation == ExifInterface.ORIENTATION_ROTATE_180
                        || orientation == ExifInterface.ORIENTATION_ROTATE_270
                        || orientation == ExifInterface.ORIENTATION_TRANSVERSE
                    ) {
                        localMedia.width = height
                        localMedia.height = width
                    } else {
                        localMedia.width = width
                        localMedia.height = height
                    }
                }
                inputStream?.apply {
                    FileUtils.close(this)
                }
            }

            MediaType.TYPE_VIDEO -> {
                // 实例化MediaMetadataRetriever,作用获取视频的属性
                val retriever = MediaMetadataRetriever()
                if (isContent(path)) {
                    retriever.setDataSource(context, Uri.parse(path))
                } else {
                    retriever.setDataSource(path)
                }
                // 获取视频的时长、角度
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLong()?.let { duration ->
                        localMedia.duration = duration
                    }
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                    ?.toInt()?.let { orientation ->
                        localMedia.orientation = orientation
                    }

                // 判断如果是非正常角度的，视频属性取相反的宽高
                if (localMedia.orientation == 90 || localMedia.orientation == 270) {
                    retriever.extractMetadata(
                        MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH
                    )?.toInt()?.let { width ->
                        localMedia.height = width
                    }
                    retriever.extractMetadata(
                        MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT
                    )?.toInt()?.let { height ->
                        localMedia.width = height
                    }
                } else {
                    retriever.extractMetadata(
                        MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH
                    )?.toInt()?.let { width ->
                        localMedia.width = width
                    }
                    retriever.extractMetadata(
                        MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT
                    )?.toInt()?.let { height ->
                        localMedia.height = height
                    }
                }
                retriever.release()
            }

            MediaType.TYPE_AUDIO -> {
            }
        }
        return localMedia
    }


}