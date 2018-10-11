package com.zhongjh.cameraviewsoundrecorder.camera.listener;

import android.graphics.Bitmap;

import java.util.HashMap;

/**
 * 拍摄后操作图片的事件
 * Created by zhongjh on 2018/10/11.
 */
public interface CaptureListener {

    /**
     * 删除图片后剩下的相关数据
     *
     * @param captureBitmaps 数据源
     */
    void remove(HashMap<Integer, Bitmap> captureBitmaps);

    /**
     * 添加图片
     *
     * @param captureBitmap  单图设置情况下的数据
     * @param captureBitmaps 多图设置情况下的数据
     */
    void add(Bitmap captureBitmap, HashMap<Integer, Bitmap> captureBitmaps);

}
