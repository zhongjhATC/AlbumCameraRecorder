package com.zhongjh.multimedia.camera.widget

import android.content.Context
import android.graphics.Point
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import kotlin.jvm.Volatile
import androidx.annotation.DrawableRes
import com.zhongjh.multimedia.R

/**
 * 焦点view
 */
class FocusView : AppCompatImageView {

    private var mFocusIngImg = 0
    private var mFocusSuccessImg = 0
    private var mFocusFailedImg = 0
    private val mAnimation by lazy { AnimationUtils.loadAnimation(context, R.anim.focusview_show_zjh) }
    private val mHandler by lazy { Handler(Looper.getMainLooper()) }
    private val mRunnable by lazy { Runnable { setFocusGone() } }

    /**
     * 使用volatile保证多线程情况下的boolean类型的原子性
     * 确保焦点样式的改变都是基于最新的(因为有个延迟)
     */
    @Volatile
    private var isDisappear = false

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FocusView)
        mFocusIngImg = typedArray.getResourceId(R.styleable.FocusView_focusIng, R.drawable.focus_ing)
        mFocusSuccessImg = typedArray.getResourceId(R.styleable.FocusView_focusSuccess, R.drawable.focus_success)
        mFocusFailedImg = typedArray.getResourceId(R.styleable.FocusView_focusFailed, R.drawable.focus_failed)
        typedArray.recycle()
    }

    private fun init() {
        visibility = GONE
    }

    fun destroy() {
        mHandler.removeCallbacks(mRunnable, null)
        visibility = GONE
    }

    fun setDisappear(disappear: Boolean) {
        isDisappear = disappear
    }

    /**
     * 在某个坐标显示焦点 - ing样式
     *
     * @param point 坐标点
     */
    fun startFocusIng(point: Point) {
        val params = layoutParams as RelativeLayout.LayoutParams
        params.topMargin = point.y - measuredHeight / 2
        params.leftMargin = point.x - measuredWidth / 2
        layoutParams = params
        visibility = VISIBLE
        setFocusResource(mFocusIngImg)
        startAnimation(mAnimation)
    }

    /**
     * 改变焦点样式 - Success样式
     */
    fun changeFocusSuccess() {
        if (isDisappear) {
            setFocusResource(mFocusSuccessImg)
        }
        // 删除延迟事件，重新进行唯一的一次延迟事件
        mHandler.removeCallbacks(mRunnable, null)
        mHandler.postDelayed(mRunnable, DELAY_MILLIS)
    }

    /**
     * 改变焦点样式 - Failed样式
     */
    fun changeFocusFailed() {
        if (isDisappear) {
            setFocusResource(mFocusFailedImg)
        }
        // 删除延迟事件，重新进行唯一的一次延迟事件
        mHandler.removeCallbacks(mRunnable, null)
        mHandler.postDelayed(mRunnable, DELAY_MILLIS)
    }

    private fun setFocusGone() {
        if (isDisappear) {
            visibility = GONE
        }
    }

    private fun setFocusResource(@DrawableRes resId: Int) {
        setImageResource(resId)
    }

    companion object {
        private const val DELAY_MILLIS: Long = 1000
    }
}