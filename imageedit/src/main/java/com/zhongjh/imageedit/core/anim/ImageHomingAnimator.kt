package com.zhongjh.imageedit.core.anim

import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import com.zhongjh.imageedit.core.homing.ImageHoming
import com.zhongjh.imageedit.core.homing.ImageHomingEvaluator

/**
 * @author zhongjh
 * @date 2025/09/01
 */
class ImageHomingAnimator : ValueAnimator() {
    private var isRotate: Boolean = false

    private var mEvaluator: ImageHomingEvaluator? = null

    init {
        interpolator = AccelerateDecelerateInterpolator()
    }

    override fun setObjectValues(vararg values: Any) {
        super.setObjectValues(*values)
        if (mEvaluator == null) {
            mEvaluator = ImageHomingEvaluator()
        }
        setEvaluator(mEvaluator)
    }

    fun setHomingValues(sHoming: ImageHoming, eHoming: ImageHoming) {
        setObjectValues(sHoming, eHoming)
        isRotate = ImageHoming.isRotate(sHoming, eHoming)
    }
}
