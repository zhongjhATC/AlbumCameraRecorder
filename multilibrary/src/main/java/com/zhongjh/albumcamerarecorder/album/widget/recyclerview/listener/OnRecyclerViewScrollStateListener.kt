package com.zhongjh.albumcamerarecorder.album.widget.recyclerview.listener

/**
 * @author：zhongjh
 * 滑动时的速度触发事件
 */
interface OnRecyclerViewScrollStateListener {
    /**
     * RecyclerView 滑动快速时触发
     */
    fun onScrollFast()

    /**
     * RecyclerView 滑动缓慢时触发
     */
    fun onScrollSlow()
}