package com.zhongjh.imageedit.core;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * 图像画笔类，负责处理图像编辑中的涂鸦和马赛克绘制功能
 * 是图像编辑器中实现自由绘制的核心组件
 * 存储绘制路径、颜色、宽度和模式等信息，并提供相应的绘制和变换方法
 * 支持涂鸦和马赛克两种主要绘制模式，可以通过变换矩阵实现路径的平移、缩放和旋转
 * 
 * @author felix
 * @date 2017/11/22 下午6:13
 */

public class ImagePen {

    /**
     * 绘制路径，记录笔刷移动的轨迹，用于绘制线条或区域
     */
    protected Path path;

    /**
     * 笔刷颜色，用于绘制涂鸦线条或填充马赛克区域
     */
    private int color;

    /**
     * 笔刷宽度，影响绘制线条的粗细或马赛克块的大小
     */
    private float width;

    /**
     * 图像模式，表示当前是涂鸦模式还是马赛克模式
     */
    private ImageMode mode;

    /**
     * 涂鸦模式的基础画笔宽度
     */
    public static final float BASE_DOODLE_WIDTH = 10f;

    /**
     * 马赛克模式的基础画笔宽度
     */
    public static final float BASE_MOSAIC_WIDTH = 72f;

    /**
     * 默认构造函数，创建一个新的空路径画笔，默认使用涂鸦模式
     * 适用于需要从头开始创建绘制路径的场景
     */
    public ImagePen() {
        this(new Path());
    }

    /**
     * 构造函数，使用指定的路径创建画笔，默认使用涂鸦模式和红色
     * 适用于已有预设路径需要进行绘制的场景
     * 
     * @param path 绘制路径对象
     */
    public ImagePen(Path path) {
        this(path, ImageMode.DOODLE);
    }

    /**
     * 构造函数，使用指定的路径和模式创建画笔，默认使用红色
     * 适用于需要指定绘制模式的场景
     * 
     * @param path 绘制路径对象
     * @param mode 图像模式（涂鸦或马赛克）
     */
    public ImagePen(Path path, ImageMode mode) {
        this(path, mode, Color.RED);
    }

    /**
     * 构造函数，使用指定的路径、模式和颜色创建画笔
     * 适用于需要自定义绘制颜色的场景
     * 
     * @param path 绘制路径对象
     * @param mode 图像模式（涂鸦或马赛克）
     * @param color 笔刷颜色
     */
    public ImagePen(Path path, ImageMode mode, int color) {
        this(path, mode, color, BASE_MOSAIC_WIDTH);
    }

    /**
     * 完整构造函数，使用指定的路径、模式、颜色和宽度创建画笔
     * 对于马赛克模式，会自动设置路径的填充类型为EVEN_ODD
     * 适用于需要完全自定义画笔参数的场景
     * 
     * @param path 绘制路径对象
     * @param mode 图像模式（涂鸦或马赛克）
     * @param color 笔刷颜色
     * @param width 笔刷宽度
     */
    public ImagePen(Path path, ImageMode mode, int color, float width) {
        this.path = path;
        this.mode = mode;
        this.color = color;
        this.width = width;
        // 马赛克模式需要特殊的填充类型，确保马赛克区域正确填充
        if (mode == ImageMode.MOSAIC) {
            path.setFillType(Path.FillType.EVEN_ODD);
        }
    }

    /**
     * 获取绘制路径
     * 
     * @return 当前的绘制路径对象
     */
    public Path getPath() {
        return path;
    }

    /**
     * 设置绘制路径
     * 
     * @param path 新的绘制路径对象
     */
    public void setPath(Path path) {
        this.path = path;
    }

    /**
     * 获取笔刷颜色
     * 
     * @return 当前的笔刷颜色值
     */
    public int getColor() {
        return color;
    }

    /**
     * 设置笔刷颜色
     * 
     * @param color 新的笔刷颜色值
     */
    public void setColor(int color) {
        this.color = color;
    }

    /**
     * 获取图像模式
     * 
     * @return 当前的图像模式（涂鸦或马赛克）
     */
    public ImageMode getMode() {
        return mode;
    }

    /**
     * 设置图像模式
     * 
     * @param mode 新的图像模式（涂鸦或马赛克）
     */
    public void setMode(ImageMode mode) {
        this.mode = mode;
    }

    /**
     * 设置笔刷宽度
     * 
     * @param width 新的笔刷宽度值
     */
    public void setWidth(float width) {
        this.width = width;
    }

    /**
     * 获取笔刷宽度
     * 
     * @return 当前的笔刷宽度值
     */
    public float getWidth() {
        return width;
    }

    /**
     * 绘制涂鸦效果
     * 只有当模式为涂鸦模式时才会执行绘制操作
     * 
     * @param canvas 画布对象，用于绘制操作
     * @param paint 画笔对象，设置绘制参数
     */
    public void onDrawDoodle(Canvas canvas, Paint paint) {
        if (mode == ImageMode.DOODLE) {
            // 设置画笔颜色为当前笔刷颜色
            paint.setColor(color);
            // 设置画笔宽度为基础涂鸦宽度
            paint.setStrokeWidth(BASE_DOODLE_WIDTH);
            // 使用画布绘制完整的涂鸦路径
            canvas.drawPath(path, paint);
        }
    }

    /**
     * 绘制马赛克效果
     * 只有当模式为马赛克模式时才会执行绘制操作
     * 
     * @param canvas 画布对象，用于绘制操作
     * @param paint 画笔对象，设置绘制参数
     */
    public void onDrawMosaic(Canvas canvas, Paint paint) {
        if (mode == ImageMode.MOSAIC) {
            // 设置画笔宽度为当前笔刷宽度，影响马赛克块的大小
            paint.setStrokeWidth(width);
            // 使用画布绘制马赛克区域路径
            canvas.drawPath(path, paint);
        }
    }

    /**
     * 对绘制路径应用变换矩阵
     * 可用于实现路径的平移、缩放、旋转等变换操作
     * 在图像缩放、旋转或平移时，需要调用此方法保持绘制内容与图像同步
     * 
     * @param matrix 变换矩阵对象，包含要应用的变换操作
     */
    public void transform(Matrix matrix) {
        // 对绘制路径应用变换矩阵，实现路径的几何变换
        path.transform(matrix);
    }
}
