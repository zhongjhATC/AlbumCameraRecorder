package com.zhongjh.common.enums

import android.text.TextUtils
import android.webkit.MimeTypeMap
import androidx.collection.ArraySet
import java.io.File
import java.net.URLConnection
import java.util.EnumSet
import java.util.Locale

/**
 *
 * @author zhongjh
 * @date 2021/11/12
 * @param mimeTypeName 类型名称
 * @param extensions 保存所有类型
 */
enum class MimeType(val mimeTypeName: String, val extensions: Set<String>) {

    // ============== 图片 ==============
    JPEG(
        "image/jpeg", ArraySet(
            listOf(
                "jpg",
                "jpeg"
            )
        )
    ),
    PNG(
        "image/png", ArraySet(
            listOf(
                "png"
            )
        )
    ),
    GIF(
        "image/gif", ArraySet(
            listOf(
                "gif"
            )
        )
    ),
    BMP(
        "image/bmp", ArraySet(
            listOf(
                "bmp"
            )
        )
    ),
    X_MS_BMP(
        "image/x-ms-bmp", ArraySet(
            listOf(
                "x-ms-bmp"
            )
        )
    ),
    VND_BMP(
        "image/vnd.wap.wbmp", ArraySet(
            listOf(
                "vnd.wap.wbmp"
            )
        )
    ),
    WEBP(
        "image/webp", ArraySet(
            listOf(
                "webp"
            )
        )
    ),
    HEIC(
        "image/heic", ArraySet(
            listOf(
                "heic"
            )
        )
    ),

    // ============== 音频 ==============
    AUDIO_MPEG(
        "audio/mpeg", ArraySet(
            listOf(
                "mpeg"
            )
        )
    ),
    AAC(
        "video/aac", ArraySet(
            listOf(
                "aac"
            )
        )
    ),

    // ============== 视频 ==============
    MPEG(
        "video/mpeg", ArraySet(
            listOf(
                "mpeg",
                "mpg"
            )
        )
    ),
    MP4(
        "video/mp4", ArraySet(
            listOf(
                "mp4",
                "m4v"
            )
        )
    ),
    QUICKTIME(
        "video/quicktime", ArraySet(
            listOf(
                "mov"
            )
        )
    ),
    THREE_GPP(
        "video/3gpp", ArraySet(
            listOf(
                "3gp",
                "3gpp"
            )
        )
    ),
    THREE_GPP2(
        "video/3gpp2", ArraySet(
            listOf(
                "3g2",
                "3gpp2"
            )
        )
    ),
    MKV(
        "video/x-matroska", ArraySet(
            listOf(
                "mkv"
            )
        )
    ),
    WEBM(
        "video/webm", ArraySet(
            listOf(
                "webm"
            )
        )
    ),
    TS(
        "video/mp2ts", ArraySet(
            listOf(
                "ts"
            )
        )
    ),
    AVI(
        "video/avi", ArraySet(
            listOf(
                "avi"
            )
        )
    );

    override fun toString(): String {
        return mimeTypeName
    }

    fun checkType(absolutePath: String): Boolean {
        for (extension in extensions) {
            // 判断字符串是否以指定类型后缀结尾
            if (absolutePath.lowercase(Locale.getDefault()).endsWith(extension.lowercase(Locale.getDefault()))) {
                return true
            }
        }
        // 如果类型或者地址后缀都不一样则范围false
        return false
    }

    /**
     * 类型工具类
     */
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
            return EnumSet.of(JPEG, PNG, GIF, BMP, X_MS_BMP, VND_BMP, WEBP, HEIC)
        }

        @JvmStatic
        fun ofVideo(): Set<MimeType> {
            return EnumSet.of(MPEG, MP4, QUICKTIME, THREE_GPP, THREE_GPP2, MKV, WEBM, TS, AVI)
        }

        @JvmStatic
        fun isImageOrGif(mimeType: String?): Boolean {
            return mimeType?.startsWith("image") ?: false
        }

        @JvmStatic
        fun isVideo(mimeType: String?): Boolean {
            return mimeType?.startsWith("video") ?: false
        }

        @JvmStatic
        fun isAudio(mimeType: String?): Boolean {
            return mimeType?.startsWith("audio") ?: false
        }

        @JvmStatic
        fun hasMimeTypeOfUnknown(mimeType: String?): Boolean {
            return mimeType != null && mimeType.startsWith("image/*")
        }

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
         * is Network
         *
         * @param path path
         * @return 判断path是否http类型
         */
        fun isHasHttp(path: String): Boolean {
            if (TextUtils.isEmpty(path)) {
                return false
            }
            return path.startsWith("http") || path.startsWith("https")
        }

        /**
         * 获取图片的mimeType
         *
         * @param path 根据路径获取
         * @return MimeType
         */
        fun getMimeType(path: String?): String? {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(path)
            var mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                fileExtension.lowercase(Locale.getDefault())
            )
            if (TextUtils.isEmpty(mimeType)) {
                val fileNameMap = URLConnection.getFileNameMap()
                mimeType = fileNameMap.getContentTypeFor(path?.let { File(it).name })
            }
            return mimeType
        }

        @JvmStatic
        fun ofImageArray(): Array<String> {
            return arrayOf(
                JPEG.mimeTypeName,
                PNG.mimeTypeName,
                GIF.mimeTypeName,
                BMP.mimeTypeName,
                X_MS_BMP.mimeTypeName,
                VND_BMP.mimeTypeName,
                WEBP.mimeTypeName,
                HEIC.mimeTypeName
            )
        }

        @JvmStatic
        fun ofVideoArray(): Array<String> {
            return arrayOf(
                MPEG.mimeTypeName,
                MP4.mimeTypeName,
                QUICKTIME.mimeTypeName,
                THREE_GPP.mimeTypeName,
                THREE_GPP2.mimeTypeName,
                MKV.mimeTypeName,
                WEBM.mimeTypeName,
                TS.mimeTypeName,
                AVI.mimeTypeName
            )
        }


    }
}