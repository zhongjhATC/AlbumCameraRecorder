package com.zhongjh.common.entity

import android.content.Context
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import com.zhongjh.common.enums.MimeType
import com.zhongjh.common.utils.MediaStoreCompat
import com.zhongjh.common.utils.MediaUtils
import com.zhongjh.common.utils.UriUtils
import java.io.File

/**
 * 文件地址
 * 该实体会分别拥有三个地址
 * [path] 真实路径。如果开启压缩功能则是压缩后的路径，否则跟原图一样
 * [originalPath] 原始路径。如果开启压缩功能则是压缩前的路径，否则跟[path]一样
 * [oldPath] 编辑前的路径。当使用编辑功能后，[path]会记录下编辑后的路径，而[oldPath]则会记录下编辑前的路径。如果没有进行编辑，则为null
 * 当用户勾选原图事件，albumSetting.setOnCheckedListener该事件会触发,赋值[isOriginal]
 * @author zhongjh
 * @date 2021/12/24
 */
open class LocalFile : Parcelable {

    /**
     * 相册的id数据
     */
    var id: Long = 0

    /**
     * 真实路径。如果开启压缩功能则是压缩后的路径，否则跟原图一样
     */
    var path: String? = null

    /**
     * uri。如果开启压缩功能则是压缩后的路径，否则跟原图一样
     */
    var uri: Uri? = null

    /**
     * 原始的真实路径。如果开启压缩功能则是压缩前的路径，否则跟[path]一样
     */
    var originalPath: String? = null

    /**
     * 原始uri。如果开启压缩功能则是压缩前的路径，否则跟[uri]一样
     */
    var originalUri: Uri? = null

    /**
     * 具体类型，jpg,png,mp3等等
     * {@link MimeType }
     */
    var mimeType: String? = null
    var size: Long = 0

    /**
     * only for video, in ms
     */
    var duration: Long = 0

    /**
     * 宽度,只针对图片、视频
     */
    var width: Int = 0

    /**
     * 高度
     */
    var height: Int = 0

    /**
     * 是否开启了原图
     */
    var isOriginal = false

    /**
     * 编辑前的真实路径
     */
    var oldPath: String? = null

    /**
     * 编辑前的URI
     */
    var oldUri: Uri? = null

    constructor()

    /**
     * 从 localFile 赋值到另外一个新的 localFile
     * 之所以这样做是因为 Parcelable 如果使用的是看似父类其实是子类就会出问题
     */
    constructor(localFile: LocalFile) : super() {
        id = localFile.id
        path = localFile.path
        uri = localFile.uri
        originalPath = localFile.originalPath
        originalUri = localFile.originalUri
        mimeType = localFile.mimeType
        size = localFile.size
        duration = localFile.duration
        oldPath = localFile.oldPath
        oldUri = localFile.oldUri
        isOriginal = localFile.isOriginal
        width = localFile.width
        height = localFile.height
    }

    /**
     * 赋值一个新的path，借由这个新的path，修改相关参数
     */
    constructor(
        context: Context,
        mediaStoreCompat: MediaStoreCompat,
        localFile: LocalFile,
        compressionFile: File,
        isCompress: Boolean
    ) : super() {
        updateFile(context, mediaStoreCompat, localFile, compressionFile, isCompress)
    }

    constructor(input: Parcel) {
        id = input.readLong()
        path = input.readString()
        uri = input.readParcelable(Uri::class.java.classLoader)
        mimeType = input.readString()
        size = input.readLong()
        duration = input.readLong()
        originalPath = input.readString()
        originalUri = input.readParcelable(Uri::class.java.classLoader)
        oldPath = input.readString()
        oldUri = input.readParcelable(Uri::class.java.classLoader)
        val original = input.readLong()
        isOriginal = original == 1L
        width = input.readInt()
        height = input.readInt()
    }

    constructor(multiMedia: MultiMedia) {
        id = multiMedia.id
        path = multiMedia.path
        uri = multiMedia.uri
        mimeType = multiMedia.mimeType
        size = multiMedia.size
        duration = multiMedia.duration
        originalPath = multiMedia.originalPath
        originalUri = multiMedia.originalUri
        oldPath = multiMedia.oldPath
        oldUri = multiMedia.oldUri
        isOriginal = multiMedia.isOriginal
        width = multiMedia.width
        height = multiMedia.height
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(id)
        dest.writeString(path)
        dest.writeParcelable(uri, flags)
        dest.writeString(mimeType)
        dest.writeLong(size)
        dest.writeLong(duration)
        dest.writeString(originalPath)
        dest.writeParcelable(originalUri, flags)
        dest.writeString(oldPath)
        dest.writeParcelable(oldUri, flags)
        if (isOriginal) {
            dest.writeLong(1)
        } else {
            dest.writeLong(0)
        }
        dest.writeInt(width)
        dest.writeInt(height)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LocalFile> {
        override fun createFromParcel(parcel: Parcel): LocalFile {
            return LocalFile(parcel)
        }

        override fun newArray(size: Int): Array<LocalFile?> {
            return arrayOfNulls(size)
        }
    }

    /**
     * 不包含gif
     */
    fun isImage(): Boolean {
        if (mimeType == null) {
            return false
        }
        return mimeType.equals(MimeType.JPEG.toString())
                || mimeType.equals(MimeType.PNG.toString())
                || mimeType.equals(MimeType.BMP.toString())
                || mimeType.equals(MimeType.WEBP.toString())
    }

    /**
     * 单纯gif
     */
    fun isGif(): Boolean {
        if (mimeType == null) {
            return false
        }
        return mimeType.equals(MimeType.GIF.toString())
    }

    /**
     * 包含gif
     */
    fun isImageOrGif(): Boolean {
        if (mimeType == null) {
            return false
        }
        return mimeType.equals(MimeType.JPEG.toString())
                || mimeType.equals(MimeType.PNG.toString())
                || mimeType.equals(MimeType.GIF.toString())
                || mimeType.equals(MimeType.BMP.toString())
                || mimeType.equals(MimeType.WEBP.toString())
    }

    fun isAudio(): Boolean {
        if (mimeType == null) {
            return false
        }
        return mimeType.equals(MimeType.AAC.toString())
    }

    fun isVideo(): Boolean {
        if (mimeType == null) {
            return false
        }
        return mimeType.equals(MimeType.MPEG.toString())
                || mimeType.equals(MimeType.MP4.toString())
                || mimeType.equals(MimeType.QUICKTIME.toString())
                || mimeType.equals(MimeType.THREEGPP.toString())
                || mimeType.equals(MimeType.THREEGPP2.toString())
                || mimeType.equals(MimeType.MKV.toString())
                || mimeType.equals(MimeType.WEBM.toString())
                || mimeType.equals(MimeType.TS.toString())
                || mimeType.equals(MimeType.AVI.toString())
    }

    /**
     * 场景：在相册预览等界面迁移图片到配置文件夹处，重新生成新的地址
     * 修改新的file
     */
    fun updateFile(
        context: Context,
        mediaStoreCompat: MediaStoreCompat,
        localFile: LocalFile,
        compressionFile: File,
        isCompress: Boolean
    ) {
        id = localFile.id
        this.path = compressionFile.absolutePath
        this.uri = mediaStoreCompat.getUri(compressionFile.absolutePath)
        // 如果支持压缩，则原图是压缩前的，否则原图跟path是一样的
        if (isCompress) {
            this.originalPath = localFile.originalPath
            this.originalUri = localFile.originalUri
        } else {
            this.originalPath = this.path
            this.originalUri = this.uri
        }
        mimeType = localFile.mimeType
        size = compressionFile.length()
        duration = localFile.duration
        oldPath = localFile.oldPath
        oldUri = localFile.oldUri
        isOriginal = localFile.isOriginal
        if (isImageOrGif()) {
            val imageWidthAndHeight: IntArray =
                MediaUtils.getImageWidthAndHeight(compressionFile.absolutePath)
            width = imageWidthAndHeight[0]
            height = imageWidthAndHeight[1]
        } else if (isVideo()) {
            // 有些手机视频拍照没有宽高的
            if (localFile.width == 0) {
                val mediaExtraInfo = MediaUtils.getVideoSize(context, compressionFile.absolutePath)
                width = mediaExtraInfo.width
                height = mediaExtraInfo.height
                duration = mediaExtraInfo.duration
            } else {
                width = localFile.width
                height = localFile.height
            }
        }
    }

    /**
     * 场景：初始化相册时点击item时赋值
     * 根据相册当前uri同时赋值真实路径path和原图真实路径originalPath
     */
    fun analysesUriSetPathAndOriginalPath(context: Context) {
        if (TextUtils.isEmpty(path)) {
            // 相册是只有uri没有path的，此时确定后转换
            val file = UriUtils.uriToFile(context, uri)
            if (file != null && file.exists()) {
                path = file.path
                originalPath = file.path
            }
        }
    }

    /**
     * 场景：图片进行编辑后的赋值
     * 处理编辑后的赋值
     */
    fun handleEditValue(newPath: String, newUri: Uri?, oldPath: String?, oldUri: Uri?) {
        this.path = newPath
        this.uri = newUri
        this.originalPath = newPath
        this.originalUri = newUri
        this.oldPath = oldPath
        this.oldUri = oldUri
    }

}