package com.zhongjh.cameraviewsoundrecorder.camera.other;

import android.graphics.Bitmap;

/**
 * 有关 Camera 的回调
 */
public interface CameraCallback {

    interface CameraOpenOverCallback {
        void cameraHasOpened();
    }

    /**
     * 异常回调
     */
    interface ErrorCallback {
        void onError();
    }

    /**
     * 停止录像的回调
     */
    interface StopRecordCallback {
        void recordResult(String url, Bitmap firstFrame);
    }

    interface FocusCallback {
        void focusSuccess();

    }

    interface TakePictureCallback {
        void captureResult(Bitmap bitmap, boolean isVertical);
    }



}
