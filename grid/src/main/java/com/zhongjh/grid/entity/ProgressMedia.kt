package com.zhongjh.grid.entity

import android.os.Parcel
import android.os.Parcelable
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.grid.apapter.PhotoAdapter
import com.zhongjh.grid.widget.PlayProgressView

/**
 * 多媒体实体类,包含着view
 *
 * @author zhongjh
 * @date 2021/12/13
 */
class ProgressMedia : LocalMedia, Parcelable {

    /**
     * 用于区分，因为九宫数据是允许选择重复的
     */
    var multiMediaId: Long = 0

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
     * @param playProgressView
     */
    fun setPercentage(playProgressView: PlayProgressView, percent: Int) {
        // 隐藏显示音频的设置一系列动作
        playProgressView.mViewHolder.numberProgressBar.progress = percent
        if (percent == FULL_PERCENT) {
            playProgressView.audioUploadCompleted()
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeLong(multiMediaId)
        parcel.writeString(url)
        parcel.writeByte(if (isUploading) 1 else 0)
        parcel.writeInt(progress)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProgressMedia> {

        private const val FULL_PERCENT = 100

        override fun createFromParcel(parcel: Parcel): ProgressMedia {
            return ProgressMedia(parcel)
        }

        override fun newArray(size: Int): Array<ProgressMedia?> {
            return arrayOfNulls(size)
        }
    }

}