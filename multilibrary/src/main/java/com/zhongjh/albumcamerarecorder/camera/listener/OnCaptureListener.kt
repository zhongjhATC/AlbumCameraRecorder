package com.zhongjh.albumcamerarecorder.camera.listener

import com.zhongjh.albumcamerarecorder.camera.entity.BitmapData

/**
 * 拍摄后操作图片的事件
 *
 * @author zhongjh
 * @date 2018/10/11
 */
interface OnCaptureListener {

    /**
     * 删除图片后剩下的相关数据
     *
     * @param captureData 数据源
     * @param position 删除的索引
     */
    fun remove(captureData: List<BitmapData>, position: Int)

    /**
     * 添加图片
     *
     * @param captureDatas 图片数据
     * @param position 添加的索引
     */
    fun add(captureDatas: List<BitmapData>, position: Int)
}