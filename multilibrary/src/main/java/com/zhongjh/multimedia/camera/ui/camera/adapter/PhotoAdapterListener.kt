package com.zhongjh.multimedia.camera.ui.camera.adapter

import android.content.Intent
import com.zhongjh.multimedia.camera.entity.BitmapData

/**
 * 多图的AdapterListener
 *
 * @author zhongjh
 */
interface PhotoAdapterListener {
    /**
     * 点击图片事件
     *
     * @param intent 点击后，封装相关数据进入该intent
     */
    fun onPhotoAdapterClick(intent: Intent)

    /**
     * 删除该图片
     *
     * @param bitmapData 数据
     * @param position   删除的索引
     */
    fun onPhotoAdapterDelete(bitmapData: BitmapData, position: Int)
}
