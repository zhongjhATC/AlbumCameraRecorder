package com.zhongjh.imageedit.core

/**
 * 图像处理中的视图方位控制接口
 * 定义了控制图像视图位置、旋转、缩放等基本操作的方法
 *
 * 该接口模仿Android View的部分核心功能，为图像编辑框架提供统一的视图控制能力
 * 主要用于贴纸、文字等可交互元素的位置和变形控制
 *
 * @author zhongjh
 * @date 2025-10-20
 */
interface ImageViewPortrait {
    /**
     * 获取当前旋转角度
     * 表示视图绕中心点旋转的角度，单位为度
     */
    var stickerRotation: Float

    /**
     * 获取旋转或缩放的中心点x坐标
     * 表示视图进行旋转或缩放操作时的中心点x坐标位置
     */
    val stickerPivotX: Float

    /**
     * 获取旋转或缩放的中心点y坐标
     * 表示视图进行旋转或缩放操作时的中心点y坐标位置
     */
    val stickerPivotY: Float

    /**
     * 获取视图左上角相对于父视图的x坐标
     * 表示视图在父容器中的水平位置
     */
    var stickerX: Float


    /**
     * 设置视图左上角相对于父视图的y坐标
     * 用于改变视图在父容器中的垂直位置
     */
    var stickerY: Float

    /**
     * 获取当前缩放比例
     * 表示视图当前的缩放系数，1.0表示原始大小
     */
    var stickerScale: Float

    /**
     * 在当前缩放比例的基础上叠加新的缩放值
     * 用于实现连续缩放操作，如手势缩放
     */
    fun addStickerScale(scale: Float)
}
