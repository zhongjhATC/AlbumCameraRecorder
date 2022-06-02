package com.zhongjh.albumcamerarecorder.camera.adapter;

import com.zhongjh.albumcamerarecorder.camera.entity.BitmapData;

/**
 * 多图的AdapterListener
 *
 * @author zhongjh
 */
public interface PhotoAdapterListener {

    /**
     * 点击图片事件
     */
    void onClick();

    /**
     * 删除该图片
     *
     * @param bitmapData 数据
     */
    void onDelete(BitmapData bitmapData);

}
