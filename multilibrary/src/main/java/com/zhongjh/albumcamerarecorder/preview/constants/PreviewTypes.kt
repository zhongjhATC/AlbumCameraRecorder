package com.zhongjh.albumcamerarecorder.preview.constants

import android.widget.GridView
import androidx.annotation.IntDef

/**
 * 预览界面类型：
 * 1. 从相册界面打开的
 * 2. 从拍摄界面打开的
 * 3. 从九宫格界面打开的
 * 4. 从其他界面打开的
 *
 * @author zhongjh
 * @date 2025/5/26
 */
// @IntDef 限定常量不允许重复
@IntDef(PreviewTypes.ALBUM, PreviewTypes.CAMERA, PreviewTypes.GRID, PreviewTypes.THIRD_PARTY)
@Retention(AnnotationRetention.SOURCE)
annotation class PreviewTypes {
    companion object {
        /**
         * 从相册界面打开的
         */
        const val ALBUM = 0

        /**
         * 从拍摄界面打开的
         */
        const val CAMERA = 1

        /**
         * 从九宫格界面打开的
         */
        const val GRID = 2

        /**
         * 从其他界面打开的
         */
        const val THIRD_PARTY = 9
    }
}