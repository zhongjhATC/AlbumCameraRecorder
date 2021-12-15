package com.zhongjh.progresslibrary.entity

import android.os.Parcel
import android.os.Parcelable

/**
 * 音频文件的存储
 * @author zhongjh
 */
class RecordingItem() : Parcelable {

    /**
     * 路径
     */
    var filePath: String? = null

    /**
     * 网址
     */
    var url: String? = null

    /**
     * 长度，单位秒
     */
    var length = 0

    constructor(parcel: Parcel) : this() {
        this.filePath = parcel.readString()
        this.url = parcel.readString()
        this.length = parcel.readInt()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(filePath)
        dest.writeString(url)
        dest.writeInt(length)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RecordingItem> {
        override fun createFromParcel(parcel: Parcel): RecordingItem {
            return RecordingItem(parcel)
        }

        override fun newArray(size: Int): Array<RecordingItem?> {
            return arrayOfNulls(size)
        }
    }


}