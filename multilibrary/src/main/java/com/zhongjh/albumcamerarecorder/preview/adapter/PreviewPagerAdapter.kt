package com.zhongjh.albumcamerarecorder.preview.adapter

import com.zhongjh.albumcamerarecorder.settings.GlobalSpec.imageEngine
import android.app.Activity
import android.content.Context
import com.zhongjh.albumcamerarecorder.preview.adapter.PreviewPagerAdapter.PreviewViewHolder
import com.zhongjh.common.entity.LocalMedia
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.zhongjh.albumcamerarecorder.R
import com.github.chrisbanes.photoview.PhotoView
import com.zhongjh.common.utils.BitmapUtils
import com.zhongjh.common.utils.MediaUtils
import java.util.ArrayList

/**
 * @author zhongjh
 */
class PreviewPagerAdapter(private val mContext: Context, private val mActivity: Activity) :
    RecyclerView.Adapter<PreviewViewHolder>() {

    /**
     * 数据源
     */
    private val items = ArrayList<LocalMedia>()

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

        val size = getRealSizeFromMedia(item)
        val mediaComputeSize = BitmapUtils.getComputeImageSize(size[0], size[1])
        val width = mediaComputeSize[0]
        val height = mediaComputeSize[1]
        imageEngine.loadUrlImage(
            mContext,
            width,
            height,
            holder.imageView,
            item.editorPath ?: item.path
        )

        if (MediaUtils.isLongImage(item.width, item.height)) {
            holder.imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        } else {
            holder.imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        }
//        if (item.getUri() != null) {
//            Point size = PhotoMetadataUtils.getBitmapSize(item.getUri(), mActivity);
//            if (item.isGif()) {
//                GlobalSpec.INSTANCE.getImageEngine().loadGifImage(mContext, size.x, size.y, holder.imageView,
//                        item.getUri());
//            } else {
//                GlobalSpec.INSTANCE.getImageEngine().loadImage(mContext, size.x, size.y, holder.imageView,
//                        item.getUri());
//
//            }
//        } else if (item.getUrl() != null) {
//            GlobalSpec.INSTANCE.getImageEngine().loadUrlImage(mContext, holder.imageView,
//                    item.getUrl());
//        } else if (item.getDrawableId() != -1) {
//            GlobalSpec.INSTANCE.getImageEngine().loadDrawableImage(mContext, holder.imageView,
//                    item.getDrawableId());
//        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    val size: Int
        get() = items.size

    fun getLocalMedia(position: Int): LocalMedia? {
        return if (size > 0 && position < size) items[position] else null
    }

    fun getCurrentViewHolder(position: Int): PreviewViewHolder? {
        return mViewHolderCache[position]
    }

    fun setLocalMedia(position: Int, localMedia: LocalMedia) {
        items[position] = localMedia
    }

    fun addAll(items: List<LocalMedia>) {
        this.items.addAll(items)
    }

    private fun getRealSizeFromMedia(media: LocalMedia): IntArray {
        return intArrayOf(media.width, media.height)
    }

    class PreviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var videoPlayButton: View
        var imageView: PhotoView

        init {
            videoPlayButton = itemView.findViewById(R.id.video_play_button)
            imageView = itemView.findViewById(R.id.image_view)
        }
    }
}