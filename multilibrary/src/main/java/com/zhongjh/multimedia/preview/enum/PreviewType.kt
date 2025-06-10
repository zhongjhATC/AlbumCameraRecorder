package com.zhongjh.multimedia.preview.enum

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
enum class PreviewType {

    /**
     * 从相册界面以Activity形式打开
     */
    ALBUM_ACTIVITY,

    /**
     * 从相册界面以Fragment形式打开
     */
    ALBUM_FRAGMENT,

    /**
     * 从拍摄界面打开
     */
    CAMERA,

    /**
     * 从九宫格界面打开
     */
    GRID,

    /**
     * 从其他界面打开
     */
    THIRD_PARTY
}