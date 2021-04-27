package com.zhongjh.imageedit.core.anim;

import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.zhongjh.imageedit.core.homing.ImageHoming;
import com.zhongjh.imageedit.core.homing.ImageHomingEvaluator;

/**
 *
 * @author felix
 * @date 2017/11/28 下午12:54
 */
public class ImageHomingAnimator extends ValueAnimator {

    private boolean isRotate = false;

    private ImageHomingEvaluator mEvaluator;

    public ImageHomingAnimator() {
        setInterpolator(new AccelerateDecelerateInterpolator());
    }

    @Override
    public void setObjectValues(Object... values) {
        super.setObjectValues(values);
        if (mEvaluator == null) {
            mEvaluator = new ImageHomingEvaluator();
        }
        setEvaluator(mEvaluator);
    }

    public void setHomingValues(ImageHoming sHoming, ImageHoming eHoming) {
        setObjectValues(sHoming, eHoming);
        isRotate = ImageHoming.isRotate(sHoming, eHoming);
    }

    public boolean isRotate() {
        return isRotate;
    }
}
