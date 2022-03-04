package com.zhongjh.albumcamerarecorder.album.entity

/**
 * 这是选择当前数据，如果最大值时展现相应信息
 * @author zhongjh
 * @date 2021/7/15
 */
class SelectedCountMessage {
    /**
     * 是否已经最大值
     */
    var isMaxSelectableReached = false

    /**
     * 类型 image、video、image_video
     */
    lateinit var type: String

    /**
     * 最大的数量
     */
    var maxCount = 0
}