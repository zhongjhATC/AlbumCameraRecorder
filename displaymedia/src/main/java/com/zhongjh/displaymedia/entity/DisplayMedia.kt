package com.zhongjh.displaymedia.entity

import android.os.Parcel
import android.os.Parcelable
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.displaymedia.widget.AudioProgressView

/**
 * 多媒体实体类,主要用于展示和显示进度
 *
 * @author zhongjh
 * @date 2021/12/13
 */
class DisplayMedia : LocalMedia, Parcelable {

    /**
     * 用于区分，因为九宫数据是允许选择重复的
     */
    var displayMediaId: Long = 0

    /**
     * 在线网址
     */
    var url: String? = null

    /**
     * 是否进行上传动作
     */
    var isUploading = false

    /**
     * 进度
     */
    var progress: Int = 0

    constructor() : super()

    constructor(parcel: Parcel) : super(parcel) {
        url = parcel.readString()
        isUploading = parcel.readByte() != 0.toByte()
        progress = parcel.readInt()
    }

    constructor(localMedia: LocalMedia) : super(localMedia)

    constructor(mimeType: String) {
        this.mimeType = mimeType
    }

    /**
     * 给予进度
     *
     * @param percent 进度数值
     * @param audioProgressView
     */
    fun setPercentage(audioProgressView: AudioProgressView, percent: Int) {
        // 隐藏显示音频的设置一系列动作
        audioProgressView.mViewHolder.numberProgressBar.progress = percent
        if (percent == FULL_PERCENT) {
            audioProgressView.audioUploadCompleted()
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeLong(displayMediaId)
        parcel.writeString(url)
        parcel.writeByte(if (isUploading) 1 else 0)
        parcel.writeInt(progress)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DisplayMedia> {

        const val FULL_PERCENT = 100

        override fun createFromParcel(parcel: Parcel): DisplayMedia {
            return DisplayMedia(parcel)
        }

        override fun newArray(size: Int): Array<DisplayMedia?> {
            return arrayOfNulls(size)
        }
    }

}