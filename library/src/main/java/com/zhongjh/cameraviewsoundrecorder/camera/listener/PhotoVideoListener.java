package com.zhongjh.cameraviewsoundrecorder.camera.listener;

/**
 * 拍照录制事件回调
 * Created by zhongjh on 2018/7/23.
 */
public interface PhotoVideoListener {

    /**
     * 按钮点击后的效果，用于禁止滑动等别的界面的操作
     */
    void actionDown();

    /**
     * 拍照
     */
    void takePictures();

    /**
     * 录像如果过短
     * @param time 时间
     */
    void recordShort(long time);

    /**
     * 录像启动
     */
    void recordStart();

    /**
     * 记录结束
     * @param time 时间
     */
    void recordEnd(long time);

    /**
     * 记录变焦
     * @param zoom 变焦
     */
    void recordZoom(float zoom);

    /**
     * 记录异常
     */
    void recordError();

}
