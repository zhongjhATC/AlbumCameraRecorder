package com.zhongjh.imageedit.core

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path

/**
 * 图像画笔类，负责处理图像编辑中的涂鸦和马赛克绘制功能
 * 是图像编辑器中实现自由绘制的核心组件
 * 存储绘制路径、颜色、宽度和模式等信息，并提供相应的绘制和变换方法
 * 支持涂鸦和马赛克两种主要绘制模式，可以通过变换矩阵实现路径的平移、缩放和旋转
 *
 * @param path 新的绘制路径对象
 * @param mode 新的图像模式（涂鸦或马赛克）
 * @param color 新的笔刷颜色值
 * @param width 新的笔刷宽度值
 *
 * @author zhongjh
 * @date 2025/10/15
 */
open class ImagePen @JvmOverloads constructor(
    var path: Path = Path(), var mode: ImageMode = ImageMode.DOODLE, var color: Int = Color.RED, var width: Float = BASE_MOSAIC_WIDTH
) {
    /**
     * 默认构造函数，创建一个新的空路径画笔，默认使用涂鸦模式
     * 适用于需要从头开始创建绘制路径的场景
     */
    init {
        // 马赛克模式需要特殊的填充类型，确保马赛克区域正确填充
        if (mode == ImageMode.MOSAIC) {
            path.fillType = Path.FillType.EVEN_ODD
        }
    }

    /**
     * 绘制涂鸦效果
     * 只有当模式为涂鸦模式时才会执行绘制操作
     *
     * @param canvas 画布对象，用于绘制操作
     * @param paint 画笔对象，设置绘制参数
     */
    fun onDrawDoodle(canvas: Canvas, paint: Paint) {
        if (mode == ImageMode.DOODLE) {
            // 设置画笔颜色为当前笔刷颜色
            paint.color = color
            // 设置画笔宽度为基础涂鸦宽度
            paint.strokeWidth = BASE_DOODLE_WIDTH
            // 使用画布绘制完整的涂鸦路径
            canvas.drawPath(path, paint)
        }
    }

    /**
     * 绘制马赛克效果
     * 只有当模式为马赛克模式时才会执行绘制操作
     *
     * @param canvas 画布对象，用于绘制操作
     * @param paint 画笔对象，设置绘制参数
     */
    fun onDrawMosaic(canvas: Canvas, paint: Paint) {
        if (mode == ImageMode.MOSAIC) {
            // 设置画笔宽度为当前笔刷宽度，影响马赛克块的大小
            paint.strokeWidth = width
            // 使用画布绘制马赛克区域路径
            canvas.drawPath(path, paint)
        }
    }

    /**
     * 对绘制路径应用变换矩阵
     * 可用于实现路径的平移、缩放、旋转等变换操作
     * 在图像缩放、旋转或平移时，需要调用此方法保持绘制内容与图像同步
     *
     * @param matrix 变换矩阵对象，包含要应用的变换操作
     */
    fun transform(matrix: Matrix) {
        // 对绘制路径应用变换矩阵，实现路径的几何变换
        path.transform(matrix)
    }

    companion object {
        /**
         * 涂鸦模式的基础画笔宽度
         */
        const val BASE_DOODLE_WIDTH: Float = 10f

        /**
         * 马赛克模式的基础画笔宽度
         */
        const val BASE_MOSAIC_WIDTH: Float = 72f
    }
}
