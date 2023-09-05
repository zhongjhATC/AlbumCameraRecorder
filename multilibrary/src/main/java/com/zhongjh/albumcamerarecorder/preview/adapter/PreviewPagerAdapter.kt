package com.zhongjh.albumcamerarecorder.preview.adapter

import com.zhongjh.albumcamerarecorder.settings.GlobalSpec.imageEngine
import android.app.Activity
import android.content.Context
import com.zhongjh.albumcamerarecorder.preview.adapter.PreviewPagerAdapter.PreviewViewHolder
import com.zhongjh.common.entity.LocalMedia
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.zhongjh.albumcamerarecorder.R
import com.github.chrisbanes.photoview.PhotoView
import java.util.ArrayList

/**
 * @author zhongjh
 */
class PreviewPagerAdapter(private val mContext: Context, private val mActivity: Activity) :
    RecyclerView.Adapter<PreviewViewHolder>() {
    val items = ArrayList<LocalMedia>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviewViewHolder {
        return PreviewViewHolder(
            LayoutInflater.from(mActivity)
                .inflate(R.layout.fragment_preview_item_zjh, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PreviewViewHolder, position: Int) {
        val item = items[position]
        if (item.isVideo()) {
            holder.videoPlayButton.visibility = View.VISIBLE
            holder.videoPlayButton.setOnClickListener { v: View? -> }
        } else {
            holder.videoPlayButton.visibility = View.GONE
        }
        imageEngine.loadUrlImage(
            mContext, holder.imageView,
            item.path
        )

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

    fun setMediaItem(position: Int, localMedia: LocalMedia) {
        items[position] = localMedia
    }

    fun addAll(items: List<LocalMedia>?) {
        items.addAll(items)
    }

    internal class PreviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var videoPlayButton: View
        var imageView: PhotoView

        init {
            videoPlayButton = itemView.findViewById(R.id.video_play_button)
            imageView = itemView.findViewById(R.id.image_view)
        }
    }
}