package com.zhongjh.common.enums

import androidx.annotation.IntDef

/**
 * 拍摄界面标记的两个类型
 *
 * @author zhongjh
 * @date 2021/12/22
 */
@IntDef(MediaType.TYPE_PICTURE, MediaType.TYPE_VIDEO, MediaType.TYPE_AUDIO)
@Retention(AnnotationRetention.SOURCE)
annotation class MediaType {
    companion object {
        /**
         * 图片
         */
        const val TYPE_PICTURE: Int = 0x001

        /**
         * 视频
         */
        const val TYPE_VIDEO: Int = 0x002

        /**
         * 音频
         */
        const val TYPE_AUDIO: Int = 0x003
    }
}
