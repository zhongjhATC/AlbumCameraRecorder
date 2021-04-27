package com.zhongjh.albumcamerarecorder.camera.listener;

/**
 * 处理异常的Listener
 */
public interface ErrorListener {

    /**
     * 发现异常
     */
    void onError();

    /**
     * 发现权限异常
     */
    void onAudioPermissionError();

}
