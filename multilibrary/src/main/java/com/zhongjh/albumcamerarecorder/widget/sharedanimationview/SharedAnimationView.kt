package com.zhongjh.albumcamerarecorder.widget.sharedanimationview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.zhongjh.common.utils.DisplayMetricsUtils.getRealScreenHeight
import com.zhongjh.common.utils.DisplayMetricsUtils.getRealScreenWidth

/**
 * 一个共享动画的View
 * 容器可以添加任何View
 */
class SharedAnimationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(
    context, attrs, defStyleAttr
) {

    /**
     * 用于 [backgroundView]的透明度值
     */
    private var mAlpha = 0.0f
    /**
     * 动画的时长
     */
    private val animationDuration: Long = 250
    private var mOriginLeft = 0
    private var mOriginTop = 0
    private var mOriginHeight = 0
    private var mOriginWidth = 0
    private var screenWidth = 0
    private var screenHeight = 0
    /**
     * 屏幕高度，包含状态栏
     */
    private val appInScreenHeight: Int
    private var targetImageTop = 0
    private var targetImageWidth = 0
    private var targetImageHeight = 0
    private var targetEndLeft = 0
    private var realWidth = 0
    private var realHeight = 0
    private var isAnimating = false
    /**
     * 内容View
     */
    private val contentLayout: FrameLayout
    /**
     * 背景View
     */
    private val backgroundView: View
    /**
     * 封装View
     */
    private val sharedAnimationWrapper: SharedAnimationWrapper
    private var startX = 0
    private var startY = 0


    init {
        appInScreenHeight = getRealScreenHeight(context)
        getScreenSize()
        backgroundView = View(context)
        backgroundView.layoutParams =
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        backgroundView.alpha = mAlpha
        addView(backgroundView)
        contentLayout = FrameLayout(context)
        contentLayout.layoutParams =
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        addView(contentLayout)
        sharedAnimationWrapper = SharedAnimationWrapper(contentLayout)
    }

    fun startNormal(realWidth: Int, realHeight: Int, showImmediately: Boolean) {
        this.realWidth = realWidth
        this.realHeight = realHeight
        mOriginLeft = 0
        mOriginTop = 0
        mOriginWidth = 0
        mOriginHeight = 0
        visibility = VISIBLE
        setOriginParams()
        showNormalMin(
            targetImageTop.toFloat(),
            targetEndLeft.toFloat(),
            targetImageWidth.toFloat(),
            targetImageHeight.toFloat()
        )
        if (showImmediately) {
            mAlpha = 1f
            backgroundView.alpha = mAlpha
        } else {
            mAlpha = 0f
            backgroundView.alpha = mAlpha
            contentLayout.alpha = 0f
            contentLayout.animate().alpha(1f).setDuration(animationDuration).start()
            backgroundView.animate().alpha(1f).setDuration(animationDuration).start()
        }
        setShowEndParams()
    }

    /**
     * 添加View
     */
    fun setContentView(view: View) {
        contentLayout.addView(view)
    }

    /**
     * 设置背景透明颜色度
     */
    fun setBackgroundAlpha(mAlpha: Float) {
        this.mAlpha = mAlpha
        backgroundView.alpha = mAlpha
    }

    fun setViewParams(
        left: Int,
        top: Int,
        originWidth: Int,
        originHeight: Int,
        realWidth: Int,
        realHeight: Int,
    ) {
        this.realWidth = realWidth
        this.realHeight = realHeight
        this.mOriginLeft = left
        this.mOriginTop = top
        this.mOriginWidth = originWidth
        this.mOriginHeight = originHeight
    }

    /**
     * getScreenSize
     */
    private fun getScreenSize() {
        screenWidth = getRealScreenWidth(context)
        screenHeight = getRealScreenHeight(context)
    }

    /**
     * 设置原点参数
     */
    private fun setOriginParams() {
        val locationImage = IntArray(2)
        contentLayout.getLocationOnScreen(locationImage)
        targetEndLeft = 0
        if (screenWidth / screenHeight.toFloat() < realWidth / realHeight.toFloat()) {
            targetImageWidth = screenWidth
            targetImageHeight = (targetImageWidth * (realHeight / realWidth.toFloat())).toInt()
            targetImageTop = (screenHeight - targetImageHeight) / 2
        } else {
            targetImageHeight = screenHeight
            targetImageWidth = (targetImageHeight * (realWidth / realHeight.toFloat())).toInt()
            targetImageTop = 0
            targetEndLeft = (screenWidth - targetImageWidth) / 2
        }
        sharedAnimationWrapper.setWidth(mOriginWidth.toFloat())
        sharedAnimationWrapper.setHeight(mOriginHeight.toFloat())
        sharedAnimationWrapper.marginLeft = mOriginLeft
        sharedAnimationWrapper.marginTop = mOriginTop
    }

    private fun setShowEndParams() {
        isAnimating = false
        changeContentViewToFullscreen()
        onMagicalViewListener?.onBeginMagicalAnimComplete(this@MagicalView, false)
    }

    private fun showNormalMin(
        animRatio: Float, startY: Float, endY: Float, startLeft: Float, endLeft: Float,
        startWidth: Float, endWidth: Float, startHeight: Float, endHeight: Float,
    ) {
        showNormalMin(
            false,
            animRatio,
            startY,
            endY,
            startLeft,
            endLeft,
            startWidth,
            endWidth,
            startHeight,
            endHeight
        )
    }

    private fun showNormalMin(endY: Float, endLeft: Float, endWidth: Float, endHeight: Float) {
        showNormalMin(true, 0f, 0f, endY, 0f, endLeft, 0f, endWidth, 0f, endHeight)
    }

    private fun showNormalMin(
        showImmediately: Boolean,
        animRatio: Float,
        startY: Float,
        endY: Float,
        startLeft: Float,
        endLeft: Float,
        startWidth: Float,
        endWidth: Float,
        startHeight: Float,
        endHeight: Float,
    ) {
        if (showImmediately) {
            sharedAnimationWrapper.setWidth(endWidth)
            sharedAnimationWrapper.setHeight(endHeight)
            sharedAnimationWrapper.marginLeft = endLeft.toInt()
            sharedAnimationWrapper.marginTop = endY.toInt()
        } else {
            val xOffset = animRatio * (endLeft - startLeft)
            val widthOffset = animRatio * (endWidth - startWidth)
            val heightOffset = animRatio * (endHeight - startHeight)
            val topOffset = animRatio * (endY - startY)
            sharedAnimationWrapper.setWidth(startWidth + widthOffset)
            sharedAnimationWrapper.setHeight(startHeight + heightOffset)
            sharedAnimationWrapper.marginLeft = (startLeft + xOffset).toInt()
            sharedAnimationWrapper.marginTop = (startY + topOffset).toInt()
        }
    }

    private fun changeContentViewToFullscreen() {
        targetImageHeight = screenHeight
        targetImageWidth = screenWidth
        targetImageTop = 0
        sharedAnimationWrapper.setHeight(screenHeight.toFloat())
        sharedAnimationWrapper.setWidth(screenWidth.toFloat())
        sharedAnimationWrapper.marginTop = 0
        sharedAnimationWrapper.marginLeft = 0
    }

    private var onMagicalViewListener: OnMagicalViewListener? = null

    fun setOnMagicalViewListener(l: OnMagicalViewListener?) {
        this.onMagicalViewListener = l
    }

}