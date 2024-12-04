package com.zhongjh.albumcamerarecorder.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.FileUtils
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.exifinterface.media.ExifInterface
import com.zhongjh.albumcamerarecorder.album.loader.MediaLoader
import com.zhongjh.albumcamerarecorder.album.loader.MediaLoader.Companion.QUERY_URI
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.enums.MediaType
import com.zhongjh.common.enums.MimeType
import com.zhongjh.common.utils.AppUtils.getAppName
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
    private const val DCIM_CAMERA: String = "DCIM/Camera"

    /**
     * 插入图片、视频到图库
     * 兼容AndroidQ
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    @JvmStatic
    fun displayToGalleryAndroidQ(
        context: Context, file: File, @MediaType type: Int,
        duration: Long, width: Int, height: Int
    ): Uri? {
        // 插入file数据到相册
        val values = ContentValues()
        values.put(MediaStore.MediaColumns.TITLE, getAppName(context))
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
        values.put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis())
        values.put(MediaStore.MediaColumns.ORIENTATION, 0)
        values.put(MediaStore.MediaColumns.SIZE, file.length())
        values.put(MediaStore.MediaColumns.WIDTH, width)
        values.put(MediaStore.MediaColumns.HEIGHT, height)
        val suffix = file.name.substring(file.name.lastIndexOf("."))
        var external: Uri? = null
        when (type) {
            MediaType.TYPE_VIDEO -> {
                external = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                values.put(MediaStore.Video.Media.MIME_TYPE, MimeType.getMimeType(suffix))
                values.put(MediaStore.Video.Media.RELATIVE_PATH, DCIM_CAMERA)
                // 计算时间
                if (duration == 0L) {
                    val photoPath = file.path
                    val uri = FileMediaUtil.getUri(context, photoPath)
                    val mp = MediaPlayer.create(context, uri)
                    values.put("duration", mp.duration.toLong())
                    mp.release()
                } else {
                    values.put("duration", duration)
                }
            }

            MediaType.TYPE_PICTURE -> {
                external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                values.put(MediaStore.Images.Media.MIME_TYPE, MimeType.getMimeType(suffix))
                values.put(MediaStore.Images.Media.RELATIVE_PATH, DCIM_CAMERA)

                // 需要增加这个，不然AndroidQ识别不到TAG_DATETIME_ORIGINAL创建时间
                try {
                    val exif = ExifInterface(file.path)
                    if (TextUtils.isEmpty(exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL))) {
                        val simpleDateFormat = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault())
                        exif.setAttribute(
                            ExifInterface.TAG_DATETIME_ORIGINAL,
                            simpleDateFormat.format(System.currentTimeMillis())
                        )
                        exif.saveAttributes()
                    }
                } catch (e: IOException) {
                    Log.d(TAG, e.message.toString())
                    e.printStackTrace()
                }
            }
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(external!!, values)
        values.clear()
        uri?.let {
            val out = resolver.openOutputStream(uri)
            val fis = FileInputStream(file)
            out?.let {
                FileUtils.copy(fis, out)
                fis.close()
                out.close()
            }
        }
        return uri
    }

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
        context: Context, file: File, @MediaType type: Int,
        duration: Long, width: Int, height: Int,
        directory: String
    ): Uri? {
        Log.d("displayToGallery", file.path)
        if (!file.exists()) {
            return null
        }
        var uri: Uri?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uri = displayToGalleryAndroidQ(context, file, type, duration, width, height, directory)
        } else {
            val photoPath = file.path
            uri = FileMediaUtil.getUri(context, photoPath)
            // 添加到图库数据库
            val values = ContentValues()
            values.put(MediaStore.Images.Media.DATA, photoPath)
            values.put(MediaStore.Images.Media.TITLE, getAppName(context))
            values.put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
            values.put(MediaStore.Images.Media.SIZE, file.length())
            values.put(MediaStore.Images.Media.WIDTH, width)
            values.put(MediaStore.Images.Media.HEIGHT, height)
            val suffix = file.name.substring(file.name.lastIndexOf("."))
            when (type) {
                MediaType.TYPE_VIDEO -> {
                    values.put(MediaStore.Images.Media.MIME_TYPE, MimeType.getMimeType(suffix))
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

                MediaType.TYPE_PICTURE -> {
                    values.put(MediaStore.Images.Media.MIME_TYPE, MimeType.getMimeType(suffix))
                    uri = context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                    )
                }

                MediaType.TYPE_AUDIO -> {
                    values.put(MediaStore.Audio.Media.MIME_TYPE, MimeType.getMimeType(suffix))
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
        context: Context, file: File, @MediaType type: Int,
        duration: Long, width: Int, height: Int, directory: String
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
        val suffix = file.name.substring(file.name.lastIndexOf("."))
        var external: Uri? = null
        when (type) {
            MediaType.TYPE_VIDEO -> {
                values.put(MediaStore.Video.Media.MIME_TYPE, MimeType.getMimeType(suffix))
                // 计算时间
                if (duration == 0L) {
                    val photoPath = file.path
                    val uri = FileMediaUtil.getUri(context, photoPath)
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

            MediaType.TYPE_PICTURE -> {
                values.put(MediaStore.Images.Media.MIME_TYPE, MimeType.getMimeType(suffix))
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

            MediaType.TYPE_AUDIO -> {}
            else -> {}
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

    /**
     * AndroidQ包含及以上
     * 根据uri获取相册数据
     * @param context 上下文
     * @param uri 文件路径
     * @return localMedia 查询出的数据
     */
    fun getMediaDataByUri(context: Context, uri: Uri): LocalMedia {
        val id = getId(uri)
        val cursor: Cursor? = context.contentResolver.query(
            QUERY_URI,
            MediaLoader.PROJECTION,
            MediaStore.Files.FileColumns._ID + "=?",
            arrayOf(id.toString()),
            null
        )
        if (cursor != null && cursor.moveToFirst()) {
            val mediaLoader = MediaLoader(context)
            return mediaLoader.parse(cursor)
        }
        return LocalMedia()
    }


    /**
     * AndroidQ包含及以上
     * 根据uri获取相册数据
     * @param context 上下文
     * @param path 文件路径
     * @return localMedia 查询出的数据
     */
    fun getMediaDataByPath(context: Context, path: String): LocalMedia {
        Log.d(TAG, "path:$path")
        val cursor: Cursor? = context.contentResolver.query(
            QUERY_URI,
            MediaLoader.PROJECTION,
            MediaStore.Images.Media.DATA + "=?",
            arrayOf(path),
            null
        )
        Log.d(TAG, "cursor:${cursor?.columnNames?.size}" + " cursor.count:${cursor?.count}")
        if (cursor != null && cursor.moveToFirst()) {
            val mediaLoader = MediaLoader(context)
            return mediaLoader.parse(cursor)
        }
        return LocalMedia()
    }


}