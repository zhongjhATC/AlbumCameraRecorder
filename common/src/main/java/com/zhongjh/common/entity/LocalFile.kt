package com.zhongjh.common.entity

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.zhongjh.common.enums.MultimediaTypes

/**
 * 文件地址
 * @author zhongjh
 * @date 2021/12/24
 */
open class LocalFile() : Parcelable {

    /**
     * 真实路径
     */
    var path: String? = null

    /**
     * 以路径转换成的uri
     */
    var uri: Uri? = null

    /**
     * 范围类型,0是图片,1是视频,2是音频,-1是添加功能 MultimediaTypes
     */
    @MultimediaTypes
    var type = 0

    /**
     * 具体类型，jpg,png,mp3等等
     */
    var mimeType: String? = null
    var size: Long = 0

    /**
     * only for video, in ms
     */
    var duration: Long = 0

    /**
     * 编辑前的真实路径
     */
    var oldPath: String? = null

    /**
     * 编辑前的相册URI
     */
    var oldMediaUri: Uri? = null

    /**
     * 编辑前的URI
     */
    var oldUri: Uri? = null

    private constructor(input: Parcel) {
        path = input.readString()
        uri = input.readParcelable(Uri::class.java.classLoader)
        type = input.readInt()
        mimeType = input.readString()
        size = input.readLong()
        duration = input.readLong()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(path)
        dest.writeParcelable(uri, flags)
        dest.writeInt(type)
        dest.writeString(mimeType)
        dest.writeLong(size)
        dest.writeLong(duration)
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

}