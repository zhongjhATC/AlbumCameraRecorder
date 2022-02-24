package com.zhongjh.common.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import androidx.exifinterface.media.ExifInterface
import com.zhongjh.common.entity.MediaExtraInfo
import java.io.IOException
import java.io.InputStream

/**
 * 多媒体的工具类，获取宽高、视频音频长度等
 *
 * @author zhongjh
 * @date 2022/2/8
 */
object MediaUtils {

    /**
     * 90度
     */
    private const val ORIENTATION_ROTATE_90 = "90"

    /**
     * 270度
     */
    private const val ORIENTATION_ROTATE_270 = "270"

    /**
     * 获取图片的宽高
     *
     * @param context 上下文
     * @param path    path
     * @return 根据图片path获取相关参数
     */
    fun getImageSize(context: Context, path: String): MediaExtraInfo {
        val mediaExtraInfo = MediaExtraInfo()
        val exifInterface: ExifInterface
        var inputStream: InputStream? = null
        try {
            if (MimeTypeUtils.isContent(path)) {
                inputStream = context.contentResolver.openInputStream(Uri.parse(path))
                exifInterface = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    ExifInterface(inputStream!!)
                } else {
                    ExifInterface(path)
                }
            } else {
                exifInterface = ExifInterface(path)
            }
            mediaExtraInfo.width = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, ExifInterface.ORIENTATION_NORMAL)
            mediaExtraInfo.height = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, ExifInterface.ORIENTATION_NORMAL)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return mediaExtraInfo
    }

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
            if (MimeTypeUtils.isContent(path)) {
                retriever.setDataSource(context, Uri.parse(path))
            } else {
                retriever.setDataSource(path)
            }
            val orientation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
            val width: Int
            val height: Int
            if (TextUtils.equals(ORIENTATION_ROTATE_90, orientation) || TextUtils.equals(ORIENTATION_ROTATE_270, orientation)) {
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
            mediaExtraInfo.duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLong() ?: 0
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
            if (MimeTypeUtils.isContent(path)) {
                retriever.setDataSource(context, Uri.parse(path))
            } else {
                retriever.setDataSource(path)
            }
            mediaExtraInfo.duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLong() ?: 0
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
}