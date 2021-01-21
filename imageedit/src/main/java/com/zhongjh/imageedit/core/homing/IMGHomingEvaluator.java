package com.zhongjh.imageedit.core.homing;

import android.animation.TypeEvaluator;

/**
 * Created by felix on 2017/11/28 下午4:13.
 */

public class IMGHomingEvaluator implements TypeEvaluator<IMGHoming> {

    private IMGHoming homing;

    public IMGHomingEvaluator() {

    }

    public IMGHomingEvaluator(IMGHoming homing) {
        this.homing = homing;
    }

    @Override
    public IMGHoming evaluate(float fraction, IMGHoming startValue, IMGHoming endValue) {
        float x = startValue.x + fraction * (endValue.x - startValue.x);
        float y = startValue.y + fraction * (endValue.y - startValue.y);
        float scale = startValue.scale + fraction * (endValue.scale - startValue.scale);
        float rotate = startValue.rotate + fraction * (endValue.rotate - startValue.rotate);

        if (homing == null) {
            homing = new IMGHoming(x, y, scale, rotate);
        } else {
            homing.set(x, y, scale, rotate);
        }

        return homing;
    }
}
