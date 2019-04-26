package com.zhongjh.albumcamerarecorder.camera;

import android.graphics.Rect;

import com.zhongjh.albumcamerarecorder.camera.listener.CameraOperationListener;
import com.zhongjh.albumcamerarecorder.camera.listener.ErrorListener;

/**
 * 相机接口
 * Created by zhongjh on 2019/1/2.
 */
interface CameraInterface {

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

    /**
     * 有关该类的回调事件
     */
    void setCameraOperationListener(CameraOperationListener cameraOperationListener);

}
