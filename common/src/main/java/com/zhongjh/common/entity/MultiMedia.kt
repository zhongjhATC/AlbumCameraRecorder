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
    constructor(id: Long, mimeType: String, size: Long, duration: Long, width: Int, height: Int) : super() {
        this.id = id
        this.mimeType = mimeType
        val contentUri: Uri
        when {
            isImage() -> {
                contentUri = Images.Media.EXTERNAL_CONTENT_URI
                this.type = MultimediaTypes.PICTURE
            }
            isVideo() -> {
                contentUri = Video.Media.EXTERNAL_CONTENT_URI
                this.type = MultimediaTypes.VIDEO
            }
            else -> {
                contentUri = Files.getContentUri("external")
            }
        }
        this.uri = ContentUris.withAppendedId(contentUri, id)
        this.size = size
        this.duration = duration
        this.width = width
        this.height = height

    }

    /**
     * 这个实现的方法，成员变量的写入顺序必须和成员变量的声明顺序
     * 保持一致，不然会导致传递后数据为Null或者闪退
     */
    constructor(input: Parcel) : super(input) {
        id = input.readLong()
        drawableId = input.readInt()
        url = input.readString()
    }

    /**
     * 这个实现的方法，成员变量的写入顺序必须和成员变量的声明顺序
     * 保持一致，不然会导致传递后数据为Null或者闪退
     */
    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeLong(id)
        dest.writeInt(drawableId)
        dest.writeString(url)
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
                && (url != null && url!! == multiMedia.url || (url == null && multiMedia.url == null))
                && size == multiMedia.size
                && duration == multiMedia.duration
                && drawableId == multiMedia.drawableId
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
        if (url != null) {
            result = 31 * result + url.hashCode()
        }
        result = 31 * result + java.lang.Long.valueOf(size).hashCode()
        result = 31 * result + java.lang.Long.valueOf(duration).hashCode()
        result = 31 * result + java.lang.Long.valueOf(drawableId.toLong()).hashCode()
        return result
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

    fun isImageOrGif(): Boolean {
        if (mimeType == null) {
            return false
        }
        return mimeType.equals(MimeType.JPEG.toString())
                || mimeType.equals(MimeType.PNG.toString())
                || mimeType.equals(MimeType.GIF.toString())
                || mimeType.equals(MimeType.BMP.toString())
                || mimeType.equals(MimeType.WEBP.toString())
                || mimeType.equals(MimeType.GIF.toString())
    }

    fun isAudio(): Boolean {
        if (mimeType == null) {
            return false
        }
        return mimeType.equals(MimeType.AAC.toString())
    }

    fun isVideo(): Boolean {
        return type == MultimediaTypes.VIDEO
                || mimeType.equals(MimeType.MPEG.toString())
                || mimeType.equals(MimeType.MP4.toString())
                || mimeType.equals(MimeType.QUICKTIME.toString())
                || mimeType.equals(MimeType.THREEGPP.toString())
                || mimeType.equals(MimeType.THREEGPP2.toString())
                || mimeType.equals(MimeType.MKV.toString())
                || mimeType.equals(MimeType.WEBM.toString())
                || mimeType.equals(MimeType.TS.toString())
                || mimeType.equals(MimeType.AVI.toString())
    }

//    fun initDataByPath() {
//        if (TextUtils.isEmpty(this.mimeType)) {
//            // 获取相关属性
//            val mmr = MediaMetadataRetriever()
//            mmr.setDataSource(url, HashMap<String, String>())
//            val duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()?: 0
//            this.duration = duration
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                    this.size = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_EXIF_LENGTH)?.toLong()?: 0
//            }
//            this.width = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt()?: 0
//            this.height = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt()?: 0
//            this.mimeType = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
//        }
//    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<MultiMedia> {
        override fun createFromParcel(parcel: Parcel): MultiMedia {
            return MultiMedia(parcel)
        }

        override fun newArray(size: Int): Array<MultiMedia?> {
            return arrayOfNulls(size)
        }

        @JvmStatic
        @SuppressLint("Range")
        fun valueOf(cursor: Cursor): MultiMedia {
            return MultiMedia(cursor.getLong(cursor.getColumnIndex(Files.FileColumns._ID)),
                    cursor.getString(cursor.getColumnIndex(MediaColumns.MIME_TYPE)),
                    cursor.getLong(cursor.getColumnIndex(MediaColumns.SIZE)),
                    cursor.getLong(cursor.getColumnIndex("duration")),
                    cursor.getInt(cursor.getColumnIndex(MediaColumns.WIDTH)),
                    cursor.getInt(cursor.getColumnIndex(MediaColumns.HEIGHT)))
        }
    }


}