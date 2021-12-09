package com.zhongjh.common.enums

import android.content.ContentResolver
import android.net.Uri
import android.text.TextUtils
import android.webkit.MimeTypeMap
import androidx.collection.ArraySet
import com.zhongjh.common.utils.BasePhotoMetadataUtils
import java.util.*

/**
 *
 * @author zhongjh
 * @date 2021/11/12
 * @param mMimeTypeName 类型名称
 * @param mExtensions 保存所有类型
 */
enum class MimeType(val mMimeTypeName: String, val mExtensions: Set<String>) {

    // ============== 图片 ==============
    JPEG("image/jpeg", ArraySet(listOf(
            "jpg",
            "jpeg"
    ))),
    PNG("image/png", ArraySet(listOf(
            "png"
    ))),
    GIF("image/gif", ArraySet(listOf(
            "gif"
    ))),
    BMP("image/x-ms-bmp", ArraySet(listOf(
            "bmp"
    ))),
    WEBP("image/webp", ArraySet(listOf(
            "webp"
    ))),
    // ============== 音频 ==============
    MP3("video/mp3", ArraySet(listOf(
            "mp3"
    ))),
    // ============== 视频 ==============
    MPEG("video/mpeg", ArraySet(listOf(
            "mpeg",
            "mpg"
    ))),
    MP4("video/mp4", ArraySet(listOf(
            "mp4",
            "m4v"
    ))),
    QUICKTIME("video/quicktime", ArraySet(listOf(
            "mov"
    ))),
    THREEGPP("video/3gpp", ArraySet(listOf(
            "3gp",
            "3gpp"
    ))),
    THREEGPP2("video/3gpp2", ArraySet(listOf(
            "3g2",
            "3gpp2"
    ))),
    MKV("video/x-matroska", ArraySet(listOf(
            "mkv"
    ))),
    WEBM("video/webm", ArraySet(listOf(
            "webm"
    ))),
    TS("video/mp2ts", ArraySet(listOf(
            "ts"
    ))),
    AVI("video/avi", ArraySet(listOf(
            "avi"
    )));

    override fun toString(): String {
        return mMimeTypeName
    }

    fun checkType(resolver: ContentResolver, uri: Uri?): Boolean {
        val map = MimeTypeMap.getSingleton()
        if (uri == null) {
            return false
        }
        // 获取类型
        val type = map.getExtensionFromMimeType(resolver.getType(uri))
        var path: String? = null
        var pathParsed = false
        for (extension in mExtensions) {
            if (extension == type) {
                // 如果有符合的类型，直接返回true
                return true
            }
            if (!pathParsed) {
                path = BasePhotoMetadataUtils.getPath(resolver, uri)
                if (!TextUtils.isEmpty(path)) {
                    if (path != null) {
                        path = path.toLowerCase(Locale.US)
                    }
                }
                pathParsed = true
            }
            // 判断字符串是否以指定类型后缀结尾
            if (path != null && path.endsWith(extension)) {
                return true
            }
        }
        // 如果类型或者地址后缀都不一样则范围false
        return false
    }



    companion object {

        @JvmStatic
        fun ofAll(): Set<MimeType> {
            return EnumSet.allOf(MimeType::class.java)
        }

        @JvmStatic
        fun of(type: MimeType, vararg rest: MimeType): Set<MimeType> {
            return EnumSet.of(type, *rest)
        }

        @JvmStatic
        fun ofImage(): Set<MimeType> {
            return EnumSet.of(JPEG, PNG, GIF, BMP, WEBP)
        }

        @JvmStatic
        fun ofVideo(): Set<MimeType> {
            return EnumSet.of(MPEG, MP4, QUICKTIME, THREEGPP, THREEGPP2, MKV, WEBM, TS, AVI)
        }

        @JvmStatic
        fun isImage(mimeType: String?): Boolean {
            return mimeType?.startsWith("image") ?: false
        }

        @JvmStatic
        fun isVideo(mimeType: String?): Boolean {
            return mimeType?.startsWith("video") ?: false
        }

        @JvmStatic
        fun isGif(mimeType: String?): Boolean {
            if (mimeType == null) {
                return false
            } else {
                return mimeType == GIF.toString()
            }
        }


    }
}