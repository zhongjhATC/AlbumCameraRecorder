package com.zhongjh.multimedia.widget.progressbutton

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable

internal class CircularProgressDrawable(val size: Int, private val mStrokeWidth: Int, private val mStrokeColor: Int) : Drawable() {
    private var mSweepAngle = 0f
    private val mStartAngle = -90f
    private val mRectF by lazy {
        val index = mStrokeWidth / 2
        val rect = RectF(index.toFloat(), index.toFloat(), (size - index).toFloat(), (size - index).toFloat())
        rect
    }
    private val mPaint by lazy {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = mStrokeWidth.toFloat()
        paint.color = mStrokeColor
        paint
    }
    private var mPath = Path()

    fun setSweepAngle(sweepAngle: Float) {
        mSweepAngle = sweepAngle
    }

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        mPath.reset()
        mPath.addArc(mRectF, mStartAngle, mSweepAngle)
        mPath.offset(bounds.left.toFloat(), bounds.top.toFloat())
        canvas.drawPath(mPath, mPaint)
    }

    override fun setAlpha(alpha: Int) {
    }

    override fun setColorFilter(cf: ColorFilter?) {
    }

    @SuppressLint("WrongConstant")
    override fun getOpacity(): Int {
        return 1
    }
}
