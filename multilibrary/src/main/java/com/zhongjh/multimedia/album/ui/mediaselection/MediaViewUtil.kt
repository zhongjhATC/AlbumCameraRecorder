package com.zhongjh.multimedia.album.ui.mediaselection

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.multimedia.R
import com.zhongjh.multimedia.album.entity.Album
import com.zhongjh.multimedia.album.ui.mediaselection.adapter.AlbumAdapter
import com.zhongjh.multimedia.album.ui.mediaselection.adapter.widget.MediaGridInset
import com.zhongjh.multimedia.album.utils.UiUtils.spanCount
import com.zhongjh.multimedia.album.widget.recyclerview.RecyclerLoadMoreView
import com.zhongjh.multimedia.album.widget.recyclerview.listener.OnRecyclerViewScrollStateListener
import com.zhongjh.multimedia.model.MainModel
import com.zhongjh.multimedia.model.SelectedModel
import com.zhongjh.multimedia.settings.AlbumSpec
import com.zhongjh.multimedia.settings.AlbumSpec.thumbnailScale
import com.zhongjh.multimedia.settings.GlobalSpec.imageEngine
import com.zhongjh.multimedia.settings.GlobalSpec.onLogListener
import com.zhongjh.multimedia.utils.LifecycleFlowCollector

/**
 * 以前是MediaSelectionFragment,现在为了能滑动影响上下布局，放弃Fragment布局，直接使用RecyclerView
 * Fragment嵌套RecyclerView的话，会在配置小的机器下产生性能卡顿问题
 *
 * @param checkStateListener 单选事件
 * @param onMediaClickListener 点击事件
 *
 * @author zhongjh
 * @date 2022/9/19
 */
class MediaViewUtil(
    private val context: Context, private val owner: LifecycleOwner, private val mainModel: MainModel, private val selectedModel: SelectedModel,
    private val recyclerView: RecyclerLoadMoreView, private val placeholder: Drawable?,
    private var checkStateListener: AlbumAdapter.CheckStateListener?, private var onMediaClickListener: AlbumAdapter.OnMediaClickListener?
) : AlbumAdapter.CheckStateListener, AlbumAdapter.OnMediaClickListener {

    private var mAdapter: AlbumAdapter? = null
    private lateinit var mAlbum: Album
    private var mAlbumSpec = AlbumSpec

    init {
        init()
    }

    private fun init() {
        // 先设置recyclerView的布局
        val spanCount = if (mAlbumSpec.gridExpectedSize > 0) {
            spanCount(context, mAlbumSpec.gridExpectedSize)
        } else {
            mAlbumSpec.spanCount
        }
        // 删除动画
        recyclerView.itemAnimator = null
        recyclerView.layoutManager = GridLayoutManager(context, spanCount)
        // 需要先设置布局获取确定的spanCount，才能设置adapter
        mAdapter = AlbumAdapter(context, owner, selectedModel, placeholder, imageResize)
        Log.d("onSaveInstanceState", " mAdapter")
        mAdapter?.registerCheckStateListener(this)
        mAdapter?.registerOnMediaClickListener(this)
        mAdapter?.setHasStableIds(true)
        recyclerView.setHasFixedSize(true)

        // 加载线，recyclerView加载数据
        val spacing = context.resources.getDimensionPixelSize(R.dimen.z_media_grid_spacing)
        recyclerView.addItemDecoration(MediaGridInset(spanCount, spacing, false))
        recyclerView.adapter = mAdapter

        // 加载更多事件
        recyclerView.setOnRecyclerViewLoadMoreListener { mainModel.addAllPageMediaData(mAlbum.id, mAlbumSpec.pageSize) }

        // 滑动事件
        recyclerView.setOnRecyclerViewScrollStateListener(object : OnRecyclerViewScrollStateListener {
            override fun onScrollFast() {
                imageEngine.pauseRequests(context)
            }

            override fun onScrollSlow() {
                imageEngine.resumeRequests(context)
            }
        })

        LifecycleFlowCollector.collect(owner, mainModel.mediaPageState) { state ->
            when (state) {
                // 监听到新的相册数据
                is MainModel.MediaPageState.RefreshSuccess -> {
                    // 如果没有数据，则关闭下拉加载
                    recyclerView.setEnabledLoadMore(state.reloadPageMediaData.data.isNotEmpty())
                    mAdapter?.setReloadPageMediaData(state.reloadPageMediaData)
                    recyclerView.scrollToPosition(0)
                }

                // 监听到下拉相册数据
                is MainModel.MediaPageState.LoadMoreSuccess -> {
                    // 如果没有数据，则关闭下拉加载
                    recyclerView.setEnabledLoadMore(state.startPosition != -1)
                    mAdapter?.setData(state.data)
                    mAdapter?.notifyItemInserted(state.startPosition)
                }

                // 输出失败信息
                is MainModel.MediaPageState.Error -> {
                    onLogListener?.logError(state.cause)
                }

                else -> {}
            }
        }
    }

    fun onDestroyView() {
        mAdapter?.unregisterCheckStateListener()
        mAdapter?.unregisterOnMediaClickListener()
        checkStateListener = null
        onMediaClickListener = null
        mAdapter = null
    }

    /**
     * 每次筛选后，重新查询数据
     *
     * @param album 专辑
     */
    fun load(album: Album) {
        mAlbum = album
        mainModel.reloadPageMediaData(mAlbum.id, mAlbumSpec.pageSize)
    }

    /**
     * 刷新数据源
     */
    fun refreshMediaGrid() {
        mAdapter?.let { adapter ->
            adapter.notifyItemRangeChanged(0, adapter.itemCount)
        }
    }

    /**
     * 刷新列表数据
     */
    fun notifyItemByLocalMedia() {
        mAdapter?.notifyCheckStateChanged()
    }

    override fun onUpdate() {
        // 通知外部活动检查状态改变
        checkStateListener?.onUpdate()
    }

    override fun onMediaClick(album: Album?, imageView: ImageView?, item: LocalMedia?, adapterPosition: Int) {
        onMediaClickListener?.onMediaClick(mAlbum, imageView, item, adapterPosition)
    }

    private val imageResize: Int
        /**
         * 返回图片调整大小
         *
         * @return 列表的每个格子的宽度 * 缩放比例
         */
        get() {
            var imageResize: Int
            val lm = recyclerView.layoutManager
            var spanCount = 0
            if (lm != null) {
                spanCount = (lm as GridLayoutManager).spanCount
            }
            val screenWidth = context.resources.displayMetrics.widthPixels
            val availableWidth = screenWidth - context.resources.getDimensionPixelSize(
                R.dimen.z_media_grid_spacing
            ) * (spanCount - 1)
            // 图片调整后的大小：获取列表的每个格子的宽度
            imageResize = availableWidth / spanCount
            // 图片调整后的大小 * 缩放比例
            imageResize = (imageResize * thumbnailScale).toInt()
            return imageResize
        }
}
