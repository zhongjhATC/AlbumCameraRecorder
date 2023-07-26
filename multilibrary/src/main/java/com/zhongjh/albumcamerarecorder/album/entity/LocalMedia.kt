package com.zhongjh.albumcamerarecorder.album.entity;

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize;

/**
 * 多媒体文件
 *
 * @author zhongjh
 * @date 2023/7/26
 */
@Parcelize
class LocalMedia : Parcelable {

    /**
     * 文件id
     */
    var id: Long = 0

    /**
     * 路径
     */
    var path: String = ""

    /**
     * 真正的路径，但是不兼容AndroidQ
     */
    var realPath: String = ""

    /**
     * 原始路径
     */
    var originalPath: String = ""

    /**
     * 压缩路径
     */
    var compressPath: String = ""

    /**
     * 裁剪后的路径
     */
    var cutPath: String = ""

    /**
     * 此字段仅在Android Q版本中返回
     * Android Q版本图像或视频路径
     */
    var androidQToPath: String = ""

    /**
     * 视频的持续时间
     */
    var duration: Long = 0

    /**
     * 如果是被选中
     */
    var isChecked: Boolean = false

    /**
     * 是否裁剪的
     */
    var isCut: Boolean = false

    /**
     * 列表中的索引
     */
    var position: Int = 0

    /**
     * 媒体号qq选择风格
     */
    var num: Int = 0

    /**
     * 媒体资源类型
     */
    var mimeType: String = ""

    /**
     * 画廊选择模式
     */
    var chooseModel: Int = 0

    /**
     * 是否被压缩
     */
    var isCompressed: Boolean = false

    /**
     * 图像或视频宽度
     * 如果出现0，开发人员需要额外处理
     */
    var width: Int = 0

    /**
     * 图像或视频宽度
     * 如果出现0，开发人员需要额外处理
     */
    var height: Int = 0

    /**
     * 裁剪图片的宽度
     */
    var cropImageWidth: Int = 0

    /**
     * 裁剪图片的高度
     */
    var cropImageHeight: Int = 0

    /**
     * 裁剪比例X
     */
    var cropOffsetX: Int = 0

    /**
     * 裁剪比例Y
     */
    var cropOffsetY: Int = 0

    /**
     * 裁剪纵横比
     */
    var cropResultAspectRatio: Float = 0F

    /**
     * 文件大小
     */
    var size: Long = 0

    /**
     * 是否显示原始图像
     */
    var isOriginal: Boolean = false

    /**
     * 文件名称
     */
    var fileName: String = ""

    /**
     * 父文件夹名称
     */
    var parentFolderName: String = ""

    /**
     * 专辑ID
     */
    var bucketId: Long = -1

    /**
     * 图像是否被编辑过
     * 内部使用
     */
    var isEditorImage: Boolean = false

    /**
     * 文件创建时间
     */
    var dateAddedTime: Long = 0

}
