package com.zhongjh.common.entity

/**
 * 多媒体参数类
 *
 * @author zhongjh
 * @date 2022/2/08
 */
class MediaExtraInfo {
    var width: Int = 0
    var height: Int = 0
    var duration: Long = 0
    /**
     * 具体类型，jpg,png,mp3等等
     * {@link MimeType }
     */
    var mimeType: String? = null
}