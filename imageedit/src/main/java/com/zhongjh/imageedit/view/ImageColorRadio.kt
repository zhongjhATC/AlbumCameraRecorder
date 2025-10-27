package com.zhongjh.imageedit.view

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.widget.AppCompatRadioButton
import com.zhongjh.imageedit.R
import kotlin.math.min

/**
 * @author zhongjh
 * @date 2025/10/27
 */
class ImageColorRadio : AppCompatRadioButton, AnimatorUpdateListener {
    private var mColor = Color.WHITE

    private var mStrokeColor = Color.WHITE

    private var mRadiusRatio = 0f

    private val mAnimator: ValueAnimator by lazy {
        ValueAnimator.ofFloat(0f, 1f)
    }

    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize(context, attrs)
    }

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
        initialize(context, attrs)
    }

    private fun initialize(context: Context, attrs: AttributeSet?) {
        var imageColorRadio: TypedArray? = null
        try {
            imageColorRadio = context.obtainStyledAttributes(attrs, R.styleable.ImageColorRadio)
            mColor = imageColorRadio.getColor(R.styleable.ImageColorRadio_z_image_color, Color.WHITE)
            mStrokeColor = imageColorRadio.getColor(R.styleable.ImageColorRadio_z_image_stroke_color, Color.WHITE)
        } finally {
            if (null != imageColorRadio) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    imageColorRadio.close()
                } else {
                    imageColorRadio.recycle()
                }
            }
        }

        buttonDrawable = null

        mPaint.color = mColor
        mPaint.strokeWidth = 5f
    }

    private val animator: ValueAnimator
        get() {
            mAnimator.addUpdateListener(this)
            mAnimator.setDuration(200)
            mAnimator.interpolator = AccelerateDecelerateInterpolator()
            return mAnimator
        }

    var color: Int
        get() = mColor
        set(color) {
            mColor = color
            mPaint.color = mColor
        }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        val hw = width / 2f
        val hh = height / 2f
        val radius = min(hw.toDouble(), hh.toDouble()).toFloat()

        canvas.save()
        mPaint.color = mColor
        mPaint.style = Paint.Style.FILL
        canvas.drawCircle(hw, hh, getBallRadius(radius), mPaint)

        mPaint.color = mStrokeColor
        mPaint.style = Paint.Style.STROKE
        canvas.drawCircle(hw, hh, getRingRadius(radius), mPaint)
        canvas.restore()
    }

    private fun getBallRadius(radius: Float): Float {
        return radius * ((RADIUS_BALL - RADIUS_BASE) * mRadiusRatio + RADIUS_BASE)
    }

    private fun getRingRadius(radius: Float): Float {
        return radius * ((RADIUS_RING - RADIUS_BASE) * mRadiusRatio + RADIUS_BASE)
    }

    override fun setChecked(checked: Boolean) {
        val isChanged = checked != isChecked

        super.setChecked(checked)

        if (isChanged) {
            val animator = animator

            if (checked) {
                animator.start()
            } else {
                animator.reverse()
            }
        }
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        mRadiusRatio = animation.animatedValue as Float
        invalidate()
    }

    companion object {
        private const val RADIUS_BASE = 0.6f

        private const val RADIUS_RING = 0.9f

        private const val RADIUS_BALL = 0.72f
    }
}
