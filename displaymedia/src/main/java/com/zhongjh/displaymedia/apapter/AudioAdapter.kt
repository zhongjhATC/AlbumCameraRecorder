package com.zhongjh.displaymedia.apapter

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.numberprogressbar.NumberProgressBar
import com.zhongjh.common.entity.RecordingItem
import com.zhongjh.common.utils.ThreadUtils
import com.zhongjh.common.utils.ThreadUtils.SimpleTask
import com.zhongjh.displaymedia.R
import com.zhongjh.displaymedia.entity.DisplayMedia
import com.zhongjh.displaymedia.entity.DisplayMedia.CREATOR.FULL_PERCENT
import com.zhongjh.displaymedia.listener.DisplayMediaLayoutListener
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class AudioAdapter(
    private val mContext: Context,
    private val audioDeleteColor: Int, private val audioProgressColor: Int, private val audioPlayColor: Int
) : RecyclerView.Adapter<AudioAdapter.VideoHolder>() {

    companion object {
        val TAG: String = AudioAdapter::class.java.simpleName
        const val AUDIO_IS_PLAY = "AUDIO_IS_PLAY"
    }

    /**
     * 音频数据源
     */
    val list = ArrayList<DisplayMedia>()

    /**
     * 是否允许操作(一般只用于展览作用)
     */
    var isOperation = true

    var callback: Callback? = null

    /**
     * 相关事件
     */
    var listener: DisplayMediaLayoutListener? = null

    private val mInflater: LayoutInflater = LayoutInflater.from(mContext)

    /**
     * 音频播放器，只允许播放一个
     */
    private val mMediaPlayer by lazy {
        MediaPlayer()
    }

    /**
     * 每次添加数据增长的id
     */
    private var mId: Long = 0

    /**
     * 互斥变量，防止定时器与SeekBar拖动时进度冲突
     */
    private var mIsChanging = false

    /**
     * 启动播放事件的viewHolder
     */
    private var mPlayViewHolder: VideoHolder? = null

    interface Callback {
        /**
         * 音频删除事件
         */
        fun onRemoveRecorder(holder: VideoHolder)
    }

    /**
     * 异步任务
     */
    private var mPlayTask: SimpleTask<Boolean>? = null

    private fun getCompressFileTask(): SimpleTask<Boolean>? {
        mPlayTask = object : SimpleTask<Boolean>() {
            override fun doInBackground(): Boolean {
                if (mIsChanging) {
                    return false
                }
                return true
            }

            @SuppressLint("SetTextI18n")
            override fun onSuccess(result: Boolean) {
                if (result) {
                    mPlayViewHolder?.let {
                        //设置当前播放进度
                        it.seekBar.progress = mMediaPlayer.currentPosition
                        it.tvCurrentProgress.text = generateTime(mMediaPlayer.currentPosition.toLong(), 1) + File.separator
                    }
                }
            }
        }
        return mPlayTask
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
        videoHolder.imgPlay.setColorFilter(audioPlayColor)
        videoHolder.tvCurrentProgress.setTextColor(audioProgressColor)
        videoHolder.tvTotalProgress.setTextColor(audioProgressColor)

        return videoHolder
    }

    override fun onBindViewHolder(holder: VideoHolder, position: Int) {
        val displayMedia = list[position]
        // 显示完成后的音频
        holder.showPlayView()
        isShowRemoveRecorder(holder)
        // 设置数据源
        val recordingItem = RecordingItem()
        recordingItem.path = displayMedia.path
        recordingItem.duration = displayMedia.duration
        initListener(holder, displayMedia, position)
    }

    /**
     * 示例： https://blog.csdn.net/a1064072510/article/details/82871034
     *
     * @param holder holder
     * @param position 索引
     * @param payloads   用于标识 刷新布局里面的那个具体控件
     */
    override fun onBindViewHolder(holder: VideoHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
            return
        }
        val progressMedia = list[position]
        for (payload in payloads) {
            when (payload) {
                // 设置进度条
                ImagesAndVideoAdapter.PHOTO_ADAPTER_PROGRESS -> {
                    holder.showProgress(progressMedia.progress)
                }
                AUDIO_IS_PLAY -> {
                    holder.imgPlay.setImageResource(R.drawable.ic_play_circle_outline_black_24dp_zhongjh)
                }
            }
        }
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
     * 销毁播放器
     */
    fun onDestroy() {
        // 试图停止所有正在执行的活动任务
        mPlayTask?.cancel()
        if (mMediaPlayer.isPlaying) {
            mMediaPlayer.stop()
        }
        mMediaPlayer.release()
        listener = null
    }

    /**
     * 初始化所有事件
     */
    @SuppressLint("SetTextI18n")
    private fun initListener(holder: VideoHolder, displayMedia: DisplayMedia, position: Int) {
        // 音频删除事件
        holder.imgRemoveRecorder.setOnClickListener {
            callback?.onRemoveRecorder(holder)
            list.remove(displayMedia)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, list.size - position)
        }

        // 播放按钮
        holder.imgPlay.setOnClickListener {
            // 判断该音频是否有文件地址，如果没有则请求下载
            if (!TextUtils.isEmpty(displayMedia.path)) {
                onPlay(holder, displayMedia)
            } else {
                // 调用下载
                displayMedia.url?.let {
                    listener?.onItemAudioStartDownload(holder, it)
                }
            }
        }

        // 异步准备（准备完成），准备到准备完成期间可以显示进度条之类的东西。
        mMediaPlayer.setOnPreparedListener {
            holder.seekBar.progress = 0
            holder.imgPlay.isEnabled = true
            // 当前时间
            holder.tvCurrentProgress.text = "00:00/"
            // 总计时间
            holder.tvTotalProgress.text = generateTime(mMediaPlayer.duration.toLong(), 0)
            // 设置进度条
            holder.seekBar.max = mMediaPlayer.duration
        }

        // 播放完成事件
        mMediaPlayer.setOnCompletionListener {
            // 进度归零
            mMediaPlayer.seekTo(0)
            // 进度条归零
            holder.seekBar.progress = 0
            // 控制栏中的播放按钮显示暂停状态
            holder.imgPlay.setImageResource(R.drawable.ic_play_circle_outline_black_24dp_zhongjh)
            displayMedia.videoMedia?.isPlaying = false
            // 当前时间
            holder.tvCurrentProgress.text = "00:00/"
            // 总计时间
            holder.tvTotalProgress.text = generateTime(mMediaPlayer.duration.toLong(), 0)
            // 重置并准备重新播放
            mMediaPlayer.reset()
            displayMedia.videoMedia?.isCompletion = true
        }

        // 进度条
        holder.seekBar.setOnSeekBarChangeListener(MySeekBar())
    }

    /**
     * 设置是否显示删除音频按钮
     */
    private fun isShowRemoveRecorder(holder: VideoHolder) {
        if (isOperation) {
            // 如果是可操作的，就判断是否有音频数据
            if (holder.clPlay.visibility == View.VISIBLE
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

    /**
     * 获取数量
     */
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

    /**
     * 播放音频
     */
    private fun onPlay(holder: VideoHolder, displayMedia: DisplayMedia) {
        displayMedia.videoMedia?.let {
            if (it.isPlaying) {
                // 如果当前正在播放  停止播放 更改控制栏播放状态
                if (mMediaPlayer.isPlaying) {
                    mMediaPlayer.pause()
                    holder.imgPlay.setImageResource(R.drawable.ic_play_circle_outline_black_24dp_zhongjh)
                }
            } else {
                // 暂停线程
                mPlayTask?.cancel()
                // 设置当前viewHolder
                mPlayViewHolder = holder
                // 循环所有列表设置停止
                for (i in list.indices.reversed()) {
                    list[i].videoMedia?.let { videoMedia ->
                        if (videoMedia.isPlaying) {
                            // 设置暂停
                            videoMedia.isPlaying = false
                            videoMedia.isCompletion = true
                            notifyItemChanged(i, AUDIO_IS_PLAY)
                        }
                    }
                }
                // 如果当前停止播放  继续播放 更改控制栏状态
                holder.imgPlay.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp_zhongjh)
                // 判断如果是结束了就是重新播放，否则就是继续播放
                if (it.isCompletion) {
                    try {
                        mMediaPlayer.setDataSource(displayMedia.path)
                        mMediaPlayer.prepare()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                mMediaPlayer.start()
                // 定时器 更新进度
                ThreadUtils.executeBySingleAtFixRate(getCompressFileTask(), 1, TimeUnit.SECONDS)
            }
            it.isPlaying = !it.isPlaying
            it.isCompletion = false
        }
    }

    /**
     * 进度条的进度变化事件
     */
    internal inner class MySeekBar : SeekBar.OnSeekBarChangeListener {

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            // 当进度条变化时触发
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            // 开始拖拽进度条
            mIsChanging = true
        }

        @SuppressLint("SetTextI18n")
        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            // 停止拖拽进度条
            mPlayViewHolder?.seekBar?.let {
                mMediaPlayer.seekTo(it.progress)
            }
            mPlayViewHolder?.tvCurrentProgress?.let {
                it.text = generateTime(mMediaPlayer.currentPosition.toLong(), 0) + File.separator
            }
            mIsChanging = false
        }
    }

    /**
     * 时间
     */
    private fun generateTime(time: Long, secondsAdd: Int): String {
        val totalSeconds = (time / 1000).toInt()
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        return if (hours > 0) {
            java.lang.String.format(Locale.CANADA, "%02d:%02d:%02d", hours, minutes, seconds + secondsAdd)
        } else {
            java.lang.String.format(Locale.CANADA, "%02d:%02d", minutes, seconds + secondsAdd)
        }
    }

    class VideoHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val numberProgressBar: NumberProgressBar = itemView.findViewById(R.id.numberProgressBar)
        val imgRemoveRecorder: ImageView = itemView.findViewById(R.id.imgRemoveRecorder)
        val groupRecorderProgress: Group = itemView.findViewById(R.id.groupRecorderProgress)
        val tvRecorderTip: TextView = itemView.findViewById(R.id.tvRecorderTip)

        val clPlay: ConstraintLayout = itemView.findViewById(R.id.clPlay)
        val imgPlay: ImageView = itemView.findViewById(R.id.imgPlay)
        val seekBar: SeekBar = itemView.findViewById(R.id.seekBar)
        val tvCurrentProgress: TextView = itemView.findViewById(R.id.tvCurrentProgress)
        val tvTotalProgress: TextView = itemView.findViewById(R.id.tvTotalProgress)

        /**
         * 显示进度的view
         */
        fun showProgress(progress: Int) {
            if (progress == FULL_PERCENT) {
                showPlayView()
            } else {
                // 显示进度中的
                groupRecorderProgress.visibility = View.VISIBLE
                clPlay.visibility = View.INVISIBLE
                numberProgressBar.progress = progress
            }
        }

        /**
         * 显示播放的view
         */
        fun showPlayView() {
            groupRecorderProgress.visibility = View.GONE
            clPlay.visibility = View.VISIBLE
        }

    }


}