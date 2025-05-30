package com.zhongjh.albumcamerarecorder.preview.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.chrisbanes.photoview.PhotoView
import com.zhongjh.albumcamerarecorder.R
import com.zhongjh.albumcamerarecorder.preview.adapter.PreviewPagerAdapter.PreviewViewHolder
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec.imageEngine
import com.zhongjh.common.entity.BaseMedia
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.utils.BitmapUtils

/**
 * @author zhongjh
 */
class PreviewPagerAdapter(private val mContext: Context, private val mActivity: Activity) :
    RecyclerView.Adapter<PreviewViewHolder>() {

    /**
     * 数据源
     */
    private val items = ArrayList<BaseMedia>()

    /**
     * view的缓存
     */
    private val mViewHolderCache = LinkedHashMap<Int, PreviewViewHolder>()
    private var isFirstAttachedToWindow = false

    private var onFirstAttachedToWindowListener: OnFirstAttachedToWindowListener? = null

    fun setOnFirstAttachedToWindowListener(listener: OnFirstAttachedToWindowListener) {
        this.onFirstAttachedToWindowListener = listener
    }

    interface OnFirstAttachedToWindowListener {
        fun onViewFirstAttachedToWindow(holder: PreviewViewHolder)
    }

    override fun onViewAttachedToWindow(holder: PreviewViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (!isFirstAttachedToWindow) {
            // 只有第一次初始化该Adapter才触发
            onFirstAttachedToWindowListener?.onViewFirstAttachedToWindow(holder)
            isFirstAttachedToWindow = true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviewViewHolder {
        return PreviewViewHolder(
            LayoutInflater.from(mActivity)
                .inflate(R.layout.fragment_preview_item_zjh, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PreviewViewHolder, position: Int) {
        mViewHolderCache[position] = holder
        val item = items[position]
        if (item.isVideo()) {
            holder.videoPlayButton.visibility = View.VISIBLE
            holder.videoPlayButton.setOnClickListener { }
        } else {
            holder.videoPlayButton.visibility = View.GONE
        }

        item.loadImage(mContext, imageEngine, holder.imageView)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    val size: Int
        get() = items.size

    fun getLocalMedia(position: Int): LocalMedia? {
        return if (size > 0 && position < size) items[position] as LocalMedia else null
    }

    fun getCurrentViewHolder(position: Int): PreviewViewHolder? {
        return mViewHolderCache[position]
    }

    fun setLocalMedia(position: Int, localMedia: BaseMedia) {
        items[position] = localMedia
    }

    fun addAll(items: List<BaseMedia>) {
        this.items.addAll(items)
    }

    class PreviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var videoPlayButton: View = itemView.findViewById(R.id.video_play_button)
        var imageView: PhotoView = itemView.findViewById(R.id.image_view)
    }
}