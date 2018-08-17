package com.zhongjh.cameraviewsoundrecorder.listener;

import android.graphics.Bitmap;

/**
 * 提交成功的事件
 */
public interface CameraSuccessListener {

    void captureSuccess(Bitmap bitmap);

    void recordSuccess(String url, Bitmap firstFrame);

}
