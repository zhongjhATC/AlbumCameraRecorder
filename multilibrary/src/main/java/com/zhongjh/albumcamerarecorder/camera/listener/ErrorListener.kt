package com.zhongjh.albumcamerarecorder.camera.listener

/**
 * 处理异常的Listener
 * @author zhongjh
 */
interface ErrorListener {
    /**
     * 发现异常
     */
    fun onError()

    /**
     * 发现权限异常
     */
    fun onAudioPermissionError()
}