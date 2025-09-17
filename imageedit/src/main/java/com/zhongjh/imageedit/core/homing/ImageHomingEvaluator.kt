package com.zhongjh.imageedit.core.homing

import android.animation.TypeEvaluator

/**
 * 用来处理放大-旋转的实体
 * @author zhongjh
 * @date 2017/11/28 下午4:13
 */
class ImageHomingEvaluator : TypeEvaluator<ImageHoming> {
    private var homing: ImageHoming? = null

    override fun evaluate(fraction: Float, startValue: ImageHoming, endValue: ImageHoming): ImageHoming {
        val x = startValue.x + fraction * (endValue.x - startValue.x)
        val y = startValue.y + fraction * (endValue.y - startValue.y)
        val scale = startValue.scale + fraction * (endValue.scale - startValue.scale)
        val rotate = startValue.rotate + fraction * (endValue.rotate - startValue.rotate)

        homing?.set(x, y, scale, rotate) ?: let {
            homing = ImageHoming(x, y, scale, rotate)
        }

        return homing!!
    }
}
