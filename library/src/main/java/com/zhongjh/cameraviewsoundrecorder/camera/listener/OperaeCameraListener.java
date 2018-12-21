package com.zhongjh.cameraviewsoundrecorder.camera.listener;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 拍照录像
 * 操作按钮的Listener
 */
public interface OperaeCameraListener {

    /**
     * 取消事件
     */
    void cancel();

    /**
     * 拍照成功后点击确认事件
     * @param paths 文件地址
     */
    void captureSuccess(ArrayList<String> paths);

    /**
     * 录像成功后点击确认事件
     * @param url url
     * @param firstFrame 文件地址
     */
    void recordSuccess(String url, Bitmap firstFrame);

}
