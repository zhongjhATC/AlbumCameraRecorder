package com.zhongjh.albumcamerarecorder.widget

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

    private var screenWidth = 0
    private var screenHeight = 0

    /**
     * 屏幕高度，包含状态栏
     */
    private val appInScreenHeight: Int

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

    /**
     * getScreenSize
     */
    private fun getScreenSize() {
        screenWidth = getRealScreenWidth(context)
        screenHeight = getRealScreenHeight(context)
    }

    /**
     * 添加View
     */
    fun setContentView(view: View) {
        contentLayout.addView(view)
    }


}