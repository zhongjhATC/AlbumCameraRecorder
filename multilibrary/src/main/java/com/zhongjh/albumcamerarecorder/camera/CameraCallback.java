package com.zhongjh.albumcamerarecorder.camera;

import android.graphics.Bitmap;

import java.io.File;

/**
 * 有关 Camera 的回调
 */
interface CameraCallback {

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
         *
         * @param file 视频的file
         */
        void recordResult(File file);
    }

    interface FocusCallback {
        void focusSuccess();

    }

    interface TakePictureCallback {
        void captureResult(Bitmap bitmap, boolean isVertical);
    }


}
