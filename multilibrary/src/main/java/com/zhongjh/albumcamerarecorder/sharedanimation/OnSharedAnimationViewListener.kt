package com.zhongjh.albumcamerarecorder.sharedanimation

/**
 * 一个共享动画的View的事件
 */
interface OnSharedAnimationViewListener {

    fun onBeginBackMinAnim()
    fun onBeginBackMinMagicalFinish(isResetSize: Boolean)
    fun onBeginMagicalAnimComplete(mojitoView: SharedAnimationView, showImmediately: Boolean)
    fun onBackgroundAlpha(alpha: Float)
    fun onMagicalViewFinish()
}