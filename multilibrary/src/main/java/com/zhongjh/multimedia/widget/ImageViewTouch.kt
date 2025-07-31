package com.zhongjh.multimedia.widget

import android.content.Context
import android.util.AttributeSet
import com.github.chrisbanes.photoview.PhotoView

/**
 * 独立出来继承单纯是为了后期如果更换第三方库的时候，别人使用的话是不会也跟着修改
 * @author zhongjh
 * @date 2022/8/18
 */
class ImageViewTouch : PhotoView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attr: AttributeSet) : super(context, attr)

    constructor(context: Context, attr: AttributeSet, defStyle: Int) : super(context, attr, defStyle)
}
