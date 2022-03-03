package com.zhongjh.albumcamerarecorder.camera.listener

/**
 * 点击或者长按事件回调
 *
 * @author zhongjh
 * @date 2018/7/23
 */
interface ClickOrLongListener {

    /**
     * 按钮按下后的效果，用于禁止滑动等别的界面的操作
     */
    fun actionDown()

    /**
     * 点击
     */
    fun onClick()

    /**
     * 长按启动
     */
    fun onLongClick()

    /**
     * 长按结束
     * @param time 时间
     */
    fun onLongClickEnd(time: Long)

    /**
     * 长按结束如果过短
     * @param time 时间
     */
    fun onLongClickShort(time: Long)

    /**
     * 长按中途出现异常
     */
    fun onLongClickError()

    /**
     * 禁止点击后，依然点击时的提示
     */
    fun onBanClickTips()

    /**
     * 可以点击结束的提示
     */
    fun onClickStopTips()
}