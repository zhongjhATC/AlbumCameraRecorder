package com.zhongjh.albumcamerarecorder.album.widget.recyclerview

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/**
 * 用于上拉加载更多的RecyclerView
 * @author zhongjh
 */
class RecyclerLoadMoreView(context: Context) : RecyclerView(context) {

    companion object {
        const val BOTTOM_PRELOAD_COUNT = 2
        const val LIMIT = 150
    }

    private var isEnabledLoadMore = false

    /**
     * 设置是否加载更多
     *
     * @param isEnabledLoadMore 是否加载更多
     */
    fun setEnabledLoadMore(isEnabledLoadMore: Boolean) {
        this.isEnabledLoadMore = isEnabledLoadMore
    }

    /**
     * 是否加载更多
     */
    fun isEnabledLoadMore(): Boolean {
        return isEnabledLoadMore
    }

    /**
     * 滑动事件
     * @param dx 以像素为单位滚动的水平距离
     * @param dy 以像素为单位滚动的垂直距离
     */
    override fun onScrolled(dx: Int, dy: Int) {
    }

}