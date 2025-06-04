package com.zhongjh.common.entity

import android.os.Parcel
import android.os.Parcelable

/**
 * 多媒体实体类,主要用于展示和显示进度
 *
 * @author zhongjh
 * @date 2021/12/13
 */
class GridMedia : LocalMedia, Parcelable {

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

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeString(url)
        parcel.writeByte(if (isUploading) 1 else 0)
        parcel.writeInt(progress)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GridMedia> {

        const val FULL_PERCENT = 100

        override fun createFromParcel(parcel: Parcel): GridMedia {
            return GridMedia(parcel)
        }

        override fun newArray(size: Int): Array<GridMedia?> {
            return arrayOfNulls(size)
        }
    }

}