package com.zhongjh.multimedia.album.ui.mediaselection.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.zhongjh.common.entity.IncapableCause.Companion.handleCause
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.multimedia.R
import com.zhongjh.multimedia.album.entity.Album
import com.zhongjh.multimedia.album.entity.ReloadPageMediaData
import com.zhongjh.multimedia.album.ui.mediaselection.adapter.widget.MediaGrid
import com.zhongjh.multimedia.album.widget.CheckView
import com.zhongjh.multimedia.model.SelectedModel
import com.zhongjh.multimedia.settings.AlbumSpec
import kotlinx.coroutines.launch

/**
 * 相册适配器
 *
 * @author zhongjh
 */
class AlbumAdapter(
    context: Context, private val lifecycleOwner: LifecycleOwner, private val mSelectedModel: SelectedModel,
    private val placeholder: Drawable?, imageResize: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    MediaGrid.OnMediaGridClickListener {
    private val tag: String = this@AlbumAdapter.javaClass.simpleName

    private val mAlbumSpec = AlbumSpec
    private var data: MutableList<LocalMedia> = ArrayList()
    private var mCheckStateListener: CheckStateListener? = null
    private var mOnMediaClickListener: OnMediaClickListener? = null
    private val mImageResize: Int

    init {
        Log.d("onSaveInstanceState", mSelectedModel.selectedData.localMedias.size.toString() + " AlbumMediaAdapter")
        mImageResize = imageResize
    }

    /**
     * 重新赋值数据
     *
     * @param reloadPageMediaData 数据源和比较数据
     */
    fun setData(reloadPageMediaData: ReloadPageMediaData) {
        // 使用协程作用域启动后台任务
        lifecycleOwner.lifecycleScope.launch {
            // 结果自动回到主线程，更新UI
            this@AlbumAdapter.data = reloadPageMediaData.data
            reloadPageMediaData.diffResult.dispatchUpdatesTo(this@AlbumAdapter)
        }
    }

    /**
     * 添加数据
     *
     * @param positionStart 用于notifyItemInserted
     */
    fun addData(positionStart: Int) {
        notifyItemInserted(positionStart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // 相片的item
        return MediaViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.media_grid_item_zjh, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d("onSaveInstanceState", mSelectedModel.selectedData.localMedias.size.toString() + " onBindViewHolder")
        // 相片的item
        val mediaViewHolder = holder as MediaViewHolder

        val item = data[position]
        Log.d(tag, "position: $position")
        if (position == 0) {
            Log.d(tag, "path: " + item.uri)
        }
        // 传递相关的值
        mediaViewHolder.mMediaGrid.preBindMedia(MediaGrid.PreBindInfo(mImageResize, placeholder!!, mAlbumSpec.countable, holder))

        mediaViewHolder.mMediaGrid.bindMedia(item)
        mediaViewHolder.mMediaGrid.setOnMediaGridClickListener(this)
        setCheckStatus(item, mediaViewHolder.mMediaGrid)
    }

    override fun getItemId(position: Int): Long {
        // 需要返回id，否则不会重复调用onBindViewHolder，因为设置了mAdapter.setHasStableIds(true)
        return data[position].fileId
    }

    override fun getItemCount(): Int {
        Log.d(tag, "data.size(): " + data.size)
        return data.size
    }

    /**
     * 设置当前选择状态
     *
     * @param item      数据
     * @param mediaGrid holder
     */
    private fun setCheckStatus(item: LocalMedia, mediaGrid: MediaGrid) {
        Log.d("onSaveInstanceState", mSelectedModel.selectedData.localMedias.size.toString() + " setCheckStatus")
        // 是否多选时,显示数字
        if (mAlbumSpec.countable) {
            // 显示数字
            val checkedNum = mSelectedModel.selectedData.checkedNumOf(item)
            if (checkedNum > 0) {
                // 设置启用,设置数量
                mediaGrid.setCheckEnabled(true)
                mediaGrid.setCheckedNum(checkedNum)
            } else {
                // 判断当前数量 和 当前选择最大数量比较 是否相等，相等就设置为false，否则true
                if (mSelectedModel.selectedData.maxSelectableReached()) {
                    mediaGrid.setCheckEnabled(false)
                    mediaGrid.setCheckedNum(CheckView.UNCHECKED)
                } else {
                    mediaGrid.setCheckEnabled(true)
                    mediaGrid.setCheckedNum(checkedNum)
                }
            }
        } else {
            // 不显示数字
            val selected = mSelectedModel.selectedData.isSelected(item)
            // 如果被选中了，就设置选择
            if (selected) {
                mediaGrid.setCheckEnabled(true)
                mediaGrid.setChecked(true)
            } else {
                // 判断当前数量 和 当前选择最大数量比较 是否相等，相等就设置为false，否则true
                mediaGrid.setCheckEnabled(!mSelectedModel.selectedData.maxSelectableReached())
                mediaGrid.setChecked(false)
            }
        }
    }

    override fun onThumbnailClicked(imageView: ImageView, item: LocalMedia, holder: RecyclerView.ViewHolder) {
        if (mOnMediaClickListener != null) {
            mOnMediaClickListener!!.onMediaClick(null, imageView, item, holder.bindingAdapterPosition)
        }
    }

    override fun onCheckViewClicked(checkView: CheckView, item: LocalMedia, holder: RecyclerView.ViewHolder) {
        Log.d("onSaveInstanceState", mSelectedModel.selectedData.localMedias.size.toString() + " onCheckViewClicked")
        // 是否多选模式,显示数字
        if (mAlbumSpec.countable) {
            // 获取当前选择的第几个
            val checkedNum = mSelectedModel.selectedData.checkedNumOf(item)
            if (checkedNum == CheckView.UNCHECKED) {
                // 如果当前数据是未选状态
                if (assertAddSelection(holder.itemView.context, item)) {
                    // 添加选择了当前数据
                    mSelectedModel.addSelectedData(item)
                    // 刷新数据源
                    notifyCheckStateChanged()
                }
            } else {
                // 删除当前选择
                mSelectedModel.removeSelectedData(item)
                // 刷新数据
                notifyCheckStateChanged()
            }
        } else {
            // 不是多选模式
            if (mSelectedModel.selectedData.isSelected(item)) {
                // 如果当前已经被选中，再次选择就是取消了
                mSelectedModel.removeSelectedData(item)
                // 刷新数据源
                notifyCheckStateChanged()
            } else {
                if (assertAddSelection(holder.itemView.context, item)) {
                    mSelectedModel.addSelectedData(item)
                    notifyCheckStateChanged()
                }
            }
        }
    }

    /**
     * 刷新数据
     */
    @SuppressLint("NotifyDataSetChanged")
    fun notifyCheckStateChanged() {
        notifyDataSetChanged()
        if (mCheckStateListener != null) {
            mCheckStateListener!!.onUpdate()
        }
    }

    /**
     * 验证当前item是否满足可以被选中的条件
     *
     * @param context 上下文
     * @param item    数据源
     */
    private fun assertAddSelection(context: Context, item: LocalMedia): Boolean {
        val cause = mSelectedModel.selectedData.isAcceptable(item)
        handleCause(context, cause)
        return cause == null
    }

    /**
     * 注册选择事件
     *
     * @param listener 事件
     */
    fun registerCheckStateListener(listener: CheckStateListener?) {
        mCheckStateListener = listener
    }

    /**
     * 注销选择事件
     */
    fun unregisterCheckStateListener() {
        mCheckStateListener = null
    }

    /**
     * 注册图片点击事件
     *
     * @param listener 事件
     */
    fun registerOnMediaClickListener(listener: OnMediaClickListener?) {
        mOnMediaClickListener = listener
    }

    /**
     * 注销图片点击事件
     */
    fun unregisterOnMediaClickListener() {
        mOnMediaClickListener = null
    }

    interface CheckStateListener {
        /**
         * 选择选项后更新事件
         */
        fun onUpdate()
    }

    interface OnMediaClickListener {
        /**
         * 点击事件
         *
         * @param album           相册集合
         * @param imageView       图片View
         * @param item            选项
         * @param adapterPosition 索引
         * @noinspection unused
         */
        fun onMediaClick(album: Album?, imageView: ImageView?, item: LocalMedia?, adapterPosition: Int)
    }

    private class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mMediaGrid: MediaGrid = itemView as MediaGrid
    }
}
