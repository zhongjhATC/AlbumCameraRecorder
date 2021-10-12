package com.zhongjh.albumcamerarecorder.camera.listener;

import com.zhongjh.albumcamerarecorder.camera.entity.BitmapData;

import java.util.HashMap;
import java.util.List;

/**
 * 拍摄后操作图片的事件
 *
 * @author zhongjh
 * @date 2018/10/11
 */
public interface CaptureListener {

    /**
     * 删除图片后剩下的相关数据
     *
     * @param captureData 数据源
     */
    void remove(List<BitmapData> captureData);

    /**
     * 添加图片
     *
     * @param captureDatas 图片数据
     */
    void add(List<BitmapData> captureDatas);

}
