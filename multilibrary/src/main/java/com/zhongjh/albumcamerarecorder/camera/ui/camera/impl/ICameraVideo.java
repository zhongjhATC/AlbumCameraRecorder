package com.zhongjh.albumcamerarecorder.camera.ui.camera.impl;

/**
 * 拍摄界面的有关视频View的接口
 *
 * @author zhongjh
 * @date 2022/8/23
 */
public interface ICameraVideo {

    /**
     * 初始化有关视频的配置数据
     */
    void initData();

    /**
     * 生命周期onDestroy
     */
    void onDestroy();

    /**
     * 录制视频
     */
    void recordVideo();

    /**
     * 录像暂停
     *
     * @param recordedDurationNanos 当前视频持续时间：纳米单位
     */
    void onRecordPause(long recordedDurationNanos);

    /**
     * 视频录制成功
     *
     * @param path 视频录制结束后提供的路径
     */
    void onRecordSuccess(String path);
}
