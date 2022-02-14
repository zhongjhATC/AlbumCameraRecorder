package com.zhongjh.common.utils

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.zhongjh.common.entity.SaveStrategy
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 有关多媒体的文件操作
 *
 * @author zhongjh
 * @date 2018/8/23
 *
 * @param context 上下文，因为有的是Activity,所以使用弱引用
 * @param saveStrategy 设置目录
 */
class MediaStoreCompat(private val context: Context, var saveStrategy: SaveStrategy) {

    /**
     * 创建文件
     *
     * @param type    0是图片 1是视频 2是音频
     * @param isCache 是否缓存文件夹
     * @param format 后缀名格式
     * @return 文件
     */
    fun createFile(type: Int, isCache: Boolean, format: String): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault()).format(Date())
        val fileName = when (type) {
            0 -> String.format("IMAGE_%s.$format", timeStamp)
            1 -> String.format("VIDEO_%s.$format", timeStamp)
            2 -> String.format("AUDIO_%s.$format", timeStamp)
            else -> throw RuntimeException("The type must be 2-0.")
        }
        return createFile(fileName, type, isCache)
    }

    /**
     * 通过名字创建文件
     *
     * @param fileName 文件名
     * @param type     0是图片 1是视频 2是音频
     * @param isCache  是否缓存文件夹
     * @return 文件
     */
    fun createFile(fileName: String, type: Int, isCache: Boolean): File {
        val storageDir: File
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // 29以上的版本都必须是私有的或者公共目录
            if (isCache) {
                storageDir = File(context.externalCacheDir!!.path + File.separator + saveStrategy.directory)
            } else {
                storageDir = when (type) {
                    0 -> context.getExternalFilesDir(Environment.DIRECTORY_PICTURES + File.separator + saveStrategy.directory)!!
                    1 -> context.getExternalFilesDir(Environment.DIRECTORY_MOVIES + File.separator + saveStrategy.directory)!!
                    2 -> context.getExternalFilesDir(Environment.DIRECTORY_MUSIC + File.separator + saveStrategy.directory)!!
                    else -> throw RuntimeException("The type must be 2-0.")
                }
            }
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }
        } else {
            if (isCache) {
                storageDir = File(context.externalCacheDir!!.path + File.separator + saveStrategy.directory)
                if (!storageDir.exists()) {
                    storageDir.mkdirs()
                }
            } else {
                if (saveStrategy.isPublic) {
                    // sd卡外部目录
                    storageDir = Environment.getExternalStoragePublicDirectory(saveStrategy.directory)
                    if (!storageDir.exists()) {
                        storageDir.mkdirs()
                    }
                } else {
                    // sd卡外部目录下，app卸载后会清除掉
                    storageDir = context.getExternalFilesDir(saveStrategy.directory)!!
                }
            }
        }
        return File(storageDir, fileName)
    }

    /**
     * 通过名字new文件，并不新建
     *
     * @param fileName 文件名
     * @param type     0是图片 1是视频 2是音频
     * @param isCache  是否缓存文件夹
     * @return 文件
     */
    fun fineFile(fileName: String, type: Int, isCache: Boolean): File {
        val storageDir: File
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // 29以上的版本都必须是私有的或者公共目录
            if (isCache) {
                storageDir = File(context.externalCacheDir!!.path + File.separator + saveStrategy.directory)
            } else {
                storageDir = when (type) {
                    0 -> context.getExternalFilesDir(Environment.DIRECTORY_PICTURES + File.separator + saveStrategy.directory)!!
                    1 -> context.getExternalFilesDir(Environment.DIRECTORY_MOVIES + File.separator + saveStrategy.directory)!!
                    2 -> context.getExternalFilesDir(Environment.DIRECTORY_MUSIC + File.separator + saveStrategy.directory)!!
                    else -> throw RuntimeException("The type must be 2-0.")
                }
            }
        } else {
            if (isCache) {
                storageDir = File(context.externalCacheDir!!.path + File.separator + saveStrategy.directory)
            } else {
                if (saveStrategy.isPublic) {
                    // sd卡外部目录
                    storageDir = Environment.getExternalStoragePublicDirectory(saveStrategy.directory)
                } else {
                    // sd卡外部目录下，app卸载后会清除掉
                    storageDir = context.getExternalFilesDir(saveStrategy.directory)!!
                }
            }
        }
        return File(storageDir, fileName)
    }

    /**
     * @param bitmap  保存bitmap到file
     * @param isCache 是否缓存文件夹
     * @return 返回file的路径
     */
    fun saveFileByBitmap(bitmap: Bitmap, isCache: Boolean): File {
        val file = createFile(0, isCache, "jpg")
        try {
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }

    fun getUri(path: String): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, saveStrategy.authority!!, File(path))
        } else {
            Uri.fromFile(File(path))
        }
    }

    fun getUri(): Uri {
        return FileProvider.getUriForFile(context, saveStrategy.authority!!, File(saveStrategy.directory))
    }

    companion object {
        /**
         * 检查设备是否具有相机特性。
         *
         * @param context 检查相机特征的上下文。
         * @return 如果设备具有相机特性，则为真。否则为假。
         */
        @JvmStatic
        fun hasCameraFeature(context: Context): Boolean {
            val pm = context.applicationContext.packageManager
            return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)
        }
    }

}