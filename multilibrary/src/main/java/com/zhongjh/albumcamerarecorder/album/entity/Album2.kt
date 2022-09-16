package com.zhongjh.albumcamerarecorder.album.entity

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 专辑
 *
 * @param
 *
 * @author zhongjh
 * @date 2022/9/16
 */
@Parcelize
class Album2 internal constructor(
    var id: String,
    var path: String,
    var displayName: String,
    var count: Long
) : Parcelable