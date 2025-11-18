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
     * 是否进行上传动作
     */
    var isUploading = false

    /**
     * 进度
     */
    var progress: Int = 0

    /**
     * 该数据是否添加
     */
    var isAdd: Boolean = false

    constructor() : super()

    constructor(parcel: Parcel) : super(parcel) {
        isUploading = parcel.readByte() != 0.toByte()
        progress = parcel.readInt()
    }

    constructor(localMedia: LocalMedia) : super(localMedia)

    constructor(mimeType: String) {
        this.mimeType = mimeType
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeByte(if (isUploading) 1 else 0)
        parcel.writeInt(progress)
        parcel.writeByte(if (isAdd) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GridMedia> {

        override fun createFromParcel(parcel: Parcel): GridMedia {
            return GridMedia(parcel)
        }

        override fun newArray(size: Int): Array<GridMedia?> {
            return arrayOfNulls(size)
        }
    }

    /**
     * 深度拷贝
     */
    fun copyGridMedia(gridMedia: GridMedia) {
        super.copyLocalMedia(gridMedia)
        isUploading = gridMedia.isUploading
        progress = gridMedia.progress
        isAdd = gridMedia.isAdd
    }

}