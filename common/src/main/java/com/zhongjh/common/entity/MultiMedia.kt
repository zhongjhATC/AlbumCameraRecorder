package com.zhongjh.common.entity

import android.annotation.SuppressLint
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.provider.MediaStore.*

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
    var multiMediaId: Long = 0

    /**
     * 在线网址
     */
    var url: String? = null

    /**
     * 图片资源id
     */
    var drawableId: Int = -1

    constructor() : super()

    constructor(localFile: LocalFile) : super(localFile)

    /**
     * 相册初始化调用
     */
    constructor(id: Long, mimeType: String, size: Long, duration: Long, width: Int, height: Int) : super() {
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
        this.originalUri = ContentUris.withAppendedId(contentUri, id)
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
        multiMediaId = input.readLong()
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
        dest.writeLong(multiMediaId)
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
        return id == multiMedia.id
                && multiMediaId == multiMedia.multiMediaId
                && (mimeType != null && mimeType.equals(multiMedia.mimeType) || (mimeType == null && multiMedia.mimeType == null))
                && (uri != null && uri!! == multiMedia.uri || (uri == null && multiMedia.uri == null))
                && (url != null && url!! == multiMedia.url || (url == null && multiMedia.url == null))
                && size == multiMedia.size
                && duration == multiMedia.duration
                && drawableId == multiMedia.drawableId
    }

    /**
     * 重写hashCode，所以如果修改以下这些值，那么将会它存于的hashMap找不到它
     */
    override fun hashCode(): Int {
        var result = 1
        result = 31 * result + java.lang.Long.valueOf(id).hashCode()
        result = 31 * result + java.lang.Long.valueOf(multiMediaId).hashCode()
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

        /**
         * 获取相同数据的索引
         *
         * @param items 数据列表
         * @param item  当前数据
         * @return 索引
         */
        @JvmStatic
        fun checkedNumOf(items: List<MultiMedia>, item: MultiMedia): Int {
            var index = -1
            if (item.uri != null) {
                for (i in items.indices) {
                    if (items[i].uri != null && items[i].uri == item.uri
                        && items[i].multiMediaId == item.multiMediaId
                    ) {
                        index = i
                        break
                    }
                }
            } else if (item.drawableId != -1) {
                for (i in items.indices) {
                    if (items[i].drawableId != -1 && items[i].drawableId == item.drawableId && items[i].multiMediaId == item.multiMediaId) {
                        index = i
                        break
                    }
                }
            } else if (item.url != null) {
                for (i in items.indices) {
                    if (items[i].url != null && items[i].url == item.url
                        && items[i].multiMediaId == item.multiMediaId
                    ) {
                        index = i
                        break
                    }
                }
            }
            // 如果选择的为 -1 就是未选状态，否则选择基础数量+1
            return if (index == -1) Int.MIN_VALUE else index + 1
        }

        /**
         * 获取相同数据的對象
         *
         * @param items 数据列表
         * @param item  当前数据
         * @return 索引
         */
        @JvmStatic
        fun checkedMultiMediaOf(items: List<MultiMedia>, item: MultiMedia): MultiMedia? {
            var multiMedia: MultiMedia? = null
            if (item.uri != null) {
                for (i in items.indices) {
                    if (items[i].uri == item.uri) {
                        multiMedia = items[i]
                        break
                    }
                }
            } else if (item.drawableId != -1) {
                for (i in items.indices) {
                    if (items[i].drawableId == item.drawableId) {
                        multiMedia = items[i]
                        break
                    }
                }
            } else if (item.url != null) {
                for (i in items.indices) {
                    if (items[i].url == item.url) {
                        multiMedia = items[i]
                        break
                    }
                }
            }
            return multiMedia
        }

    }


}