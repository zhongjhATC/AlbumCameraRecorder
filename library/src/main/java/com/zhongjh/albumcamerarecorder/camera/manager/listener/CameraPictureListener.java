package com.zhongjh.albumcamerarecorder.camera.manager.listener;

import com.zhongjh.albumcamerarecorder.camera.listener.OnCameraResultListener;

import java.io.File;

public interface CameraPictureListener {

    void onPictureTaken(byte[] bytes, File photoFile, OnCameraResultListener callback);

    void onPictureTakeError();
}
