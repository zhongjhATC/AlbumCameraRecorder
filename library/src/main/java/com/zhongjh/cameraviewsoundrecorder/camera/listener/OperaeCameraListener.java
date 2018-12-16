package com.zhongjh.cameraviewsoundrecorder.camera.listener;

import android.graphics.Bitmap;

import java.util.HashMap;

/**
 * 拍照录像
 * 操作按钮的Listener
 */
public interface OperaeCameraListener {

    void cancel();

    void confirm(HashMap<Integer, Bitmap> captureBitmaps);

}
