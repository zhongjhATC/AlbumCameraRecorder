package com.zhongjh.multimedia.album.utils

import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.graphics.Point
import android.net.Uri
import android.util.Log
import com.zhongjh.common.utils.BasePhotoMetadataUtils
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

/**
 * @author zhongjh
 */
class PhotoMetadataUtils private constructor() : BasePhotoMetadataUtils() {

    init {
        throw AssertionError("oops! the utility class is about to be instantiated...")
    }

    companion object {
        private val TAG: String = PhotoMetadataUtils::class.java.simpleName

        /**
         * 获取长度和宽度
         *
         * @param resolver ContentResolver共享数据库
         * @param uri      图片uri
         * @return xy
         */
        @JvmStatic
        fun getBitmapBound(resolver: ContentResolver, uri: Uri): Point {
            var `is`: InputStream? = null
            try {
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                `is` = resolver.openInputStream(uri)
                BitmapFactory.decodeStream(`is`, null, options)
                val width = options.outWidth
                val height = options.outHeight
                return Point(width, height)
            } catch (e: FileNotFoundException) {
                return Point(0, 0)
            } finally {
                if (`is` != null) {
                    try {
                        `is`.close()
                    } catch (e: IOException) {
                        Log.e(TAG, "getBitmapBound" + e.message)
                    }
                }
            }
        }

        /**
         * bytes转换mb
         *
         * @param sizeInBytes 容量大小
         * @return mb
         */
        @JvmStatic
        fun getSizeInMb(sizeInBytes: Long): Float {
            val df = NumberFormat.getNumberInstance(Locale.US) as DecimalFormat
            df.applyPattern("0.0")
            var result = df.format((sizeInBytes.toFloat() / 1024 / 1024).toDouble())
            Log.d(TAG, "getSizeInMB: $result")
            // in some case , 0.0 will be 0,0
            result = result.replace(",".toRegex(), ".")
            return result.toFloat()
        }
    }
}
