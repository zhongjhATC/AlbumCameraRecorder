package com.zhongjh.common.enums

import androidx.annotation.IntDef
import com.zhongjh.common.enums.MultimediaTypes.Companion.PICTURE
import com.zhongjh.common.enums.MultimediaTypes.Companion.VIDEO
import com.zhongjh.common.enums.MultimediaTypes.Companion.AUDIO
import com.zhongjh.common.enums.MultimediaTypes.Companion.BLEND
import com.zhongjh.common.enums.MultimediaTypes.Companion.ADD

/**
 *
 * 多媒体类型，区分图片，视频，音频
 *
 * @author zhongjh
 * @date 2021/11/16
 */
// @IntDef 来限定常量不允许重复
@IntDef(
        PICTURE,
        VIDEO,
        AUDIO,
        BLEND,
        ADD
)
// 表示告诉编译器，该注解是源代码级别的，生成 class 文件的时候这个注解就被编译器自动去掉了。
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
annotation class MultimediaTypes {
    companion object {
        /**
         * 图片
         */
        const val PICTURE = 0

        /**
         * 视频
         */
        const val VIDEO = 1

        /**
         * 音频
         */
        const val AUDIO = 2

        /**
         * 混合类型
         */
        const val BLEND = 3

        /**
         * 添加的一个标记
         */
        const val ADD = -1
    }

}