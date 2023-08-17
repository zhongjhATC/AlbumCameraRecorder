package com.zhongjh.albumcamerarecorder.album.widget.recyclerview.listener

/**
 * @author：zhongjh
 * 滑动时触发事件
 */
interface OnRecyclerViewScrollListener {

    /**
     * 当RecyclerView滑动时触发事件
     * @param dx 以像素为单位滚动的水平距离
     * @param dy 以像素为单位滚动的垂直距离
     */
    fun onScrolled(dx: Int, dy: Int)

    /**
     * 当RecyclerView滚动状态改变时触发事件
     * @param state 滚动状态：SCROLL_STATE_IDLE SCROLL_STATE_DRAG SCROLL_STATE_SETTLE
     */
    fun onScrollStateChanged(state: Int)
}