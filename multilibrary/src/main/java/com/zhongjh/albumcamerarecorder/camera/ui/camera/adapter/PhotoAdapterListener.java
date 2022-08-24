package com.zhongjh.albumcamerarecorder.camera.ui.camera.adapter;

import android.content.Intent;

import com.zhongjh.albumcamerarecorder.camera.entity.BitmapData;

/**
 * 多图的AdapterListener
 *
 * @author zhongjh
 */
public interface PhotoAdapterListener {

    /**
     * 点击图片事件
     *
     * @param intent 点击后，封装相关数据进入该intent
     */
    void onPhotoAdapterClick(Intent intent);

    /**
     * 删除该图片
     *
     * @param bitmapData 数据
     * @param position   删除的索引
     */
    void onPhotoAdapterDelete(BitmapData bitmapData, int position);

}
