package com.zhongjh.displaymedia.entity

import android.os.Parcel
import android.os.Parcelable
import com.zhongjh.common.entity.LocalMedia

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

    /**
     * 音频类
     */
    var videoMedia: VideoMedia? = null

    constructor() : super()

    constructor(parcel: Parcel) : super(parcel) {
        url = parcel.readString()
        isUploading = parcel.readByte() != 0.toByte()
        progress = parcel.readInt()
        videoMedia = parcel.readParcelable(VideoMedia::class.java.classLoader)
    }

    constructor(localMedia: LocalMedia) : super(localMedia)

    constructor(mimeType: String) {
        this.mimeType = mimeType
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeLong(displayMediaId)
        parcel.writeString(url)
        parcel.writeByte(if (isUploading) 1 else 0)
        parcel.writeInt(progress)
        videoMedia?.let {
            parcel.writeParcelable(it, 0)
        }
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