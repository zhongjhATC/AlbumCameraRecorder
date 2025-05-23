package com.zhongjh.gridview.entity

import android.os.Parcel
import android.os.Parcelable
import com.zhongjh.common.utils.ThreadUtils

/**
 * 音频实体
 */
class VideoMedia() : Parcelable {

    /**
     * 标记当前播放状态
     */
    var isPlaying = false

    /**
     * 标记是否播放结束
     */
    var isCompletion = true

    /**
     * 播放音乐时，各自的线程
     */
    var task: ThreadUtils.Task<Boolean>? = null

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