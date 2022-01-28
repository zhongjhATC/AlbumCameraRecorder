package com.zhongjh.common.entity

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.zhongjh.common.enums.MimeType
import com.zhongjh.common.utils.MediaStoreCompat
import com.zhongjh.common.utils.MediaStoreUtils
import java.io.File

/**
 * 文件地址
 * @author zhongjh
 * @date 2021/12/24
 */
open class LocalFile : Parcelable {

    /**
     * 用于区分，因为九宫数据是允许选择重复的
     */
    var id: Long = 0

    /**
     * 真实路径
     */
    var path: String? = null

    /**
     * 真实路径转换成的uri
     */
    var uri: Uri? = null

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
     * 编辑前的真实路径
     */
    var oldPath: String? = null

    /**
     * 编辑前的URI
     */
    var oldUri: Uri? = null

    constructor()

    /**
     * 从localFile赋值到另外一个新的localFile
     * 之所以这样做是因为Parcelable如果使用的是看似父类其实是子类就会出问题
     */
    constructor(localFile: LocalFile) : super() {
        id = localFile.id
        path = localFile.path
        uri = localFile.uri
        mimeType = localFile.mimeType
        size = localFile.size
        duration = localFile.duration
        oldPath = localFile.oldPath
        oldUri = localFile.oldUri
        height = localFile.height
        width = localFile.width
    }

    /**
     * 赋值一个新的path，借由这个新的path，修改相关参数
     */
    constructor(mediaStoreCompat: MediaStoreCompat, localFile: LocalFile, compressionFile: File) : super() {
        updateFile(mediaStoreCompat, localFile, compressionFile)
    }

    /**
     * 修改新的file
     */
    fun updateFile(mediaStoreCompat: MediaStoreCompat, localFile: LocalFile, compressionFile: File) {
        id = localFile.id
        this.path = compressionFile.absolutePath
        this.uri = mediaStoreCompat.getUri(compressionFile.absolutePath)
        mimeType = localFile.mimeType
        size = compressionFile.length()
        duration = localFile.duration
        oldPath = localFile.oldPath
        oldUri = localFile.oldUri
        val imageWidthAndHeight: IntArray = MediaStoreUtils.getImageWidthAndHeight(compressionFile.absolutePath)
        height = imageWidthAndHeight[1]
        width = imageWidthAndHeight[0]
    }

    constructor(input: Parcel) {
        id = input.readLong()
        path = input.readString()
        uri = input.readParcelable(Uri::class.java.classLoader)
        mimeType = input.readString()
        size = input.readLong()
        duration = input.readLong()
        oldPath = input.readString()
        oldUri = input.readParcelable(Uri::class.java.classLoader)
        height = input.readInt()
        width = input.readInt()
    }

    constructor(multiMedia: MultiMedia) {
        id = multiMedia.id
        path = multiMedia.path
        uri = multiMedia.uri
        mimeType = multiMedia.mimeType
        size = multiMedia.size
        duration = multiMedia.duration
        oldPath = multiMedia.oldPath
        oldUri = multiMedia.oldUri
        height = multiMedia.height
        width = multiMedia.width
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(id)
        dest.writeString(path)
        dest.writeParcelable(uri, flags)
        dest.writeString(mimeType)
        dest.writeLong(size)
        dest.writeLong(duration)
        dest.writeString(oldPath)
        dest.writeParcelable(oldUri, flags)
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
        return  mimeType.equals(MimeType.MPEG.toString())
                || mimeType.equals(MimeType.MP4.toString())
                || mimeType.equals(MimeType.QUICKTIME.toString())
                || mimeType.equals(MimeType.THREEGPP.toString())
                || mimeType.equals(MimeType.THREEGPP2.toString())
                || mimeType.equals(MimeType.MKV.toString())
                || mimeType.equals(MimeType.WEBM.toString())
                || mimeType.equals(MimeType.TS.toString())
                || mimeType.equals(MimeType.AVI.toString())
    }

}