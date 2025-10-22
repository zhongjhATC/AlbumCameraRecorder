package com.zhongjh.imageedit.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.ImageView
import com.zhongjh.imageedit.R
import com.zhongjh.imageedit.core.sticker.ImageSticker
import com.zhongjh.imageedit.core.sticker.ImageStickerAdjustHelper
import com.zhongjh.imageedit.core.sticker.ImageStickerHelper
import com.zhongjh.imageedit.core.sticker.ImageStickerMoveHelper
import com.zhongjh.imageedit.core.sticker.ImageStickerPortrait
import kotlin.math.max

/**
 *
 * @author zhongjh
 * @date 2025/10/22
 */
abstract class BaseImageStickerView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ViewGroup(context, attrs, defStyleAttr), ImageSticker, View.OnClickListener {
    private val mContentView: View by lazy {
        onCreateContentView(context)
    }

    private var mScale = 1f

    private var mDownShowing = 0

    private val mMoveHelper: ImageStickerMoveHelper by lazy {
        ImageStickerMoveHelper(this)
    }

    private val mStickerHelper: ImageStickerHelper<BaseImageStickerView> by lazy {
        ImageStickerHelper(this)
    }

    private val mRemoveView: ImageView by lazy {
        ImageView(context)
    }
    private val mAdjustView: ImageView by lazy {
        ImageView(context)
    }

    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val mMatrix = Matrix()

    private val mFrame = RectF()

    private val mTempFrame = Rect()

    init {
        mPaint.color = Color.WHITE
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = STROKE_WIDTH
    }

    init {
        this.onInitialize(context)
    }

    open fun onInitialize(context: Context) {
        setBackgroundColor(Color.TRANSPARENT)

        addView(mContentView, contentLayoutParams)

        mRemoveView.scaleType = ImageView.ScaleType.FIT_XY
        mRemoveView.setImageResource(R.mipmap.image_ic_delete)
        addView(mRemoveView, anchorLayoutParams)
        mRemoveView.setOnClickListener(this)

        mAdjustView.scaleType = ImageView.ScaleType.FIT_XY
        mAdjustView.setImageResource(R.mipmap.image_ic_adjust)
        addView(mAdjustView, anchorLayoutParams)

        ImageStickerAdjustHelper(this, mAdjustView)
    }

    /**
     * 创建view
     * @param context 上下文
     * @return view
     */
    abstract fun onCreateContentView(context: Context): View

    override var stickerScale: Float
        get() = mScale
        set(scale) {
            mScale = scale

            mContentView.scaleX = mScale
            mContentView.scaleY = mScale

            val pivotX = (left + right) shr 1
            val pivotY = (top + bottom) shr 1

            mFrame[pivotX.toFloat(), pivotY.toFloat(), pivotX.toFloat()] = pivotY.toFloat()
            mFrame.inset(-(mContentView.measuredWidth shr 1).toFloat(), -(mContentView.measuredHeight shr 1).toFloat())

            mMatrix.setScale(mScale, mScale, mFrame.centerX(), mFrame.centerY())
            mMatrix.mapRect(mFrame)

            mFrame.round(mTempFrame)

            layout(mTempFrame.left, mTempFrame.top, mTempFrame.right, mTempFrame.bottom)
        }


    override fun addStickerScale(scale: Float) {
        this.stickerScale = scale * scale
    }

    private val contentLayoutParams: LayoutParams
        get() = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )

    private val anchorLayoutParams: LayoutParams
        get() = LayoutParams(ANCHOR_SIZE, ANCHOR_SIZE)

    override fun draw(canvas: Canvas) {
        if (isShowing()) {
            canvas.drawRect(
                ANCHOR_SIZE_HALF.toFloat(), ANCHOR_SIZE_HALF.toFloat(),
                (width - ANCHOR_SIZE_HALF).toFloat(),
                (height - ANCHOR_SIZE_HALF).toFloat(), mPaint
            )
        }
        super.draw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val count = childCount

        var maxHeight = 0
        var maxWidth = 0
        var childState = 0

        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                child.measure(widthMeasureSpec, heightMeasureSpec)

                maxWidth = Math.round(max(maxWidth.toDouble(), (child.measuredWidth * child.scaleX).toDouble())).toInt()
                maxHeight = Math.round(max(maxHeight.toDouble(), (child.measuredHeight * child.scaleY).toDouble())).toInt()

                childState = combineMeasuredStates(childState, child.measuredState)
            }
        }

        maxHeight = max(maxHeight.toDouble(), suggestedMinimumHeight.toDouble()).toInt()
        maxWidth = max(maxWidth.toDouble(), suggestedMinimumWidth.toDouble()).toInt()

        setMeasuredDimension(
            resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
            resolveSizeAndState(maxHeight, heightMeasureSpec, childState shl MEASURED_HEIGHT_STATE_SHIFT)
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        mFrame[left.toFloat(), top.toFloat(), right.toFloat()] = bottom.toFloat()

        val count = childCount
        if (count == 0) {
            return
        }

        mRemoveView.layout(0, 0, mRemoveView.measuredWidth, mRemoveView.measuredHeight)
        mAdjustView.layout(
            right - left - mAdjustView.measuredWidth,
            bottom - top - mAdjustView.measuredHeight,
            right - left, bottom - top
        )

        val centerX = (right - left) shr 1
        val centerY = (bottom - top) shr 1
        val hw = mContentView.measuredWidth shr 1
        val hh = mContentView.measuredHeight shr 1

        mContentView.layout(centerX - hw, centerY - hh, centerX + hw, centerY + hh)
    }

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        return isShowing() && super.drawChild(canvas, child, drawingTime)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!isShowing() && ev.action == MotionEvent.ACTION_DOWN) {
            mDownShowing = 0
            show()
            return true
        }
        return isShowing() && super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val handled = mMoveHelper.onTouch(this, event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> mDownShowing++
            MotionEvent.ACTION_UP -> if (mDownShowing > 1 && event.eventTime - event.downTime < ViewConfiguration.getTapTimeout()) {
                onContentTap()
                return true
            }

            else -> {}
        }
        return handled or super.onTouchEvent(event)
    }

    override fun onClick(v: View) {
        if (v === mRemoveView) {
            onRemove()
        }
    }

    private fun onRemove() {
        mStickerHelper.remove()
    }

    open fun onContentTap() {
    }

    override fun show(): Boolean {
        return mStickerHelper.show()
    }

    override fun remove(): Boolean {
        return mStickerHelper.remove()
    }

    override fun dismiss(): Boolean {
        return mStickerHelper.dismiss()
    }

    override fun isShowing(): Boolean {
        return mStickerHelper.isShowing()
    }

    override fun getFrame(): RectF? {
        return mStickerHelper.getFrame()
    }

    override fun onSticker(canvas: Canvas) {
        canvas.translate(mContentView.x, mContentView.y)
        mContentView.draw(canvas)
    }

    override fun registerCallback(callback: ImageStickerPortrait.Callback?) {
        mStickerHelper.registerCallback(callback)
    }

    override fun unregisterCallback(callback: ImageStickerPortrait.Callback?) {
        mStickerHelper.unregisterCallback(callback)
    }

    companion object {
        private const val ANCHOR_SIZE = 48

        private const val ANCHOR_SIZE_HALF = ANCHOR_SIZE shr 1

        private const val STROKE_WIDTH = 3f
    }
}
