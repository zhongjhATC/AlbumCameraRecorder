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
import com.zhongjh.common.utils.ThreadUtils
import com.zhongjh.common.utils.ThreadUtils.SimpleTask
import com.zhongjh.displaymedia.R
import com.zhongjh.displaymedia.entity.DisplayMedia
import com.zhongjh.displaymedia.entity.DisplayMedia.CREATOR.FULL_PERCENT
import com.zhongjh.displaymedia.entity.VideoMedia
import com.zhongjh.displaymedia.listener.DisplayMediaLayoutListener
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class AudioAdapter(
    private val mContext: Context,
    private val audioDeleteColor: Int,
    private val audioProgressColor: Int,
    private val audioPlayColor: Int
) : RecyclerView.Adapter<AudioAdapter.AudioHolder>() {

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
    private var isOperation = true

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

    interface Callback {
        /**
         * 音频播放进度事件
         */
        fun onPlayProgress(position: Int, mediaPlayerCurrentPosition: Int)

        /**
         * 音频删除触发事件
         * 主要是为了通知九宫格(显示图片视频)刷新数据
         */
        fun onRemoveRecorder()
    }

    /**
     * 异步任务
     */
    private var mPlayTask: SimpleTask<Boolean>? = null

    private fun getPlayTask(displayMedia: DisplayMedia): SimpleTask<Boolean>? {
        val position = list.indexOf(displayMedia)
        mPlayTask = object : SimpleTask<Boolean>() {
            override fun doInBackground(): Boolean {
                return !mIsChanging
            }

            @SuppressLint("SetTextI18n")
            override fun onSuccess(result: Boolean) {
                if (result) {
                    callback?.onPlayProgress(position, mMediaPlayer.currentPosition)
                }
            }
        }
        return mPlayTask
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioHolder {
        val view: View = mInflater.inflate(R.layout.item_audio_progress_zjh, parent, false)
        val audioHolder = AudioHolder(view)

        // 设置上传音频等属性
        audioHolder.imgRemoveRecorder.setColorFilter(audioDeleteColor)
        isShowRemoveRecorder(audioHolder)
        audioHolder.numberProgressBar.setProgressTextColor(audioProgressColor)
        audioHolder.numberProgressBar.reachedBarColor = audioProgressColor
        audioHolder.tvRecorderTip.setTextColor(audioProgressColor)

        // 设置播放控件里面的播放按钮的颜色
        audioHolder.imgPlay.setColorFilter(audioPlayColor)
        audioHolder.tvCurrentProgress.setTextColor(audioProgressColor)
        audioHolder.tvTotalProgress.setTextColor(audioProgressColor)

        return audioHolder
    }

    override fun onBindViewHolder(holder: AudioHolder, position: Int) {
        val displayMedia = list[position]
        // 显示完成后的音频
        holder.showPlayView()
        showAudioView(holder, displayMedia)
        isShowRemoveRecorder(holder)
        initListener(holder, displayMedia, position)
    }

    /**
     * 示例： https://blog.csdn.net/a1064072510/article/details/82871034
     *
     * @param holder holder
     * @param position 索引
     * @param payloads   用于标识 刷新布局里面的那个具体控件
     */
    override fun onBindViewHolder(holder: AudioHolder, position: Int, payloads: MutableList<Any>) {
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
    @SuppressLint("NotifyDataSetChanged")
    fun setAudioData(displayMediaList: List<DisplayMedia>) {
        Log.d("$TAG Test", "setAudioData")
        // 删除当前所有音频
        list.clear()
        // 增加新的视频数据
        for (item in displayMediaList) {
            item.displayMediaId = mId++
        }
        list.addAll(displayMediaList)
        notifyDataSetChanged()
    }

    /**
     * 更新音频数据
     */
    fun updateItem(displayMedia: DisplayMedia) {
        for (i in 0 until list.size) {
            if (list[i].displayMediaId == displayMedia.displayMediaId) {
                notifyItemChanged(i)
            }
        }
    }

    /**
     * 销毁播放器
     */
    fun onDestroy() {
        // 试图停止所有正在执行的活动任务
        stopMediaPlayer()
        mMediaPlayer.release()
        listener = null
    }

    /**
     * 停止音频
     */
    private fun stopMediaPlayer() {
        // 试图停止所有正在执行的活动任务
        mPlayTask?.cancel()
        if (mMediaPlayer.isPlaying) {
            mMediaPlayer.stop()
            mMediaPlayer.reset()
        }
    }

    /**
     * 初始化所有事件
     */
    @SuppressLint("SetTextI18n")
    private fun initListener(holder: AudioHolder, displayMedia: DisplayMedia, position: Int) {
        // 音频删除事件
        holder.imgRemoveRecorder.setOnClickListener {
            list.remove(displayMedia)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, list.size - position)
            displayMedia.videoMedia?.let {
                // 关闭音频
                if (it.isPlaying) {
                    stopMediaPlayer()
                }
            }
            callback?.onRemoveRecorder()
        }

        // 播放按钮
        holder.imgPlay.setOnClickListener {
            // 判断该音频是否有文件地址，如果没有则请求下载
            if (!TextUtils.isEmpty(displayMedia.path)) {
                onPlay(holder, displayMedia)
            } else {
                // 调用下载
                displayMedia.url?.let {
                    listener?.onItemAudioStartDownload(holder, displayMedia, it)
                }
            }
        }

        // 进度条
        holder.seekBar.setOnSeekBarChangeListener(MySeekBar(holder))
    }

    @SuppressLint("SetTextI18n")
    private fun showAudioView(holder: AudioHolder, displayMedia: DisplayMedia) {
        holder.seekBar.progress = 0
        holder.imgPlay.isEnabled = true
        // 当前时间
        holder.tvCurrentProgress.text = "00:00/"
        // 总计时间
        holder.tvTotalProgress.text = generateTime(displayMedia.duration)
        // 设置进度条
        holder.seekBar.max = displayMedia.duration.toInt()
    }

    /**
     * 设置是否显示删除音频按钮
     */
    private fun isShowRemoveRecorder(holder: AudioHolder) {
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
    @SuppressLint("NotifyDataSetChanged")
    fun clearAll() {
        list.clear()
        notifyDataSetChanged()
    }

    /**
     * 播放音频
     */
    @SuppressLint("SetTextI18n")
    private fun onPlay(holder: AudioHolder, displayMedia: DisplayMedia) {
        if (null == displayMedia.videoMedia) {
            displayMedia.videoMedia = VideoMedia()
        }
        displayMedia.videoMedia?.let {
            if (it.isPlaying) {
                // 如果当前正在播放  停止播放 更改控制栏播放状态
                if (mMediaPlayer.isPlaying) {
                    mMediaPlayer.pause()
                    holder.imgPlay.setImageResource(R.drawable.ic_play_circle_outline_black_24dp_zhongjh)
                }
            } else {
                // 暂停音频
                stopMediaPlayer()
                // 重新设置完成事件
                mMediaPlayer.setOnCompletionListener {
                    // 线程停止
                    mPlayTask?.cancel()
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
                    holder.tvTotalProgress.text = generateTime(mMediaPlayer.duration.toLong())
                    // 重置并准备重新播放
                    mMediaPlayer.reset()
                    displayMedia.videoMedia?.isCompletion = true
                }
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
                try {
                    mMediaPlayer.start()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                // 定时器 更新进度
                ThreadUtils.executeBySingleAtFixRate(getPlayTask(displayMedia), 1L, 1, TimeUnit.SECONDS)
            }
            it.isPlaying = !it.isPlaying
            it.isCompletion = false
        }
    }

    /**
     * 进度条的进度变化事件
     */
    internal inner class MySeekBar(holder: AudioHolder) : SeekBar.OnSeekBarChangeListener {

        var holder: AudioHolder? = holder

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
            holder?.seekBar?.let {
                mMediaPlayer.seekTo(it.progress)
            }
            holder?.tvCurrentProgress?.let {
                it.text = generateTime(mMediaPlayer.currentPosition.toLong()) + File.separator
            }
            mIsChanging = false
        }
    }

    /**
     * 时间
     */
    fun generateTime(time: Long): String {
        val totalSeconds = (time / 1000).toInt()
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        return if (hours > 0) {
            java.lang.String.format(Locale.CANADA, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            java.lang.String.format(Locale.CANADA, "%02d:%02d", minutes, seconds)
        }
    }

    class AudioHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

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