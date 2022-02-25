package com.zhongjh.albumcamerarecorder.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.FileUtils
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import androidx.annotation.IntDef
import androidx.annotation.RequiresApi
import androidx.exifinterface.media.ExifInterface
import com.zhongjh.common.utils.AppUtils.getAppName
import com.zhongjh.common.utils.MediaStoreCompat
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 相册操作常用工具类
 *
 * @author Clock
 * @author zhongjh
 * @date 2015/12/31
 * @date 2022/01/05
 */
object MediaStoreUtils {

    private val TAG = MediaStoreUtils::class.java.simpleName

    /**
     * 插入图片、视频到图库
     *
     * @param context          上下文
     * @param file             要保存的文件
     * @param type             mp4 jpeg
     * @param duration         video专属的时长,图片传-1即可
     * @param width            宽
     * @param height           高
     * @param directory        子文件目录
     * @param mediaStoreCompat mediaStoreCompat
     */
    @JvmStatic
    fun displayToGallery(
        context: Context, file: File, @MediaTypes type: Int,
        duration: Long, width: Int, height: Int,
        directory: String, mediaStoreCompat: MediaStoreCompat
    ): Uri? {
        Log.d("displayToGallery", file.path)
        if (!file.exists()) {
            return null
        }
        var uri: Uri?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uri = displayToGalleryAndroidQ(
                context, file, type, duration, width, height, directory, mediaStoreCompat
            )
        } else {
            val photoPath = file.path
            uri = mediaStoreCompat.getUri(photoPath)
            // 添加到图库数据库
            val values = ContentValues()
            values.put(MediaStore.Images.Media.DATA, photoPath)
            values.put(MediaStore.Images.Media.TITLE, getAppName(context))
            values.put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
            values.put(MediaStore.Images.Media.SIZE, file.length())
            values.put(MediaStore.Images.Media.WIDTH, width)
            values.put(MediaStore.Images.Media.HEIGHT, height)
            when (type) {
                MediaTypes.TYPE_VIDEO -> {
                    values.put(MediaStore.Images.Media.MIME_TYPE, "video/mp4")
                    // 计算时间
                    if (duration == 0L) {
                        val mp = MediaPlayer.create(context, uri)
                        values.put("duration", mp.duration.toLong())
                        mp.release()
                    } else {
                        values.put("duration", duration)
                    }
                    uri = context.contentResolver.insert(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values
                    )
                }
                MediaTypes.TYPE_PICTURE -> {
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    uri = context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                    )
                }
                MediaTypes.TYPE_AUDIO -> {
                    values.put(MediaStore.Audio.Media.MIME_TYPE, "video/aac")
                    // 计算时间
                    if (duration == 0L) {
                        val mp = MediaPlayer.create(context, uri)
                        values.put("duration", mp.duration.toLong())
                        mp.release()
                    } else {
                        values.put("duration", duration)
                    }
                    uri = context.contentResolver.insert(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values
                    )
                }
                else -> {
                }
            }
            // 这个判断AndroidQ的就是用来解决ACTION_MEDIA_SCANNER_SCAN_FILE过时的方式
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
            values.clear()
        }
        return uri
    }

    /**
     * 插入图片、视频到图库
     * 兼容AndroidQ
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun displayToGalleryAndroidQ(
        context: Context, file: File, @MediaTypes type: Int,
        duration: Long, width: Int, height: Int,
        directory: String, mediaStoreCompat: MediaStoreCompat
    ): Uri? {
        // 插入file数据到相册
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, getAppName(context))
        values.put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        values.put(MediaStore.Images.Media.ORIENTATION, 0)
        values.put(MediaStore.Images.Media.SIZE, file.length())
        values.put(MediaStore.Images.Media.WIDTH, width)
        values.put(MediaStore.Images.Media.HEIGHT, height)
        var external: Uri? = null
        when (type) {
            MediaTypes.TYPE_VIDEO -> {
                values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                // 计算时间
                if (duration == 0L) {
                    val photoPath = file.path
                    val uri = mediaStoreCompat.getUri(photoPath)
                    val mp = MediaPlayer.create(context, uri)
                    values.put("duration", mp.duration.toLong())
                    mp.release()
                } else {
                    values.put("duration", duration)
                }
                values.put(
                    MediaStore.Video.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_MOVIES + File.separator + directory
                )
                external = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
            MediaTypes.TYPE_PICTURE -> {
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                values.put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + File.separator + directory
                )
                external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

                // 需要增加这个，不然AndroidQ识别不到TAG_DATETIME_ORIGINAL创建时间
                try {
                    val exif = ExifInterface(file.path)
                    if (TextUtils.isEmpty(exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL))) {
                        val simpleDateFormat =
                            SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault())
                        exif.setAttribute(
                            ExifInterface.TAG_DATETIME_ORIGINAL, simpleDateFormat.format(
                                System.currentTimeMillis()
                            )
                        )
                        exif.saveAttributes()
                    }
                } catch (e: IOException) {
                    Log.d(TAG, e.message.toString())
                    e.printStackTrace()
                }
            }
            MediaTypes.TYPE_AUDIO -> {
            }
            else -> {
            }
        }
        val resolver = context.contentResolver
        if (external == null) {
            return external
        }
        val uri = resolver.insert(external, values)
        values.clear()
        try {
            val out = resolver.openOutputStream(uri!!)
            val fis = FileInputStream(file)
            FileUtils.copy(fis, out!!)
            fis.close()
            out.close()
        } catch (e: IOException) {
            return null
        }
        return uri
    }

    /**
     * 根据uri获取里面的id
     *
     * @param uri uri
     * @return id
     */
    @JvmStatic
    fun getId(uri: Uri?): Long {
        if (uri == null) {
            return 0L
        }
        // 加入相册后的最后是id，直接使用该id
        val uriPath = uri.path
        return try {
            uriPath!!.substring(uriPath.lastIndexOf("/") + 1).toLong()
        } catch (exception: Exception) {
            0L
        }
    }

    @IntDef(MediaTypes.TYPE_PICTURE, MediaTypes.TYPE_VIDEO, MediaTypes.TYPE_AUDIO)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class MediaTypes {
        companion object {
            /**
             * 图片
             */
            const val TYPE_PICTURE = 0x001

            /**
             * 视频
             */
            const val TYPE_VIDEO = 0x002

            /**
             * 音频
             */
            const val TYPE_AUDIO = 0x003
        }
    }
}