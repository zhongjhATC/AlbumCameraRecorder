package com.zhongjh.multimedia.camera.ui.camera.impl

/**
 * 拍摄界面的有关视频View的接口
 *
 * @author zhongjh
 * @date 2022/8/23
 */
interface ICameraVideo {

    /**
     * 生命周期onDestroy
     */
    fun onDestroy()

    /**
     * 录制视频
     */
    fun recordVideo()

    /**
     * 录像暂停
     *
     * @param recordedDurationNanos 当前视频持续时间：纳米单位
     */
    fun onRecordPause(recordedDurationNanos: Long)

    /**
     * 视频开始录制
     */
    fun onRecordStart()

    /**
     * 视频录制成功
     *
     * @param path 视频录制结束后提供的路径
     * @param uri 视频录制结束后提供的uri
     */
    fun onRecordSuccess(path: String, uri: String)
}
