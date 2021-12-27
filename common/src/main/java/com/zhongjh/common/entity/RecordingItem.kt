package com.zhongjh.common.entity

import android.os.Parcel
import android.os.Parcelable

/**
 * 音频文件的存储
 * @author zhongjh
 */
class RecordingItem : LocalFile, Parcelable {

    /**
     * 网址
     */
    var url: String? = null

    constructor() : super()

    constructor(input: Parcel) : super(input) {
        url = input.readString()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeString(url)
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