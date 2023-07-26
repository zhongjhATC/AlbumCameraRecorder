package com.zhongjh.albumcamerarecorder.album.entity

/**
 * 封装多媒体文件集合的类
 *
 * @author zhongjh
 * @date 2023/7/26
 */
class MediaData {

    /**
     * 是否包含下一页
     */
    var isHasNextMore = false

    /**
     * 多媒体文件集合
     */
    var data: List<LocalMedia>? = null

}