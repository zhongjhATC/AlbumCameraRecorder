package com.zhongjh.cameraviewsoundrecorder.camera.manager.listener;

public interface CameraCloseListener<CameraId> {
    void onCameraClosed(CameraId closedCameraId);
}
