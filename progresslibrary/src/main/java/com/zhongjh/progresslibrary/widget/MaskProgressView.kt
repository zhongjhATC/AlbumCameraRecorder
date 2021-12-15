package com.zhongjh.progresslibrary.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatImageView
import com.zhongjh.common.utils.DisplayMetricsUtils.dip2px
import com.zhongjh.progresslibrary.R

/**
 * 用于图片、视频的加载进度的view
 * <p>
 * 可以考虑这个：https://github.com/dudu90/FreshDownloadView
 *
 * @author zhongjh
 * @date 2018/10/16
 */
class MaskProgressView : AppCompatImageView {

    companion object {
        private val TAG = MaskProgressView::class.java.simpleName

        /**
         * 最大进度值
         */
        private const val MAX_PROGRESS = 100
    }

    /**
     * view 宽
     */
    private var viewWidth = 0

    /**
     * view 高
     */
    private var viewHeight = 0

    /**
     * 遮罩矩形
     */
    private val rect: Rect = Rect()

    /**
     * 遮罩层画笔
     */
    private val maskingPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * 显示在遮罩层的字体画笔
     */
    private val textPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * 设置进度
     */
    private var percentage = 0

    /**
     * 设置文字进度
     */
    private var percentageTxt = 0

    /**
     * 字体的x位置
     */
    private var centerX = 0

    /**
     * 字体的y位置
     */
    private var centerY = 0

    // region 属性

    /**
     * 遮罩颜色，默认用主颜色
     */
    var maskingColor = 0
        set(value) {
            field = value
            maskingPaint.color = maskingColor
        }

    /**
     * 显示在遮罩层的字体大小
     */
    var textSize = 0
        set(value) {
            field = value
            textPaint.textSize = dip2px(textSize.toFloat()).toFloat()
        }

    /**
     * 显示在遮罩层的字体颜色
     */
    var textColor = 0
        set(value) {
            field = value
            textPaint.color = textColor
        }

    /**
     * 加载中的文字
     */
    var textString = resources.getString(R.string.z_progress_on_the_cross)
        set(value) {
            if (!TextUtils.isEmpty(value)) {
                field = value
            }
        }

    // endregion 属性

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        maskingPaint.style = Paint.Style.FILL
        maskingPaint.color = maskingColor

        textPaint.textSize = textSize.toFloat()
        textPaint.color = textColor
    }

    /**
     * 设置进度
     *
     * @param percentage 进度值
     */
    fun setPercentage(percentage: Int) {
        if (percentage in 1..MAX_PROGRESS) {
            this.percentage = 100 - percentage
            this.percentageTxt = percentage
            Log.d(TAG, "setPercentage: $percentage")
            // 重画view
            invalidate()
        }
    }

    /**
     * 重置进度
     */
    fun reset() {
        percentage = 0
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(measureSize(widthMeasureSpec), measureSize(heightMeasureSpec))
    }

    /**
     * 每次绘画
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (percentage in 1 until MAX_PROGRESS) {
            // 设置顶部，假设高度70 * 0.1 / 100
            rect.top = height * percentage / MAX_PROGRESS
            // 绘制图片遮罩
            canvas.drawRect(rect, maskingPaint)
            if (centerX == 0) {
                // 测量文字的长度
                val textLength = textPaint.measureText(textString).toInt()
                val fontMetrics = textPaint.fontMetrics
                // 获取文字的高度
                val textHeight = (fontMetrics.descent - fontMetrics.ascent).toInt()
                // 计算x轴居中的坐标
                centerX = (width - textLength) / 2
                centerY = ((height + textHeight) / 2 - fontMetrics.descent).toInt()
            }
            // 画：图片上传中
            canvas.drawText(textString, centerX.toFloat(), centerY.toFloat(), textPaint)
            // 画：百分比进度
            val percentageText = "$percentageTxt%"
            val percentageTextLength = textPaint.measureText(percentageText).toInt()
            canvas.drawText(percentageText, (width - percentageTextLength) / 2f, (height * 0.75).toInt().toFloat(), textPaint)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        //获取view的宽高
        viewWidth = width
        viewHeight = height
        // 每次改变size的时候，修改画布
        rect.left = 0
        rect.top = 0
        rect.right = viewWidth
        rect.bottom = viewHeight
    }

    /**
     * 测量宽高模式
     *
     * @param measureSpecSize 宽高度
     * @return 返回大小
     */
    private fun measureSize(measureSpecSize: Int): Int {
        var size: Int
        val ints = measureSpec(measureSpecSize)
        // 判断模式
        if (ints[0] == MeasureSpec.EXACTLY) {
            // 如果当前模式是 当前的尺寸就是当前View应该取的尺寸
            size = ints[1]
        } else {
            size = dip2px(70f)
            if (ints[0] == MeasureSpec.AT_MOST) {
                // 如果当前模式是 当前尺寸是当前View能取的最大尺寸,就取最小的那个，70或者是最大尺寸
                size = size.coerceAtMost(ints[1])
            }
        }
        return size
    }

    /**
     * 获取父布局传递给子布局的布局要求、大小
     *
     * @param measureSpec 包含 宽或高的信息，还有其他有关信息，比如模式等
     * @return 返回大小和模式的数组
     */
    private fun measureSpec(measureSpec: Int): IntArray {
        val measure = IntArray(2)
        measure[0] = MeasureSpec.getMode(measureSpec)
        measure[1] = MeasureSpec.getSize(measureSpec)
        return measure
    }

}