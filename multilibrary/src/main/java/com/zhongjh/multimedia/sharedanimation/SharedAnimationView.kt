package com.zhongjh.multimedia.sharedanimation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.transition.ChangeBounds
import android.transition.ChangeImageTransform
import android.transition.ChangeTransform
import android.transition.TransitionManager
import android.transition.TransitionSet
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import com.zhongjh.common.utils.DisplayMetricsUtils.getRealScreenHeight
import com.zhongjh.common.utils.DisplayMetricsUtils.getRealScreenWidth

/**
 * 一个共享动画的View
 * 包含以下view:
 * 1. 容器可以添加任何View，目前添加的是viewPager2
 * 2. 还有个SharedAnimationWrapper，是用于模仿上个界面RecyclerView的item的位置、宽高作用域过渡
 */
class SharedAnimationView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    private val tag: String = this@SharedAnimationView.javaClass.simpleName

    /**
     * 用于 [backgroundView]的透明度值
     */
    private var mAlpha = 0.0f

    /**
     * 动画的时长
     */
    private val animationDuration: Long = 200
    private var mOriginLeft = 0
    private var mOriginTop = 0
    private var mOriginHeight = 0
    private var mOriginWidth = 0
    private var screenWidth = 0
    private var screenHeight = 0

    /**
     * 屏幕高度，包含状态栏
     */
    private val appInScreenHeight: Int = getRealScreenHeight(context)
    private var targetImageTop = 0
    private var targetImageWidth = 0
    private var targetImageHeight = 0
    private var targetEndLeft = 0
    private var realWidth = 0
    private var realHeight = 0

    /**
     * 是否正在动画中
     */
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
     * 设置背景颜色
     * 设置backgroundView背景颜色，这样才能和共享动画一起出现，如果不自定义这个颜色，在加载数据比较大的视频上，就会先黑色背景，过1秒才显示视频这种问题出现
     *
     * @param color
     */
    override fun setBackgroundColor(color: Int) {
        backgroundView.setBackgroundColor(color)
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
     * 开始共享动画
     * @param showImmediately 是否立即显示
     */
    fun start(showImmediately: Boolean) {
        mAlpha = if (showImmediately) {
            1f
        } else {
            0f
        }
        backgroundView.alpha = mAlpha
        visibility = VISIBLE
        setOriginParams()
        beginShow(showImmediately)
    }

    /**
     * getScreenSize
     */
    private fun getScreenSize() {
        screenWidth = getRealScreenWidth(context)
        screenHeight = getRealScreenHeight(context)
    }

    /**
     * 设置初始、目标参数
     */
    private fun setOriginParams() {
        targetEndLeft = 0
        Log.d(tag, "screenRate: " + screenWidth / screenHeight.toFloat())
        Log.d(tag, "realRate: " + realWidth / realHeight.toFloat())
        // 手机比例(高比宽多)和图片比例比较
        if (screenWidth / screenHeight.toFloat() < realWidth / realHeight.toFloat()) {
            // 图片偏横向
            targetImageWidth = screenWidth
            targetImageHeight = (targetImageWidth * (realHeight / realWidth.toFloat())).toInt()
            targetImageTop = (screenHeight - targetImageHeight) / 2
            targetEndLeft = 0
            Log.d(tag, "图片偏横向 $targetImageWidth $targetImageHeight $targetImageTop 0")
        } else {
            // 图片偏竖向
            targetImageHeight = screenHeight
            targetImageWidth = (targetImageHeight * (realWidth / realHeight.toFloat())).toInt()
            targetImageTop = 0
            targetEndLeft = (screenWidth - targetImageWidth) / 2
            Log.d(tag, "图片偏竖向 $targetImageWidth $targetImageHeight 0 $targetEndLeft")
        }
        Log.d(tag, "sharedAnimationWrapper $mOriginWidth $mOriginHeight $mOriginLeft $mOriginTop")
        // 设置原始参数(RecyclerView的item的参数)
        sharedAnimationWrapper.setWidth(mOriginWidth.toFloat())
        sharedAnimationWrapper.setHeight(mOriginHeight.toFloat())
        sharedAnimationWrapper.marginLeft = mOriginLeft
        sharedAnimationWrapper.marginTop = mOriginTop
    }

    /**
     * 开始显示动画
     *
     * @param showImmediately 是否立即显示
     *
     */
    private fun beginShow(showImmediately: Boolean) {
        if (showImmediately) {
            mAlpha = 1f
            backgroundView.alpha = mAlpha
            showNormalMin(
                targetImageTop.toFloat(),
                targetEndLeft.toFloat(),
                targetImageWidth.toFloat(),
                targetImageHeight.toFloat()
            )
            setShowEndParams()
        } else {
            val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
            // 执行0-1的这个中间不停触发的动画事件addUpdateListener
            valueAnimator.addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                Log.d(
                    tag,
                    "beginShow $value $mOriginTop $targetImageTop $mOriginLeft $targetEndLeft $mOriginWidth $targetImageWidth $mOriginHeight $targetImageHeight"
                )
                showNormalMin(
                    value,
                    mOriginTop.toFloat(),
                    targetImageTop.toFloat(),
                    mOriginLeft.toFloat(),
                    targetEndLeft.toFloat(),
                    mOriginWidth.toFloat(),
                    targetImageWidth.toFloat(),
                    mOriginHeight.toFloat(),
                    targetImageHeight.toFloat()
                )
            }
            valueAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    setShowEndParams()
                }
            })
            valueAnimator.interpolator = AccelerateDecelerateInterpolator()
            valueAnimator.setDuration(animationDuration).start()
            changeBackgroundViewAlpha(false)
        }
    }

    /**
     * @param isAlpha 是否透明
     */
    private fun changeBackgroundViewAlpha(isAlpha: Boolean) {
        val end: Float = if (isAlpha) {
            0F
        } else {
            1F
        }
        val valueAnimator = ValueAnimator.ofFloat(mAlpha, end)
        valueAnimator.addUpdateListener { animation ->
            isAnimating = true
            mAlpha = animation.animatedValue as Float
            backgroundView.alpha = mAlpha
            onSharedAnimationViewListener?.onBackgroundAlpha(mAlpha)
        }
        valueAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                isAnimating = false
                if (isAlpha) {
                    onSharedAnimationViewListener?.onMagicalViewFinish()
                }
            }
        })
        valueAnimator.duration = animationDuration
        valueAnimator.start()
    }

    private fun setShowEndParams() {
        isAnimating = false
        changeContentViewToFullscreen()
        onSharedAnimationViewListener?.onBeginSharedAnimComplete(this@SharedAnimationView, false)
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

    /**
     * 开始退出的共享动画
     */
    fun backToMin() {
        if (isAnimating) {
            return
        }
        if (mOriginWidth == 0 || mOriginHeight == 0) {
            // 拿不到宽高，可能是viewPager滑动数据到RecyclerView分页后的地方。则透明形式动画
            backToMinWithoutView()
            return
        }
        onSharedAnimationViewListener?.onBeginBackMinAnim()
        backToMinWithTransition()
    }

    /**
     * 具体的退出共享动画
     */
    private fun backToMinWithTransition() {
        contentLayout.post {
            // 添加过渡动画，让系统Api实现动画
            TransitionManager.beginDelayedTransition(
                contentLayout.parent as ViewGroup,
                TransitionSet()
                    .setDuration(animationDuration)
                    .addTransition(ChangeBounds())
                    .addTransition(ChangeTransform())
                    .addTransition(ChangeImageTransform())
            )
            beginBackToMin()
            contentLayout.translationX = 0f
            contentLayout.translationY = 0f
            sharedAnimationWrapper.setWidth(mOriginWidth.toFloat())
            sharedAnimationWrapper.setHeight(mOriginHeight.toFloat())
            sharedAnimationWrapper.marginTop = mOriginTop
            sharedAnimationWrapper.marginLeft = mOriginLeft
            changeBackgroundViewAlpha(true)
        }
    }

    private fun beginBackToMin() {
        onSharedAnimationViewListener?.onBeginBackMinMagicalFinish(true)
    }

    /**
     * 透明形式动画
     */
    private fun backToMinWithoutView() {
        contentLayout.animate().alpha(0f).setDuration(animationDuration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onSharedAnimationViewListener?.onMagicalViewFinish()
                }
            }).start()
        backgroundView.animate().alpha(0f).setDuration(animationDuration).start()
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

    private var onSharedAnimationViewListener: OnSharedAnimationViewListener? = null

    fun setOnSharedAnimationViewListener(l: OnSharedAnimationViewListener?) {
        this.onSharedAnimationViewListener = l
    }

}