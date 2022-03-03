package com.zhongjh.albumcamerarecorder.camera.listener;

/**
 * 点击或者长按事件回调
 *
 * @author zhongjh
 * @date 2018/7/23
 */
public interface ClickOrLongListener {

    /**
     * 按钮按下后的效果，用于禁止滑动等别的界面的操作
     */
    void actionDown();

    /**
     * 点击
     */
    void onClick();

    /**
     * 长按启动
     */
    void onLongClick();

    /**
     * 长按结束
     * @param time 时间
     */
    void onLongClickEnd(long time);

    /**
     * 长按结束如果过短
     * @param time 时间
     */
    void onLongClickShort(long time);

    /**
     * 长按中途出现异常
     */
    void onLongClickError();

    /**
     * 禁止点击后，依然点击时的提示
     */
    void onBanClickTips();

    /**
     * 可以点击结束的提示
     */
    void onClickStopTips();

}
