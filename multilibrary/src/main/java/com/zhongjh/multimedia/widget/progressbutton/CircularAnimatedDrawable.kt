package com.zhongjh.multimedia.widget.progressbutton

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.util.Property
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator

internal class CircularAnimatedDrawable(color: Int, private val mBorderWidth: Float) : Drawable(), Animatable {
    private val fBounds = RectF()

    private val mAngleProperty: Property<CircularAnimatedDrawable, Float> = object : Property<CircularAnimatedDrawable, Float>(
        Float::class.java, "angle"
    ) {
        override fun get(circularAnimatedDrawable: CircularAnimatedDrawable): Float {
            return circularAnimatedDrawable.currentGlobalAngle
        }

        override fun set(circularAnimatedDrawable: CircularAnimatedDrawable, value: Float) {
            circularAnimatedDrawable.currentGlobalAngle = value
        }
    }

    private val mSweepProperty: Property<CircularAnimatedDrawable, Float> = object : Property<CircularAnimatedDrawable, Float>(Float::class.java, "arc") {
        override fun get(circularAnimatedDrawable: CircularAnimatedDrawable): Float {
            return circularAnimatedDrawable.currentSweepAngle
        }

        override fun set(circularAnimatedDrawable: CircularAnimatedDrawable, value: Float) {
            circularAnimatedDrawable.currentSweepAngle = value
        }
    }
    private var mObjectAnimatorSweep = ObjectAnimator.ofFloat(this, mSweepProperty, 360f - MIN_SWEEP_ANGLE * 2)
    private var mObjectAnimatorAngle = ObjectAnimator.ofFloat(this, mAngleProperty, 360f)
    private var mModeAppearing = false
    private val mPaint = Paint()
    private var mCurrentGlobalAngleOffset = 0f
    private var mCurrentGlobalAngle = 0f
    private var mCurrentSweepAngle = 0f
    private var mRunning = false

    override fun draw(canvas: Canvas) {
        var startAngle = mCurrentGlobalAngle - mCurrentGlobalAngleOffset
        var sweepAngle = mCurrentSweepAngle
        if (!mModeAppearing) {
            startAngle += sweepAngle
            sweepAngle = 360 - sweepAngle - MIN_SWEEP_ANGLE
        } else {
            sweepAngle += MIN_SWEEP_ANGLE.toFloat()
        }
        canvas.drawArc(fBounds, startAngle, sweepAngle, false, mPaint)
    }

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        mPaint.setColorFilter(cf)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("PixelFormat.TRANSPARENT", "android.graphics.PixelFormat"))
    override fun getOpacity(): Int {
        return PixelFormat.TRANSPARENT
    }

    private fun toggleAppearingMode() {
        mModeAppearing = !mModeAppearing
        if (mModeAppearing) {
            mCurrentGlobalAngleOffset = (mCurrentGlobalAngleOffset + MIN_SWEEP_ANGLE * 2) % 360
        }
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        fBounds.left = bounds.left + mBorderWidth / 2f + .5f
        fBounds.right = bounds.right - mBorderWidth / 2f - .5f
        fBounds.top = bounds.top + mBorderWidth / 2f + .5f
        fBounds.bottom = bounds.bottom - mBorderWidth / 2f - .5f
    }

    init {
        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = mBorderWidth
        mPaint.color = color

        setupAnimations()
    }

    private fun setupAnimations() {
        mObjectAnimatorAngle.interpolator = ANGLE_INTERPOLATOR
        mObjectAnimatorAngle.setDuration(ANGLE_ANIMATOR_DURATION.toLong())
        mObjectAnimatorAngle.repeatMode = ValueAnimator.RESTART
        mObjectAnimatorAngle.repeatCount = ValueAnimator.INFINITE

        mObjectAnimatorSweep.interpolator = SWEEP_INTERPOLATOR
        mObjectAnimatorSweep.setDuration(SWEEP_ANIMATOR_DURATION.toLong())
        mObjectAnimatorSweep.repeatMode = ValueAnimator.RESTART
        mObjectAnimatorSweep.repeatCount = ValueAnimator.INFINITE
        mObjectAnimatorSweep.addListener(MyAnimatorListener(this))
    }

    override fun start() {
        if (isRunning) {
            return
        }
        mRunning = true
        mObjectAnimatorAngle.start()
        mObjectAnimatorSweep.start()
        invalidateSelf()
    }

    override fun stop() {
        if (!isRunning) {
            return
        }
        mRunning = false
        mObjectAnimatorAngle.cancel()
        mObjectAnimatorSweep.cancel()
        mObjectAnimatorSweep.removeAllListeners()
        invalidateSelf()
    }

    override fun isRunning(): Boolean {
        return mRunning
    }

    var currentGlobalAngle: Float
        get() = mCurrentGlobalAngle
        set(currentGlobalAngle) {
            mCurrentGlobalAngle = currentGlobalAngle
            invalidateSelf()
        }

    var currentSweepAngle: Float
        get() = mCurrentSweepAngle
        set(currentSweepAngle) {
            mCurrentSweepAngle = currentSweepAngle
            invalidateSelf()
        }

    private class MyAnimatorListener(private val drawable: CircularAnimatedDrawable) : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {}

        override fun onAnimationEnd(animation: Animator) {}

        override fun onAnimationCancel(animation: Animator) {}

        override fun onAnimationRepeat(animation: Animator) {
            drawable.toggleAppearingMode()
        }
    }

    companion object {
        private val ANGLE_INTERPOLATOR: Interpolator = LinearInterpolator()
        private val SWEEP_INTERPOLATOR: Interpolator = DecelerateInterpolator()
        private const val ANGLE_ANIMATOR_DURATION = 2000
        private const val SWEEP_ANIMATOR_DURATION = 600
        const val MIN_SWEEP_ANGLE: Int = 30
    }
}