package com.zhongjh.albumcamerarecorder.camera.adapter;

import com.zhongjh.albumcamerarecorder.camera.entity.BitmapData;

/**
 * 处理异常的Listener
 * @author zhongjh
 */
public interface PhotoAdapterListener {

    /**
     * 删除该图片
     */
    void onDelete(BitmapData bitmapData);

}
