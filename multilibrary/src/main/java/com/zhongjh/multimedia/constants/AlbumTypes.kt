package com.zhongjh.multimedia.constants

import androidx.annotation.IntDef

/**
 * 相册类型
 *
 * @author zhongjh
 * @date 2019/1/18
 */
// @IntDef 限定常量不允许重复
@IntDef(AlbumTypes.CAMERA, AlbumTypes.FILE)
@Retention(AnnotationRetention.SOURCE)
annotation class AlbumTypes {
    companion object {
        /**
         * 拍照
         */
        const val CAMERA = 0

        /**
         * 图片/视频
         */
        const val FILE = 1
    }
}