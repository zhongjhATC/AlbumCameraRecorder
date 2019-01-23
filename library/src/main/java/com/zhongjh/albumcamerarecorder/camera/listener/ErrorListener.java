package com.zhongjh.albumcamerarecorder.camera.listener;

/**
 * 处理异常的Listener
 */
public interface ErrorListener {
    void onError();

    void AudioPermissionError();
}
