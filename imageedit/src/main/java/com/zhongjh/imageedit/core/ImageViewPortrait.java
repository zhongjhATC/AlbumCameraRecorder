package com.zhongjh.imageedit.core;

/**
 *
 * @author felix
 * @date 2017/11/16 下午5:49
 * 这是模仿view的相关接口,因为有些地方只
 */
public interface ImageViewPortrait {

    /**
     * 获取当前旋转角度
     * @return 当前旋转角度
     */
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
