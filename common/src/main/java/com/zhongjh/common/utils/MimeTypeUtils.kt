package com.zhongjh.common.utils

import android.text.TextUtils
import com.zhongjh.common.enums.MimeType
import java.io.File

/**
 * 类型工具类
 *
 * @author zhongjh
 * @date 2022/2/8
 */
object MimeTypeUtils {

    /**
     * is content://
     *
     * @param uri uri
     * @return 判断uri是否content类型
     */
    @JvmStatic
    fun isContent(uri: String): Boolean {
        return if (TextUtils.isEmpty(uri)) {
            false
        } else {
            uri.startsWith("content://")
        }
    }

    /**
     * 获取图片的mimeType
     *
     * @param path
     * @return
     */
    @JvmStatic
    fun getImageMimeType(path: String?): String {
        try {
            path?.let {
                val file = File(path)
                val fileName = file.name
                val beginIndex = fileName.lastIndexOf(".")
                val temp = if (beginIndex == -1) "jpeg" else fileName.substring(beginIndex + 1)
                return "image/$temp"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return MimeType.JPEG.mimeTypeName
        }
        return MimeType.JPEG.mimeTypeName
    }

    /**
     * isGif
     *
     * @param mimeType
     * @return
     */
    @JvmStatic
    fun isGif(mimeType: String?): Boolean {
        return mimeType != null && (mimeType == "image/gif" || mimeType == "image/GIF")
    }

    /**
     * isWebp
     *
     * @param mimeType
     * @return
     */
    @JvmStatic
    fun isWebp(mimeType: String?): Boolean {
        return mimeType != null && mimeType.equals("image/webp", ignoreCase = true)
    }

}