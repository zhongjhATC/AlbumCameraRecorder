package com.zhongjh.common.entity

import android.annotation.SuppressLint
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.provider.MediaStore.*
import com.zhongjh.common.enums.MimeType
import com.zhongjh.common.enums.MultimediaTypes

/**
 * 多媒体实体类
 *
 * @author zhongjh
 * @date 2019/1/22
 */
open class MultiMedia : LocalFile, Parcelable {

    /**
     * 用于区分，因为九宫数据是允许选择重复的
     */
    var id: Long = 0

    /**
     * 在线网址
     */
    var url: String? = null

    /**
     * 图片资源id
     */
    var drawableId: Int = -1

    constructor() : super()

    /**
     * 相册初始化调用
     */
    constructor(id: Long, mimeType: String, size: Long, duration: Long) : super() {
        this.id = id
        this.mimeType = mimeType
        val contentUri: Uri = when {
            isImage() -> {
                Images.Media.EXTERNAL_CONTENT_URI
            }
            isVideo() -> {
                Video.Media.EXTERNAL_CONTENT_URI
            }
            else -> {
                Files.getContentUri("external")
            }
        }
        this.uri = ContentUris.withAppendedId(contentUri, id)
        this.size = size
        this.duration = duration
    }

    constructor(input: Parcel) : super(input) {
        id = input.readLong()
        drawableId = input.readInt()
        url = input.readString()
    }

    /**
     * 重写equals，所以如果修改以下这些值，那么将会导致不相等
     */
    override fun equals(other: Any?): Boolean {
        if (other !is MultiMedia) {
            return false
        }
        val multiMedia: MultiMedia = other
        return id == multiMedia.id && (mimeType != null && mimeType.equals(multiMedia.mimeType) || (mimeType == null && multiMedia.mimeType == null))
                && (uri != null && uri!! == multiMedia.uri || (uri == null && multiMedia.uri == null))
                && size == multiMedia.size
                && duration == multiMedia.duration
                && drawableId == multiMedia.drawableId
    }

    fun anyEquals(other: Any?): Boolean {
        return super.equals(other)
    }

    /**
     * 重写hashCode，所以如果修改以下这些值，那么将会它存于的hashmap找不到它
     */
    override fun hashCode(): Int {
        var result = 1
        result = 31 * result + java.lang.Long.valueOf(id).hashCode()
        if (mimeType != null) {
            result = 31 * result + mimeType.hashCode()
        }
        if (uri != null) {
            result = 31 * result + uri.hashCode()
        }
        result = 31 * result + java.lang.Long.valueOf(size).hashCode()
        result = 31 * result + java.lang.Long.valueOf(duration).hashCode()
        result = 31 * result + java.lang.Long.valueOf(drawableId.toLong()).hashCode()
        return result
    }

    fun anyHashCode(): Int {
        return super.hashCode()
    }

    fun isImage(): Boolean {
        if (mimeType == null) {
            return false
        }
        return mimeType.equals(MimeType.JPEG.toString())
                || mimeType.equals(MimeType.PNG.toString())
                || mimeType.equals(MimeType.GIF.toString())
                || mimeType.equals(MimeType.BMP.toString())
                || mimeType.equals(MimeType.WEBP.toString())
    }

    fun isGif(): Boolean {
        if (mimeType == null) {
            return false
        }
        return mimeType.equals(MimeType.GIF.toString())
    }

    fun isMp3(): Boolean {
        if (mimeType == null) {
            return false
        }
        return mimeType.equals(MimeType.MP3.toString())
    }

    fun isVideo(): Boolean {
        if (mimeType != null) {
            return mimeType.equals(MimeType.MPEG.toString())
                    || mimeType.equals(MimeType.MP4.toString())
                    || mimeType.equals(MimeType.QUICKTIME.toString())
                    || mimeType.equals(MimeType.THREEGPP.toString())
                    || mimeType.equals(MimeType.THREEGPP2.toString())
                    || mimeType.equals(MimeType.MKV.toString())
                    || mimeType.equals(MimeType.WEBM.toString())
                    || mimeType.equals(MimeType.TS.toString())
                    || mimeType.equals(MimeType.AVI.toString())
        } else if (type != -1) {
            return type == MultimediaTypes.VIDEO
        }
        return false
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeLong(id)
        dest.writeString(url)
        dest.writeInt(drawableId)
    }

    companion object {

        @JvmField
        val CREATOR: Creator<MultiMedia> = object : Creator<MultiMedia> {
            override fun createFromParcel(source: Parcel): MultiMedia {
                return MultiMedia(source)
            }

            override fun newArray(size: Int): Array<MultiMedia?> {
                return arrayOfNulls(size)
            }

        }

        @JvmStatic
        @SuppressLint("Range")
        fun valueOf(cursor: Cursor): MultiMedia {
            return MultiMedia(cursor.getLong(cursor.getColumnIndex(Files.FileColumns._ID)),
                    cursor.getString(cursor.getColumnIndex(MediaColumns.MIME_TYPE)),
                    cursor.getLong(cursor.getColumnIndex(MediaColumns.SIZE)),
                    cursor.getLong(cursor.getColumnIndex("duration")))
        }


    }


}