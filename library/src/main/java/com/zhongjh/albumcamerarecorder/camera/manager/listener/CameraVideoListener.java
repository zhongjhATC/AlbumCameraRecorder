package com.zhongjh.albumcamerarecorder.camera.manager.listener;


import com.zhongjh.albumcamerarecorder.camera.entity.Size;
import com.zhongjh.albumcamerarecorder.camera.listener.OnCameraResultListener;

import java.io.File;

public interface CameraVideoListener {

    void onVideoRecordStarted(Size videoSize);

    void onVideoRecordStopped(File videoFile, OnCameraResultListener callback);

    void onVideoRecordError();
}
