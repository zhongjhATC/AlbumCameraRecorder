package com.zhongjh.multimedia.utils

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
import androidx.annotation.RequiresApi
import androidx.exifinterface.media.ExifInterface
import com.zhongjh.common.enums.MediaType
import com.zhongjh.common.enums.MimeType
import com.zhongjh.common.utils.AppUtils.getAppName
import com.zhongjh.common.utils.LogUtil
import com.zhongjh.common.utils.MediaStoreCompat
import com.zhongjh.common.utils.SdkVersionUtils
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale


/**
 * 相册操作常用工具类
 *
 * @author Clock
 * @author zhongjh
 * @date 2016/12/31
 * @date 2022/01/05
 */
object MediaStoreUtils {

    private val TAG = MediaStoreUtils::class.java.simpleName
    const val DCIM_CAMERA: String = "DCIM/Camera"

    /**
     * 创建相机输出图片Uri（适配分区存储Android 10/Q+）
     * 适配逻辑：
     * 1. Android Q(API29)及以上：使用MediaStore ContentResolver插入媒体记录，返回content://Uri，适配分区存储，无需操作File实体文件
     * 2. Android Q以下：直接创建DCIM/Camera本地物理文件，通过FileProvider生成可对外分享的content://Uri
     * 该Uri用于相机Intent的MediaStore.EXTRA_OUTPUT参数，指定拍摄图片输出位置
     *
     * @param context 上下文
     * @return 相机输出图片content://Uri，创建失败返回null
     */
    fun createCameraOutImageUri(context: Context): Uri? {
        val imageUri: Uri?
        // 生成时间戳唯一图片文件名，避免重名覆盖
        val cameraFileName = "IMAGE_" + SimpleDateFormat(
            "yyyyMMdd_HHmmssSSS", Locale.US
        ).format(System.currentTimeMillis()) + ".jpg"
        if (SdkVersionUtils.isQ) {
            // Android 10+ 分区存储，通过MediaStore插入媒体记录获取Uri
            imageUri = createImageUri(context, cameraFileName)
        } else {
            // 低版本，直接创建DCIM/Camera本地文件，再通过FileProvider转换Uri
            val dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            val cameraDir = File(dcim, "Camera")
            if (!cameraDir.exists()) cameraDir.mkdirs()
            imageUri = MediaStoreCompat.getUri(context, File(cameraDir, cameraFileName).path)
        }
        return imageUri
    }

    /**
     * 创建录像输出视频Uri（适配分区存储Android 10/Q+）
     * 适配逻辑：
     * 1. Android Q(API29)及以上：使用MediaStore ContentResolver插入视频媒体记录，返回content://Uri，适配分区存储规范
     * 2. Android Q以下：直接在DCIM/Camera目录创建mp4本地文件，通过FileProvider生成可共享的content://Uri
     * 该Uri用于录像Intent MediaStore.EXTRA_OUTPUT参数，指定录制视频保存位置
     *
     * @param context 上下文
     * @return 录像输出视频content://Uri，创建失败返回null
     */
    fun createCameraOutVideoUri(context: Context): Uri? {
        val videoUri: Uri?
        // 时间戳唯一视频文件名，mp4通用格式
        val videoFileName = "VIDEO_" + SimpleDateFormat(
            "yyyyMMdd_HHmmssSSS", Locale.US
        ).format(System.currentTimeMillis()) + ".mp4"
        if (SdkVersionUtils.isQ) {
            // Android10+分区存储，插入视频媒体库生成Uri
            videoUri = createVideoUri(context, videoFileName)
        } else {
            // 低版本直接创建本地Camera目录视频文件
            val dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
            val cameraDir = File(dcim, "Camera")
            if (!cameraDir.exists()) cameraDir.mkdirs()
            videoUri = MediaStoreCompat.getUri(context, File(cameraDir, videoFileName).path)
        }
        return videoUri
    }

    /**
     * Android Q(API29)及以上专用：插入图片媒体库，生成空白图片记录Uri
     * 分区存储规范：不直接操作File，通过ContentResolver向媒体数据库插入一条空白图片记录，
     * 返回content://类型Uri，交给系统相机写入图片二进制数据
     * 自动区分外置存储/内置存储：SD卡挂载使用外部媒体库，无SD卡使用内部媒体库
     *
     * @param ctx 上下文
     * @param cameraFileName 图片显示文件名（DISPLAY_NAME）
     * @return MediaStore插入后的content://媒体Uri，插入失败返回null
     */
    private fun createImageUri(ctx: Context, cameraFileName: String): Uri? {
        val context = ctx.applicationContext
        val insertUri: Uri?
        // 获取外部存储挂载状态，区分内外置媒体库
        val storageStatus = Environment.getExternalStorageState()
        // 组装媒体库必填字段
        val contentValues: ContentValues = buildImageContentValues(cameraFileName)
        insertUri = if (storageStatus == Environment.MEDIA_MOUNTED) {
            // 外置SD卡/手机公共存储可用，插入外部图片媒体库
            context.contentResolver
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        } else {
            // 无可用外部存储，插入设备内置存储媒体库
            context.contentResolver
                .insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, contentValues)
        }
        return insertUri
    }

    /**
     * Android Q(API29)及以上专用：插入视频媒体库，生成空白视频记录Uri
     * 分区存储规范，不直接操作本地File，通过ContentResolver插入视频媒体条目，
     * 返回content://Uri提供给系统录像程序写入视频流；
     * 自动判断存储挂载状态，优先使用外置公共存储，无SD卡则使用内置存储
     *
     * @param ctx 上下文
     * @param videoFileName 视频展示文件名
     * @return 视频媒体content://Uri，插入失败返回null
     */
    private fun createVideoUri(ctx: Context, videoFileName: String): Uri? {
        val context = ctx.applicationContext
        val insertUri: Uri?
        val storageStatus = Environment.getExternalStorageState()
        val contentValues = buildVideoContentValues(videoFileName)
        insertUri = if (storageStatus == Environment.MEDIA_MOUNTED) {
            // 外部存储可用，插入外部视频媒体库
            context.contentResolver
                .insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
        } else {
            // 仅内置存储可用，插入内部视频媒体库
            context.contentResolver
                .insert(MediaStore.Video.Media.INTERNAL_CONTENT_URI, contentValues)
        }
        return insertUri
    }

    /**
     * 构建MediaStore插入图片所需ContentValues字段集合
     * 适配分区存储：Android Q及以上额外填充相对存储路径、拍摄时间；低版本仅填充名称与MIME类型
     * 相对路径固定为DCIM/Camera，拍摄后系统媒体库会归类到相机相册目录
     *
     * @param customFileName 自定义图片显示文件名
     * @return 填充完成的媒体字段ContentValues，用于ContentResolver.insert
     */
    private fun buildImageContentValues(customFileName: String): ContentValues {
        val currentTime: String = System.currentTimeMillis().toString()
        val values = ContentValues(3)
        // 图片展示文件名
        values.put(MediaStore.Images.Media.DISPLAY_NAME, customFileName)
        // 图片MIME类型固定为jpeg
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        if (SdkVersionUtils.isQ) {
            // 图片拍摄时间戳
            values.put(MediaStore.Images.Media.DATE_TAKEN, currentTime)
            // 媒体库相对存储目录 DCIM/Camera
            values.put(MediaStore.Images.Media.RELATIVE_PATH, DCIM_CAMERA)
        }
        return values
    }

    /**
     * 构建MediaStore插入视频所需ContentValues字段集合
     * 适配Android Q分区存储：高版本补充拍摄时间、相对存储目录；低版本仅基础名称与视频MIME
     * 视频统一归类到DCIM/Camera媒体目录，格式为mp4
     *
     * @param customFileName 自定义视频显示文件名
     * @return 填充完成的视频媒体字段ContentValues，用于ContentResolver.insert
     */
    private fun buildVideoContentValues(customFileName: String): ContentValues {
        val currentTime = System.currentTimeMillis().toString()
        val values = ContentValues(3)
        // 视频展示文件名
        values.put(MediaStore.Video.Media.DISPLAY_NAME, customFileName)
        // 视频标准MIME类型 mp4
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        if (SdkVersionUtils.isQ) {
            // 录制时间戳
            values.put(MediaStore.Video.Media.DATE_TAKEN, currentTime)
            // 媒体相对存储目录 DCIM/Camera
            values.put(MediaStore.Video.Media.RELATIVE_PATH, DCIM_CAMERA)
        }
        return values
    }

    /**
     * 插入图片到图库
     *
     * 视频不需要：
     * 录像会自动加入系统相册，因为代码通过MediaStoreOutputOptions将视频保存到了系统标准的媒体库目录（DCIM/Camera），
     *
     * 并注册到了MediaStore，相册应用会自动扫描并显示该目录下的文件
     *
     * @param context          上下文
     * @param file             要保存的文件
     * @param type             mp4 jpeg
     * @param duration         video专属的时长,图片传-1即可
     * @param width            宽
     * @param height           高
     */
    @JvmStatic
    fun displayToGallery(context: Context, file: File, @MediaType type: Int, duration: Long, width: Int, height: Int): Uri? {
        LogUtil.d("displayToGallery", file.path)
        if (!file.exists()) {
            return null
        }
        var uri: Uri?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uri = displayToGalleryAndroidQ(context, file, type, duration, width, height)
        } else {
            val photoPath = file.path
            uri = MediaStoreCompat.getUri(context, photoPath)
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
                    uri = context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
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
     * 插入图片、视频到图库(不支持音频)
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
                    val uri = MediaStoreCompat.getUri(context, photoPath)
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
                    LogUtil.d(TAG, e.message.toString())
                    e.printStackTrace()
                }
            }

            MediaType.TYPE_AUDIO -> {
                TODO()
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

}