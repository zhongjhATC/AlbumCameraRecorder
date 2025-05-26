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