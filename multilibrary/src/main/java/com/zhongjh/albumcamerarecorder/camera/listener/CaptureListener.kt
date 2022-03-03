package com.zhongjh.albumcamerarecorder.camera.listener

import com.zhongjh.albumcamerarecorder.camera.entity.BitmapData

/**
 * 拍摄后操作图片的事件
 *
 * @author zhongjh
 * @date 2018/10/11
 */
interface CaptureListener {

    /**
     * 删除图片后剩下的相关数据
     *
     * @param captureData 数据源
     */
    fun remove(captureData: List<BitmapData>)

    /**
     * 添加图片
     *
     * @param captureDatas 图片数据
     */
    fun add(captureDatas: List<BitmapData>)
}