package com.zhongjh.albumcamerarecorder.camera;

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

        /**
         * 停止录像的回调
         * @param url 视频地址
         */
        void recordResult(String url);
    }

    interface FocusCallback {
        void focusSuccess();

    }

    interface TakePictureCallback {
        void captureResult(Bitmap bitmap, boolean isVertical);
    }



}
