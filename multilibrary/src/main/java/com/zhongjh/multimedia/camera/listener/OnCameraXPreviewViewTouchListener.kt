package com.zhongjh.multimedia.camera.listener

import android.annotation.SuppressLint
import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View

/**
 * 监听预览摄像的Touch
 */
class OnCameraXPreviewViewTouchListener(context: Context) : View.OnTouchListener {
    /**
     * 手势识别 - 主要捕获点击、双击
     */
    private val mGestureDetector: GestureDetector

    /**
     * 缩放手势识别 - 主要捕获放大、缩小
     */
    private val mScaleGestureDetector: ScaleGestureDetector

    /**
     * 缩放监听
     */
    private val onScaleGestureListener: ScaleGestureDetector.OnScaleGestureListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val delta = detector.scaleFactor
            mCustomTouchListener?.zoom(delta)
            return true
        }
    }

    /**
     * 点击、双击监听
     */
    private val onGestureListener: GestureDetector.SimpleOnGestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onLongPress(e: MotionEvent) {}
        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            mCustomTouchListener?.click(e.x, e.y)
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            mCustomTouchListener?.doubleClick(e.x, e.y)
            return true
        }
    }

    init {
        mGestureDetector = GestureDetector(context, onGestureListener)
        mScaleGestureDetector = ScaleGestureDetector(context, onScaleGestureListener)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        // 先判断缩放
        mScaleGestureDetector.onTouchEvent(event)
        if (!mScaleGestureDetector.isInProgress) {
            // 再判断点击
            mGestureDetector.onTouchEvent(event)
        }
        return true
    }

    private var mCustomTouchListener: CustomTouchListener? = null

    interface CustomTouchListener {
        /**
         * 放大
         */
        fun zoom(delta: Float)

        /**
         * 点击
         */
        fun click(x: Float, y: Float)

        /**
         * 双击
         */
        fun doubleClick(x: Float, y: Float)
    }

    fun setCustomTouchListener(customTouchListener: CustomTouchListener?) {
        mCustomTouchListener = customTouchListener
    }
}