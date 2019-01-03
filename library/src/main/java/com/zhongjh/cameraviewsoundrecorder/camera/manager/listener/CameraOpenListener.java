package com.zhongjh.cameraviewsoundrecorder.camera.manager.listener;


import com.zhongjh.cameraviewsoundrecorder.camera.entity.Size;

public interface CameraOpenListener<CameraId, SurfaceListener> {

    void onCameraOpened(CameraId openedCameraId, Size previewSize, SurfaceListener surfaceListener);

    void onCameraOpenError();
}
