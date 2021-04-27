package com.zhongjh.imageedit.core;

/**
 *
 * @author felix
 * @date 2017/11/16 下午5:49
 */

public interface ImageViewPortrait {

    int getWidth();

    int getHeight();

    float getScaleX();

    float getScaleY();

    float getRotation();

    float getPivotX();

    float getPivotY();

    float getX();

    float getY();

    void setX(float x);

    void setY(float y);

    void setRotation(float rotate);

    void setScaleX(float scaleX);

    void setScaleY(float scaleY);

    float getScale();

    void setScale(float scale);

    void addScale(float scale);
}
