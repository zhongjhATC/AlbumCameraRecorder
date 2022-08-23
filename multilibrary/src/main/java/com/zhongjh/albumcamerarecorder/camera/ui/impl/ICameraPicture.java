package com.zhongjh.albumcamerarecorder.camera.ui.impl;

import android.content.Intent;
import android.graphics.Bitmap;

import com.zhongjh.albumcamerarecorder.camera.entity.BitmapData;

import java.util.ArrayList;

/**
 * 拍摄界面的有关图片接口
 *
 * @author zhongjh
 * @date 2022/8/23
 */
public interface ICameraPicture {

    /**
     * 添加入数据源
     *
     * @param bitmap bitmap
     */
    void addCaptureData(Bitmap bitmap);

    /**
     * 删除临时图片
     */
    void deletePhotoFile();

    /**
     * 点击图片事件
     *
     * @param intent 点击后，封装相关数据进入该intent
     */
    void onPhotoAdapterClick(Intent intent);

    /**
     * 多图进行删除的时候
     *
     * @param bitmapData 数据
     * @param position   删除的索引
     */
    void onPhotoAdapterDelete(BitmapData bitmapData, int position);

    /**
     * 刷新多个图片
     *
     * @param bitmapDatas 最新的多图数据源
     */
    void refreshMultiPhoto(ArrayList<BitmapData> bitmapDatas);

    /**
     * 刷新编辑后的单图
     *
     * @param width  最新图片的宽度
     * @param height 最新图片的高度
     */
    void refreshEditPhoto(int width, int height);

}
