package com.zhongjh.albumcamerarecorder.album.widget

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.zhongjh.albumcamerarecorder.R

/**
 * @author zhongjh
 */
class CheckView : View {

    private var mCountable = false
    private var mChecked = false
    private var mCheckedNum = 0
    private lateinit var mStrokePaint: Paint
    private val mBackgroundPaint by lazy {
        initBackgroundPaint()
    }
    private val mTextPaint by lazy {
        initTextPaint()
    }
    private val mShadowPaint by lazy {
        initShadowPaint()
    }
    private var mCheckDrawable: Drawable? = null
    private var mDensity = 0f
    private var mCheckRect: Rect? = null
    private var mEnabled = true

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // fixed size 48dp x 48dp
        val sizeSpec = MeasureSpec.makeMeasureSpec((SIZE * mDensity).toInt(), MeasureSpec.EXACTLY)
        super.onMeasure(sizeSpec, sizeSpec)
    }

    private fun init(context: Context) {
        mDensity = context.resources.displayMetrics.density
        mStrokePaint = Paint()
        mStrokePaint.isAntiAlias = true
        mStrokePaint.style = Paint.Style.STROKE
        mStrokePaint.xfermode =
            PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
        mStrokePaint.strokeWidth = STROKE_WIDTH * mDensity
        val ta =
            getContext().theme.obtainStyledAttributes(intArrayOf(R.attr.item_checkCircle_borderColor))
        val defaultColor = ResourcesCompat.getColor(
            resources, R.color.blue_item_checkCircle_borderColor,
            getContext().theme
        )
        val color = ta.getColor(0, defaultColor)
        ta.recycle()
        mStrokePaint.color = color
        mCheckDrawable = ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.ic_check_white_18dp, context.theme
        )
    }

    fun setChecked(checked: Boolean) {
        check(!mCountable) { "CheckView is countable, call setCheckedNum() instead." }
        mChecked = checked
        invalidate()
    }

    /**
     * 设置是否多选
     */
    fun setCountable(countable: Boolean) {
        mCountable = countable
    }

    fun setCheckedNum(checkedNum: Int) {
        check(mCountable) { "CheckView is not countable, call setChecked() instead." }
        require(!(checkedNum != UNCHECKED && checkedNum <= 0)) { "checked num can't be negative." }
        mCheckedNum = checkedNum
        invalidate()
    }

    override fun setEnabled(enabled: Boolean) {
        if (mEnabled != enabled) {
            mEnabled = enabled
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // draw outer and inner shadow
        initShadowPaint()
        canvas.drawCircle(
            SIZE.toFloat() * mDensity / 2, SIZE.toFloat() * mDensity / 2,
            (STROKE_RADIUS + STROKE_WIDTH / 2 + SHADOW_WIDTH) * mDensity, mShadowPaint
        )

        // draw white stroke
        canvas.drawCircle(
            SIZE.toFloat() * mDensity / 2, SIZE.toFloat() * mDensity / 2,
            STROKE_RADIUS * mDensity, mStrokePaint
        )

        // draw content
        if (mCountable) {
            if (mCheckedNum != UNCHECKED) {
                initBackgroundPaint()
                canvas.drawCircle(
                    SIZE.toFloat() * mDensity / 2, SIZE.toFloat() * mDensity / 2,
                    BG_RADIUS * mDensity, mBackgroundPaint
                )
                initTextPaint()
                val text = mCheckedNum.toString()
                val baseX = (width - mTextPaint.measureText(text)).toInt() / 2
                val baseY = (height - mTextPaint.descent() - mTextPaint.ascent()).toInt() / 2
                canvas.drawText(text, baseX.toFloat(), baseY.toFloat(), mTextPaint)
            }
        } else {
            if (mChecked) {
                initBackgroundPaint()
                canvas.drawCircle(
                    SIZE.toFloat() * mDensity / 2, SIZE.toFloat() * mDensity / 2,
                    BG_RADIUS * mDensity, mBackgroundPaint
                )
                mCheckDrawable!!.bounds = checkRect
                mCheckDrawable!!.draw(canvas)
            }
        }

        // enable hint
        alpha = if (mEnabled) 1.0f else 0.5f
    }

    private fun initShadowPaint(): Paint {
        val shadowPaint = Paint()
        shadowPaint.isAntiAlias = true
        // all in dp
        val outerRadius = STROKE_RADIUS + STROKE_WIDTH / 2
        val innerRadius = outerRadius - STROKE_WIDTH
        val gradientRadius = outerRadius + SHADOW_WIDTH
        val stop0 = (innerRadius - SHADOW_WIDTH) / gradientRadius
        val stop1 = innerRadius / gradientRadius
        val stop2 = outerRadius / gradientRadius
        val stop3 = 1.0f
        shadowPaint.shader = RadialGradient(
            SIZE.toFloat() * mDensity / 2,
            SIZE.toFloat() * mDensity / 2,
            gradientRadius * mDensity, intArrayOf(
                Color.parseColor("#00000000"), Color.parseColor("#0D000000"),
                Color.parseColor("#0D000000"), Color.parseColor("#00000000")
            ), floatArrayOf(stop0, stop1, stop2, stop3),
            Shader.TileMode.CLAMP
        )
        return shadowPaint
    }

    private fun initBackgroundPaint(): Paint {
        val backgroundPaint = Paint()
        backgroundPaint.isAntiAlias = true
        backgroundPaint.style = Paint.Style.FILL
        val ta = context.theme
            .obtainStyledAttributes(intArrayOf(R.attr.item_checkCircle_backgroundColor))
        val defaultColor = ResourcesCompat.getColor(
            resources, R.color.blue_item_checkCircle_backgroundColor,
            context.theme
        )
        val color = ta.getColor(0, defaultColor)
        ta.recycle()
        backgroundPaint.color = color
        return backgroundPaint
    }

    private fun initTextPaint(): TextPaint {
        val textPaint = TextPaint()
        textPaint.isAntiAlias = true
        textPaint.color = Color.WHITE
        textPaint.typeface = Typeface.create(
            Typeface.DEFAULT,
            Typeface.BOLD
        )
        textPaint.textSize = 12.0f * mDensity
        return textPaint
    }

    /**
     * rect for drawing checked number or mark
     * @return rect
     */
    private val checkRect: Rect
        get() {
            if (mCheckRect == null) {
                val rectPadding = (SIZE * mDensity / 2 - CONTENT_SIZE * mDensity / 2).toInt()
                mCheckRect = Rect(
                    rectPadding, rectPadding,
                    (SIZE * mDensity - rectPadding).toInt(), (SIZE * mDensity - rectPadding).toInt()
                )
            }
            return mCheckRect!!
        }

    companion object {
        const val UNCHECKED = Int.MIN_VALUE

        /**
         * dp
         */
        private const val STROKE_WIDTH = 3.0f

        /**
         * dp
         */
        private const val SHADOW_WIDTH = 6.0f

        /**
         * dp
         */
        private const val SIZE = 48

        /**
         * dp
         */
        private const val STROKE_RADIUS = 11.5f

        /**
         * dp
         */
        private const val BG_RADIUS = 11.0f

        /**
         * dp
         */
        private const val CONTENT_SIZE = 16
    }
}