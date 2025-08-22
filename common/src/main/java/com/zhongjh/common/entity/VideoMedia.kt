package com.zhongjh.common.entity

import android.os.Parcel
import android.os.Parcelable

/**
 * 音频实体
 */
class VideoMedia() : Parcelable {

    /**
     * 标记当前播放状态
     */
    private var isPlaying = false

    /**
     * 标记是否播放结束
     */
    private var isCompletion = true

    constructor(parcel: Parcel) : this() {
        isPlaying = parcel.readByte() != 0.toByte()
        isCompletion = parcel.readByte() != 0.toByte()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeByte(if (isPlaying) 1 else 0)
        dest.writeByte(if (isCompletion) 1 else 0)
    }

    companion object CREATOR : Parcelable.Creator<VideoMedia> {
        override fun createFromParcel(parcel: Parcel): VideoMedia {
            return VideoMedia(parcel)
        }

        override fun newArray(size: Int): Array<VideoMedia?> {
            return arrayOfNulls(size)
        }
    }

}