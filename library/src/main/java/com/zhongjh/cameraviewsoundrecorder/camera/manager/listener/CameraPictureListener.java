package com.zhongjh.cameraviewsoundrecorder.camera.manager.listener;

import com.zhongjh.cameraviewsoundrecorder.camera.listener.OnCameraResultListener;

import java.io.File;

public interface CameraPictureListener {

    void onPictureTaken(byte[] bytes, File photoFile, OnCameraResultListener callback);

    void onPictureTakeError();
}
