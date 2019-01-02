package com.zhongjh.cameraviewsoundrecorder.camera;

import android.graphics.Rect;

import com.zhongjh.cameraviewsoundrecorder.camera.listener.ErrorListener;

/**
 * Created by zhongjh on 2019/1/2.
 */

public interface CameraInterface {

    /**
     * 切换摄像头
     */
    void switchCamera();

    /**
     * 拍照
     */
    void takePicture();

    /**
     * 开始录像
     */
    void recordStart();

    /**
     * 录像结束
     */
    void recordEnd();

    /**
     * 缩放
     */
    void recordZoom(Rect zoom);

    /**
     * 赋值异常事件
     */
    void setErrorLinsenter(ErrorListener errorLisenter);

}
