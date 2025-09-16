package com.zhongjh.imageedit.core.homing;

import android.animation.TypeEvaluator;

/**
 * 用来处理放大-旋转的实体
 * @author zhongjh
 * @date 2017/11/28 下午4:13
 */
public class ImageHomingEvaluator implements TypeEvaluator<ImageHoming> {

    private ImageHoming homing;

    @Override
    public ImageHoming evaluate(float fraction, ImageHoming startValue, ImageHoming endValue) {
        float x = startValue.getX() + fraction * (endValue.getX() - startValue.getX());
        float y = startValue.getY() + fraction * (endValue.getY() - startValue.getY());
        float scale = startValue.getScale() + fraction * (endValue.getScale() - startValue.getScale());
        float rotate = startValue.getRotate() + fraction * (endValue.getRotate() - startValue.getRotate());

        if (homing == null) {
            homing = new ImageHoming(x, y, scale, rotate);
        } else {
            homing.set(x, y, scale, rotate);
        }

        return homing;
    }
}
