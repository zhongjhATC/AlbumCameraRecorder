package com.zhongjh.multimedia.camera.entity

import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 添加时间水印提前配置的属性
 */
class OverlayEffectEntity {

    /**
     * 字体Paint
     */
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        color = Color.WHITE
        textSize = 36f
        style = Paint.Style.FILL
        isFilterBitmap = false // 保持低版本兼容性
        // 添加文字抗锯齿优化
        isAntiAlias = true
        textSkewX = -0.2f // 轻微倾斜文字提升视觉效果（可选）
    }

    /**
     * 固定参数缓存
     */
    val marginSize = 50F

    /**
     * 时间文本，用于提前计算
     */
    val sampleTimeText = "2024-05-20 23:59:59"

    /**
     * 时间文本宽度固定（格式固定），提前计算一次
     */
    val fixedTextWidth = textPaint.measureText(sampleTimeText)

    /**
     * 时间文本高度固定（字体大小固定），提前计算一次
     */
    val fixedTextHeight = textPaint.textSize

    /**
     * 复用矩阵对象（避免频繁GC）
     * 传感器坐标到UI坐标的矩阵，用于转换时间文本的位置
     */
    val cachedSensorToUi = Matrix()

    /**
     * 复用矩阵对象（避免频繁GC）
     * UI坐标到传感器坐标的矩阵，用于转换点击事件的坐标
     */
    val cachedUiToSensor = Matrix()

    /**
     * 复用矩阵对象（避免频繁GC）
     * 临时矩阵，用于矩阵计算
     */
    val tempMatrix = Matrix()

    /**
     * 固定文字宽度，无需每次测量
     */
    val textWidth = fixedTextWidth + marginSize

    /**
     * 缓存绘制X坐标，避免重复计算
     */
    var cachedDrawX = 0F
    /**
     * 缓存绘制Y坐标，避免重复计算
     */
    var cachedDrawY = 0F

    /**
     * 时间格式化工具
     */
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    /**
     * 复用的日期对象
     */
    val date = Date()

    /**
     * 上次更新时间戳
     */
    var lastTimeUpdateTime = 0L

    /**
     * 当前缓存的时间文本
     */
    var cachedTimeText = ""


}