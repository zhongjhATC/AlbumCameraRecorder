package com.zhongjh.multimedia.album.widget.recyclerview

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhongjh.multimedia.album.widget.recyclerview.listener.OnRecyclerViewLoadMoreListener
import com.zhongjh.multimedia.album.widget.recyclerview.listener.OnRecyclerViewScrollListener
import com.zhongjh.multimedia.album.widget.recyclerview.listener.OnRecyclerViewScrollStateListener
import kotlin.math.abs

/**
 * 用于上拉加载更多的RecyclerView
 * @author zhongjh
 */
class RecyclerLoadMoreView : RecyclerView {

    companion object {
        const val BOTTOM_PRELOAD_COUNT = 2
        const val LIMIT = 150
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    /**
     * 当前是否在底部，用于判断在底部滑动时不需要第二次触发加载事件
     */
    private var isInTheBottom = false
    private var isEnabledLoadMore = false
    private var mFirstVisiblePosition = 0
    private var mLastVisiblePosition = 0

    private var onRecyclerViewScrollListener: OnRecyclerViewScrollListener? = null
    private var onRecyclerViewLoadMoreListener: OnRecyclerViewLoadMoreListener? = null
    private var onRecyclerViewScrollStateListener: OnRecyclerViewScrollStateListener? = null

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
     * 获取可视的第一个索引
     */
    fun getFirstVisiblePosition(): Int {
        return mFirstVisiblePosition
    }

    /**
     * 加载更多事件
     */
    fun setOnRecyclerViewLoadMoreListener(listener: OnRecyclerViewLoadMoreListener?) {
        onRecyclerViewLoadMoreListener = listener
    }

    /**
     * RecyclerView Start and Pause Sliding
     */
    fun setOnRecyclerViewScrollStateListener(listener: OnRecyclerViewScrollStateListener?) {
        onRecyclerViewScrollStateListener = listener
    }

    /**
     * 滑动事件
     */
    fun setOnRecyclerViewScrollListener(listener: OnRecyclerViewScrollListener?) {
        onRecyclerViewScrollListener = listener
    }

    /**
     * 滑动事件
     * @param dx 以像素为单位滚动的水平距离
     * @param dy 以像素为单位滚动的垂直距离
     */
    override fun onScrolled(dx: Int, dy: Int) {
        super.onScrolled(dx, dy)
        val layoutManager =
            layoutManager ?: throw NullPointerException("LayoutManager is null,Please check it!")
        setLayoutManagerPosition(layoutManager)
        if (onRecyclerViewLoadMoreListener != null) {
            if (isEnabledLoadMore) {
                val adapter =
                    adapter ?: throw NullPointerException("Adapter is null,Please check it!")
                // 是否到达底部
                var isReachBottom = false
                if (layoutManager is GridLayoutManager) {
                    // 获取grid模式下的行数
                    val rowCount = adapter.itemCount / layoutManager.spanCount
                    // 获取grid模式下的可视行数
                    val lastVisibleRowPosition =
                        layoutManager.findLastVisibleItemPosition() / layoutManager.spanCount
                    isReachBottom = lastVisibleRowPosition >= rowCount - BOTTOM_PRELOAD_COUNT
                }
                if (!isReachBottom) {
                    // 没有到达底部，设置底部状态
                    isInTheBottom = false
                } else {
                    // 如果已经到达底部
                    if (!isInTheBottom) {
                        // 当前不是底部状态，则触发底部加载事件
                        onRecyclerViewLoadMoreListener?.onLoadMore()
                        if (dy > 0) {
                            isInTheBottom = true
                        }
                    } else {
                        // 当前是底部状态
                        if (dy == 0) {
                            isInTheBottom = false
                        }
                    }
                }
            }
        }
        if (onRecyclerViewScrollListener != null) {
            onRecyclerViewScrollListener?.onScrolled(dx, dy)
        }
        if (onRecyclerViewScrollStateListener != null) {
            // 根据 LIMIT 来决定滑动是快速还是缓慢事件
            if (abs(dy) < LIMIT) {
                onRecyclerViewScrollStateListener?.onScrollSlow()
            } else {
                onRecyclerViewScrollStateListener?.onScrollFast()
            }
        }
    }

    /**
     * 获取RecyclerView的可见的第一个索引和最后一个索引
     */
    private fun setLayoutManagerPosition(layoutManager: LayoutManager?) {
        if (layoutManager is GridLayoutManager) {
            mFirstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
            mLastVisiblePosition = layoutManager.findLastVisibleItemPosition()
        } else if (layoutManager is LinearLayoutManager) {
            mFirstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
            mLastVisiblePosition = layoutManager.findLastVisibleItemPosition()
        }
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        if (state == SCROLL_STATE_IDLE || state == SCROLL_STATE_DRAGGING) {
            setLayoutManagerPosition(layoutManager)
        }
        if (onRecyclerViewScrollListener != null) {
            onRecyclerViewScrollListener?.onScrollStateChanged(state)
        }
        if (state == SCROLL_STATE_IDLE) {
            if (onRecyclerViewScrollStateListener != null) {
                onRecyclerViewScrollStateListener?.onScrollSlow()
            }
        }
    }

}