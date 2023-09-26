package com.zhongjh.progresslibrary.entity

import android.os.Parcel
import android.os.Parcelable
import android.view.View
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.progresslibrary.widget.MaskProgressView
import com.zhongjh.progresslibrary.widget.PlayProgressView
import kotlinx.android.parcel.Parcelize

/**
 * 多媒体实体类,包含着view
 *
 * @author zhongjh
 * @date 2021/12/13
 */
class MultiMediaView : LocalMedia, Parcelable {



    /**
     * 绑定子view,包含其他所有控件（显示view,删除view）
     */
    lateinit var itemView: View

    /**
     * 绑定音频View
     */
    lateinit var playProgressView: PlayProgressView

    /**
     * 绑定子view: 用于显示图片、视频的view
     */
    lateinit var maskProgressView: MaskProgressView

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

    constructor() : super()

    constructor(parcel: Parcel) : super(parcel) {
        url = parcel.readString()
        isUploading = parcel.readByte() != 0.toByte()
    }

    constructor(localMedia: LocalMedia) : super(localMedia)

    constructor(mimeType: String) {
        this.mimeType = mimeType
    }

    /**
     * 给予进度，根据类型设置相应进度动作
     */
    fun setPercentage(percent: Int) {
        if (isImageOrGif() || isVideo()) {
            this.maskProgressView.setPercentage(percent)
        } else if (isAudio()) {
            // 隐藏显示音频的设置一系列动作
            playProgressView.mViewHolder.numberProgressBar.progress = percent
            if (percent == FULL_PERCENT) {
                playProgressView.audioUploadCompleted()
            }
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeLong(multiMediaId)
        parcel.writeString(url)
        parcel.writeByte(if (isUploading) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MultiMediaView> {

        private const val FULL_PERCENT = 100

        override fun createFromParcel(parcel: Parcel): MultiMediaView {
            return MultiMediaView(parcel)
        }

        override fun newArray(size: Int): Array<MultiMediaView?> {
            return arrayOfNulls(size)
        }
    }

}