package com.zhongjh.multimedia.preview.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.github.chrisbanes.photoview.PhotoView
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.enums.MimeType
import com.zhongjh.common.utils.SdkVersionUtils
import com.zhongjh.multimedia.AlbumCameraRecorderFileProvider
import com.zhongjh.multimedia.R
import com.zhongjh.multimedia.preview.adapter.PreviewPagerAdapter.PreviewViewHolder
import com.zhongjh.multimedia.settings.GlobalSpec.imageEngine
import java.io.File
import java.lang.ref.WeakReference

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

    private var onListener: WeakReference<OnListener>? = null

    fun setOnListener(listener: OnListener) {
        this.onListener = WeakReference(listener)
    }

    interface OnListener {
        /**
         * adapter显示view时的触发事件
         */
        fun onViewFirstAttachedToWindow(holder: PreviewViewHolder)

        /**
         * 播放视频或者音频触发
         */
        fun onVideoPlay(videoView: VideoView)
    }

    override fun onViewAttachedToWindow(holder: PreviewViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (!isFirstAttachedToWindow) {
            // 只有第一次初始化该Adapter才触发
            onListener?.get()?.onViewFirstAttachedToWindow(holder)
            isFirstAttachedToWindow = true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviewViewHolder {
        val holder = PreviewViewHolder(LayoutInflater.from(mActivity).inflate(R.layout.fragment_preview_item_zjh, parent, false))
        return holder
    }

    override fun onBindViewHolder(holder: PreviewViewHolder, position: Int) {
        mViewHolderCache[position] = holder
        val item = items[position]
        if (item.isVideo()) {
            holder.videoPlayButton.visibility = View.VISIBLE
            holder.videoPlayButton.setOnClickListener {
                // 播放视频
                startSystemPlayerVideo(mContext, item.path)
            }
            item.loadImage(mContext, imageEngine, holder.imageView)
        } else if (item.isAudio()) {
            holder.videoPlayButton.visibility = View.VISIBLE
            holder.videoPlayButton.setOnClickListener {
                // 播放音频
                startSystemPlayerVideo(mContext, item.path)
            }
            item.loadImage2(mContext, imageEngine, holder.imageView)
        } else {
            holder.videoPlayButton.visibility = View.GONE
            item.loadImage(mContext, imageEngine, holder.imageView)
        }

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

    fun onDestroy() {
        mViewHolderCache.clear()
    }

    /**
     * 打开系统的播放视频
     */
    private fun startSystemPlayerVideo(context: Context, path: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        val isParseUri = MimeType.isContent(path) || MimeType.isHasHttp(path)
        val data = if (SdkVersionUtils.isQ) {
            if (isParseUri) Uri.parse(path) else Uri.fromFile(File(path))
        } else if (SdkVersionUtils.isN) {
            if (isParseUri) Uri.parse(path) else AlbumCameraRecorderFileProvider.getUriForFile(context, context.packageName + ".zhongjhProvider", File(path))
        } else {
            if (isParseUri) Uri.parse(path) else Uri.fromFile(File(path))
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(data, "video/*")
        context.startActivity(intent)
    }

    class PreviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var videoPlayButton: View = itemView.findViewById(R.id.video_play_button)
        var imageView: PhotoView = itemView.findViewById(R.id.image_view)
    }
}