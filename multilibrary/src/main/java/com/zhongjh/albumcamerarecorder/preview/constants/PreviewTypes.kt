package com.zhongjh.albumcamerarecorder.preview.constants

import android.widget.GridView
import androidx.annotation.IntDef

/**
 * 预览界面类型：
 * 1. 从相册界面以Activity形式打开
 * 2. 从相册界面以Fragment形式打开
 * 3. 从拍摄界面打开
 * 4. 从九宫格界面打开
 * 5. 从其他界面打开
 *
 * @author zhongjh
 * @date 2025/5/26
 */
// @IntDef 限定常量不允许重复
@IntDef(
    PreviewTypes.ALBUM_ACTIVITY,
    PreviewTypes.ALBUM_FRAGMENT,
    PreviewTypes.CAMERA,
    PreviewTypes.GRID,
    PreviewTypes.THIRD_PARTY
)
@Retention(AnnotationRetention.SOURCE)
annotation class PreviewTypes {
    companion object {

        /**
         * 从相册界面以Activity形式打开
         */
        const val ALBUM_ACTIVITY = 0

        /**
         * 从相册界面以Fragment形式打开
         */
        const val ALBUM_FRAGMENT = 1

        /**
         * 从拍摄界面打开
         */
        const val CAMERA = 2

        /**
         * 从九宫格界面打开
         */
        const val GRID = 3

        /**
         * 从其他界面打开
         */
        const val THIRD_PARTY = 9
    }
}