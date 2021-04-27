package com.zhongjh.imageedit.core.homing;

import android.animation.TypeEvaluator;

/**
 * @author felix
 * @date 2017/11/28 下午4:13
 */
public class ImageHomingEvaluator implements TypeEvaluator<ImageHoming> {

    private ImageHoming homing;

    public ImageHomingEvaluator() {

    }

    @Override
    public ImageHoming evaluate(float fraction, ImageHoming startValue, ImageHoming endValue) {
        float x = startValue.x + fraction * (endValue.x - startValue.x);
        float y = startValue.y + fraction * (endValue.y - startValue.y);
        float scale = startValue.scale + fraction * (endValue.scale - startValue.scale);
        float rotate = startValue.rotate + fraction * (endValue.rotate - startValue.rotate);

        if (homing == null) {
            homing = new ImageHoming(x, y, scale, rotate);
        } else {
            homing.set(x, y, scale, rotate);
        }

        return homing;
    }
}
