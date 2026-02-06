package com.zhongjh.imageedit.core;

/**
 * @author felix
 * @date 2017/11/16 下午5:49
 * 这是模仿view的相关接口
 */
public interface ImageViewPortrait {

    /**
     * 获取当前旋转角度
     *
     * @return 当前旋转角度
     */
    float getRotation();

    /**
     * 获取x中心
     *
     * @return 围绕View旋转或缩放的x坐标
     */
    float getPivotX();

    /**
     * 获取y中心
     *
     * @return 围绕View旋转或缩放的y坐标
     */
    float getPivotY();

    /**
     * 获取距离父view的x坐标
     *
     * @return x坐标
     */
    float getX();

    /**
     * 获取距离父view的y坐标
     *
     * @return y坐标
     */
    float getY();

    /**
     * 设置view处于x坐标
     *
     * @param x x坐标
     */
    void setX(float x);

    /**
     * 设置view处于y坐标
     *
     * @param y x坐标
     */
    void setY(float y);

    /**
     * 设置view旋转
     *
     * @param rotate 旋转角度
     */
    void setRotation(float rotate);

    /**
     * 获取当前比例
     *
     * @return 比例
     */
    float getScale();

    /**
     * 设置当前比例
     *
     * @param scale 比例
     */
    void setScale(float scale);

    /**
     * 基础上添加比例
     *
     * @param scale 比例
     */
    void addScale(float scale);
}
