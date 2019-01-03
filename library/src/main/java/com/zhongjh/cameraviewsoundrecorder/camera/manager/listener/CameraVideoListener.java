package com.zhongjh.cameraviewsoundrecorder.camera.manager.listener;


import com.zhongjh.cameraviewsoundrecorder.camera.entity.Size;
import com.zhongjh.cameraviewsoundrecorder.camera.listener.OnCameraResultListener;

import java.io.File;

public interface CameraVideoListener {

    void onVideoRecordStarted(Size videoSize);

    void onVideoRecordStopped(File videoFile, OnCameraResultListener callback);

    void onVideoRecordError();
}
