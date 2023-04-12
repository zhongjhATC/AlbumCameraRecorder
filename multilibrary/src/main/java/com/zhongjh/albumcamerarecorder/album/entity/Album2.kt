package com.zhongjh.albumcamerarecorder.album.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 专辑
 *
 * @author zhongjh
 * @date 2022/9/16
 */
@Parcelize
class Album2 : Parcelable {
    /**
     * id 专辑id
     */
    var id: String = ""

    /**
     * 专辑的封面图path
     */
    var firstImagePath: String = ""

    /**
     * 专辑名称
     */
    var displayName: String = ""

    /**
     * 当前专辑下总共多少个文件
     */
    var count: Long = 0
}