package com.zhongjh.displaymedia.entity

import android.graphics.drawable.Drawable
import com.zhongjh.displaymedia.engine.ImageEngine
import kotlin.properties.Delegates

/**
 * 图片适配器的构造函数实体
 * 因为构造函数参数太多，所以单独用一个实体封装
 *
 * @author zhongjh
 * @date 2022/9/1
 */
class PhotoAdapterEntity {

    /**
     * 兼容各种图片加载库
     */
    var imageEngine by Delegates.notNull<ImageEngine>()

    /**
     * placeholder
     */
    var placeholder by Delegates.notNull<Drawable>()

    /**
     * 是否操作
     */
    var isOperation by Delegates.notNull<Boolean>()

    /**
     * 最多显示多少个图片/视频/语音
     */
    var maxMediaCount by Delegates.notNull<Int>()

    /**
     * 遮罩层
     */
    var masking by Delegates.notNull<Masking>()

    /**
     * 删除图标的颜色
     */
    var deleteColor by Delegates.notNull<Int>()

    /**
     * 删除图标的图片
     */
    var deleteImage: Drawable? = null

    /**
     * 添加的图片资源
     */
    var addDrawable: Drawable? = null
}