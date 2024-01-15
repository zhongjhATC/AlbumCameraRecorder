package com.zhongjh.displaymedia.apapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.numberprogressbar.NumberProgressBar
import com.zhongjh.common.entity.RecordingItem
import com.zhongjh.displaymedia.R
import com.zhongjh.displaymedia.entity.DisplayMedia
import com.zhongjh.displaymedia.entity.DisplayMedia.CREATOR.FULL_PERCENT
import com.zhongjh.displaymedia.widget.AudioView
import java.util.ArrayList

class AudioAdapter(
    private val mContext: Context,
    private val audioDeleteColor: Int, private val audioProgressColor: Int, private val audioPlayColor: Int
) : RecyclerView.Adapter<AudioAdapter.VideoHolder>() {

    companion object {
        val TAG: String = AudioAdapter::class.java.simpleName
    }

    private val mInflater: LayoutInflater = LayoutInflater.from(mContext)

    /**
     * 音频数据源
     */
    val list = ArrayList<DisplayMedia>()

    /**
     * 每次添加数据增长的id
     */
    private var mId: Long = 0

    /**
     * 是否允许操作(一般只用于展览作用)
     */
    var isOperation = true

    var callback: Callback? = null

    interface Callback {
        /**
         * 音频删除事件
         */
        fun onRemoveRecorder(holder: VideoHolder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {
        val view: View = mInflater.inflate(R.layout.item_audio_progress_zjh, parent, false)
        val videoHolder = VideoHolder(view)

        // 设置上传音频等属性
        videoHolder.imgRemoveRecorder.setColorFilter(audioDeleteColor)
        isShowRemoveRecorder(videoHolder)
        videoHolder.numberProgressBar.setProgressTextColor(audioProgressColor)
        videoHolder.numberProgressBar.reachedBarColor = audioProgressColor
        videoHolder.tvRecorderTip.setTextColor(audioProgressColor)

        // 设置播放控件里面的播放按钮的颜色
        videoHolder.audioView.mViewHolder.imgPlay.setColorFilter(audioPlayColor)
        videoHolder.audioView.mViewHolder.tvCurrentProgress.setTextColor(audioProgressColor)
        videoHolder.audioView.mViewHolder.tvTotalProgress.setTextColor(audioProgressColor)

        return videoHolder
    }

    override fun onBindViewHolder(holder: VideoHolder, position: Int) {
        val progressMedia = list[position]

        if (progressMedia.progress == FULL_PERCENT) {
            // 显示完成后的音频
            holder.groupRecorderProgress.visibility = View.GONE
            holder.audioView.visibility = View.VISIBLE
            isShowRemoveRecorder(holder)
        }

        // 设置数据源
        val recordingItem = RecordingItem()
        recordingItem.url = progressMedia.url
        holder.audioView.setData(recordingItem, audioProgressColor)

        initListener(holder)
    }

    /**
     * 添加音频数据
     *
     * @param displayMediaList 数据集合
     */
    fun addAudioData(displayMediaList: List<DisplayMedia>) {
        Log.d("$TAG Test", "addAudioData")
        for (item in displayMediaList) {
            item.displayMediaId = mId++
        }
        list.addAll(displayMediaList)
        // 刷新ui
        notifyItemRangeInserted(list.size - displayMediaList.size, displayMediaList.size)
        notifyItemRangeChanged(list.size - displayMediaList.size, displayMediaList.size)
    }

    /**
     * 赋值音频数据
     *
     * @param displayMediaList 数据集合
     */
    fun setAudioData(displayMediaList: List<DisplayMedia>) {
        Log.d("$TAG Test", "setAudioData")
        // 删除当前所有音频
        list.clear()
        // 增加新的视频数据
        for (item in displayMediaList) {
            item.displayMediaId = mId++
        }
        list.addAll(displayMediaList)
        notifyItemRangeInserted(0, list.size)
        notifyItemRangeChanged(0, list.size)
    }

    /**
     * 初始化所有事件
     */
    private fun initListener(holder: VideoHolder) {
        // 音频删除事件
        holder.imgRemoveRecorder.setOnClickListener {
            callback?.onRemoveRecorder(holder)
            // 隐藏音频相关控件
            holder.groupRecorderProgress.visibility = View.GONE
            holder.audioView.visibility = View.GONE
            holder.imgRemoveRecorder.visibility = View.GONE
        }
    }

    /**
     * 设置是否显示删除音频按钮
     */
    private fun isShowRemoveRecorder(holder: VideoHolder) {
        if (isOperation) {
            // 如果是可操作的，就判断是否有音频数据
            if (holder.audioView.visibility == View.VISIBLE
                || holder.groupRecorderProgress.visibility == View.VISIBLE
            ) {
                holder.imgRemoveRecorder.visibility = View.VISIBLE
            } else {
                holder.imgRemoveRecorder.visibility = View.GONE
            }
        } else {
            holder.imgRemoveRecorder.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * 清空数据
     */
    fun clearAll() {
        notifyItemRangeRemoved(0, list.size)
        list.clear()
    }

    class VideoHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val numberProgressBar: NumberProgressBar = itemView.findViewById(R.id.numberProgressBar)
        val imgRemoveRecorder: ImageView = itemView.findViewById(R.id.imgRemoveRecorder)
        val groupRecorderProgress: Group = itemView.findViewById(R.id.groupRecorderProgress)
        val audioView: AudioView = itemView.findViewById(R.id.playView)
        val tvRecorderTip: TextView = itemView.findViewById(R.id.tvRecorderTip)
    }


}