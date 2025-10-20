package com.zhongjh.imageedit.core;

/**
 * 图像处理中的视图方位控制接口
 * 定义了控制图像视图位置、旋转、缩放等基本操作的方法
 * 
 * 该接口模仿Android View的部分核心功能，为图像编辑框架提供统一的视图控制能力
 * 主要用于贴纸、文字等可交互元素的位置和变形控制
 * 
 * @author felix
 * @date 2017/11/16 下午5:49
 */
public interface ImageViewPortrait {

    /**
     * 获取当前旋转角度
     * 表示视图绕中心点旋转的角度，单位为度
     *
     * @return 当前旋转角度，顺时针方向为正角度
     */
    float getRotation();

    /**
     * 获取旋转或缩放的中心点x坐标
     * 表示视图进行旋转或缩放操作时的中心点x坐标位置
     *
     * @return 围绕View旋转或缩放的x坐标
     */
    float getPivotX();

    /**
     * 获取旋转或缩放的中心点y坐标
     * 表示视图进行旋转或缩放操作时的中心点y坐标位置
     *
     * @return 围绕View旋转或缩放的y坐标
     */
    float getPivotY();

    /**
     * 获取视图左上角相对于父视图的x坐标
     * 表示视图在父容器中的水平位置
     *
     * @return 距离父view的x坐标
     */
    float getX();

    /**
     * 获取视图左上角相对于父视图的y坐标
     * 表示视图在父容器中的垂直位置
     *
     * @return 距离父view的y坐标
     */
    float getY();

    /**
     * 设置视图左上角相对于父视图的x坐标
     * 用于改变视图在父容器中的水平位置
     *
     * @param x 新的x坐标位置
     */
    void setX(float x);

    /**
     * 设置视图左上角相对于父视图的y坐标
     * 用于改变视图在父容器中的垂直位置
     *
     * @param y 新的y坐标位置
     */
    void setY(float y);

    /**
     * 设置视图的旋转角度
     * 使视图绕中心点顺时针旋转指定角度
     *
     * @param rotate 旋转角度，单位为度
     */
    void setRotation(float rotate);

    /**
     * 获取当前缩放比例
     * 表示视图当前的缩放系数，1.0表示原始大小
     *
     * @return 当前缩放比例
     */
    float getScale();

    /**
     * 设置视图的缩放比例
     * 直接设置视图的缩放系数，1.0表示原始大小，大于1.0表示放大，小于1.0表示缩小
     *
     * @param scale 新的缩放比例
     */
    void setScale(float scale);

    /**
     * 在当前缩放比例的基础上叠加新的缩放值
     * 用于实现连续缩放操作，如手势缩放
     *
     * @param scale 要叠加的缩放比例增量
     */
    void addScale(float scale);
}
