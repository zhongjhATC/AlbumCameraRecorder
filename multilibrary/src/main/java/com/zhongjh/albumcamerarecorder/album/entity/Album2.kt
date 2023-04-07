package com.zhongjh.albumcamerarecorder.album.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 专辑
 *
 * @param id 专辑id
 * @param firstImagePath 专辑的封面图path
 * @param displayName 专辑名称
 * @param count 当前专辑下总共多少个文件
 *
 * @author zhongjh
 * @date 2022/9/16
 */
@Parcelize
class Album2 internal constructor(
    var id: String,
    var firstImagePath: String,
    var displayName: String,
    var count: Long
) : Parcelable