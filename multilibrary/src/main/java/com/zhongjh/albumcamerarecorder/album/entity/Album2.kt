package com.zhongjh.albumcamerarecorder.album.entity

import android.os.Parcelable
import com.zhongjh.common.enums.MimeType
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
    var id: Long = 0

    /**
     * 专辑的封面图path
     */
    var firstImagePath: String = ""

    /**
     * 专辑的封面类型
     */
    var firstMimeType: String = ""

    /**
     * 专辑名称
     */
    var name: String = ""

    /**
     * 当前专辑下总共多少个文件
     */
    var count: Int = 0

    /**
     * 专辑下的图片、视频等数据
     */
    var data: List<LocalMedia> = ArrayList()

    /**
     * 是否选择
     */
    var isChecked = false

    /**
     * 类型
     */
    var type: Set<MimeType> = MimeType.ofAll()
}