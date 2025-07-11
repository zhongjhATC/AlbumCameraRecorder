package com.zhongjh.multimedia.album.entity

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable

/**
 * 相册下拉框样式
 */
class AlbumSpinnerStyle {
    var drawableUp: Drawable? = null
    var drawableDown: Drawable? = null
    /**
     * 默认图片
     */
    var placeholder: Drawable = ColorDrawable(Color.WHITE)
    var maxHeight = 0
}