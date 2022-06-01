package com.zhongjh.progresslibrary.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.LightingColorFilter
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.zhongjh.progresslibrary.R
import com.zhongjh.common.entity.RecordingItem
import com.zhongjh.progresslibrary.listener.MaskProgressLayoutListener
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

/**
 * 一个播放的view
 *
 * @author zhongjh
 * @date 2019/2/1
 */
class PlayView : FrameLayout {

    companion object {

        private val TAG = PlayView::class.java.simpleName

    }

    /**
     * 控件集合
     */
    lateinit var mViewHolder: ViewHolder

    /**
     * 相关事件
     */
    var listener: MaskProgressLayoutListener? = null

    /**
     * 当前音频数据
     */
    private lateinit var mRecordingItem: RecordingItem

    /**
     * 标记当前播放状态
     */
    private var mIsPlaying = false

    /**
     * 标记是否播放结束
     */
    private var mIsCompletion = true

    // region 有关音频

    private val mMediaPlayer = MediaPlayer()

    /**
     * 代替Timer
     * 定时器  由于功能中有付费播放功能，需要定时器来判断
     */
    private var mExecutorService: ScheduledThreadPoolExecutor? = null
    private var mTimerTask: TimerTask? = null

    /**
     * 互斥变量，防止定时器与SeekBar拖动时进度冲突
     */
    private var isChanging = false

    private val mHandler: Handler = MyHandler(context.mainLooper, this)

    // endregion 有关音频

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
    }

    private fun initView() {
        val view = View.inflate(context, R.layout.layout_play_zjh, this)
        mViewHolder = ViewHolder(view)

        // 自定义View中如果重写了onDraw()即自定义了绘制，那么就应该在构造函数中调用view的setWillNotDraw(false).
        setWillNotDraw(false)
        mViewHolder.seekBar.isEnabled = false
        initListener()
    }

    /**
     * 初始化相关数据
     *
     * @param recordingItem      音频数据源
     * @param audioProgressColor 进度条颜色
     */
    @SuppressLint("SetTextI18n")
    fun setData(recordingItem: RecordingItem, audioProgressColor: Int) {
        mRecordingItem = recordingItem
        // 设置进度条颜色
        val filter = LightingColorFilter(audioProgressColor, audioProgressColor)
        mViewHolder.seekBar.progressDrawable.colorFilter = filter
        mViewHolder.seekBar.thumb.colorFilter = filter
        mViewHolder.seekBar.isEnabled = !TextUtils.isEmpty(recordingItem.path)

        // 当前时间
        mViewHolder.tvCurrentProgress.text = "00:00/"
        // 总计时间
        if (recordingItem.duration <= 0) {
            mViewHolder.tvTotalProgress.text = resources.getString(R.string.z_progress_click_download_to_open_the_audio)
        } else {
            mViewHolder.tvTotalProgress.text = generateTime(recordingItem.duration, 0)
        }
        // 设置进度条
        mViewHolder.seekBar.max = recordingItem.duration.toInt()
    }

    /**
     * 更新总数
     */
    fun updateTotal() {
        mViewHolder.tvTotalProgress.text = generateTime(mRecordingItem.duration, 0)
    }

    /**
     * 重置播放器
     */
    fun reset() {
        // 试图停止所有正在执行的活动任务
        mExecutorService?.shutdownNow()
        mExecutorService = null
        mTimerTask = null
        if (mMediaPlayer.isPlaying) {
            mMediaPlayer.stop()
        }
        mMediaPlayer.reset()
    }

    /**
     * 销毁播放器
     */
    fun onDestroy() {
        // 试图停止所有正在执行的活动任务
        mExecutorService?.shutdownNow()
        mExecutorService = null
        mTimerTask = null
        if (mMediaPlayer.isPlaying) {
            mMediaPlayer.stop()
        }
        mMediaPlayer.release()
        listener = null
    }

    /**
     * 所有事件
     */
    @SuppressLint("SetTextI18n")
    private fun initListener() {
        // 进度条
        mViewHolder.seekBar.setOnSeekBarChangeListener(MySeekBar())

        // 播放按钮
        mViewHolder.imgPlay.setOnClickListener {
            Log.d(TAG, "setOnClickListener")
            // 判断该音频是否有文件地址，如果没有则请求下载
            if (!TextUtils.isEmpty(mRecordingItem.path)) {
                onPlay()
            } else {
                // 调用下载
                listener?.onItemAudioStartDownload(this, mRecordingItem.url!!)
            }
        }

        // 异步准备（准备完成），准备到准备完成期间可以显示进度条之类的东西。
        mMediaPlayer.setOnPreparedListener {
            Log.d(TAG, "setOnPreparedListener")
            mViewHolder.seekBar.progress = 0
            mViewHolder.imgPlay.isEnabled = true
            // 当前时间
            mViewHolder.tvCurrentProgress.text = "00:00/"
            // 总计时间
            mViewHolder.tvTotalProgress.text = generateTime(mMediaPlayer.duration.toLong(), 0)
            // 设置进度条
            mViewHolder.seekBar.max = mMediaPlayer.duration
        }

        // 播放完成事件
        mMediaPlayer.setOnCompletionListener {
            Log.d(TAG, "setOnCompletionListener")
            // 进度归零
            mMediaPlayer.seekTo(0)
            // 进度条归零
            mViewHolder.seekBar.progress = 0
            // 控制栏中的播放按钮显示暂停状态
            mViewHolder.imgPlay.setImageResource(R.drawable.ic_play_circle_outline_black_24dp_zhongjh)
            mIsPlaying = false
            // 当前时间
            mViewHolder.tvCurrentProgress.text = "00:00/"
            // 总计时间
            mViewHolder.tvTotalProgress.text = generateTime(mMediaPlayer.duration.toLong(), 0)
            // 重置并准备重新播放
            mMediaPlayer.reset()
            mIsCompletion = true
        }
    }

    private fun onPlay() {
        if (mIsPlaying) {
            // 如果当前正在播放  停止播放 更改控制栏播放状态
            if (mMediaPlayer.isPlaying) {
                mMediaPlayer.pause()
                mViewHolder.imgPlay.setImageResource(R.drawable.ic_play_circle_outline_black_24dp_zhongjh)
            }
        } else {
            // 如果当前停止播放  继续播放 更改控制栏状态
            Log.d(TAG, "播放")
            mViewHolder.imgPlay.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp_zhongjh)
            // 判断如果是结束了就是重新播放，否则就是继续播放
            if (mIsCompletion) {
                try {
                    mMediaPlayer.setDataSource(mRecordingItem.path)
                    mMediaPlayer.prepare()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            mMediaPlayer.start()
            // 定时器 更新进度
            if (mExecutorService == null) {
                mExecutorService = ScheduledThreadPoolExecutor(1, ThreadFactory { runnable: Runnable? -> Thread(runnable) })
                mTimerTask = object : TimerTask() {
                    override fun run() {
                        if (isChanging) {
                            return
                        }
                        if (mIsPlaying) {
                            mHandler.sendEmptyMessage(0)
                            mViewHolder.seekBar.progress = mMediaPlayer.currentPosition
                        }
                    }
                }
                mExecutorService!!.scheduleAtFixedRate(mTimerTask, 0, 1000, TimeUnit.MILLISECONDS)
            }
        }
        mIsPlaying = !mIsPlaying
        mIsCompletion = false
    }

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

    private class MyHandler(looper: Looper, playView: PlayView) : Handler(looper) {

        private var mPlayView = WeakReference(playView)

        @SuppressLint("SetTextI18n")
        override fun handleMessage(msg: Message) {
            val playView = mPlayView.get()
            if (playView != null) {
                super.handleMessage(msg)
                if (msg.what == 0) {
                    //设置当前播放进度
                    playView.mViewHolder.tvCurrentProgress.text = playView.generateTime(playView.mMediaPlayer.currentPosition.toLong(), 1) + File.separator
                }
            }
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
            //开始拖拽进度条
            isChanging = true
        }

        @SuppressLint("SetTextI18n")
        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            // 停止拖拽进度条
            mMediaPlayer.seekTo(mViewHolder.seekBar.progress)
            mViewHolder.tvCurrentProgress.text = generateTime(mMediaPlayer.currentPosition.toLong(), 0) + File.separator
            isChanging = false
        }

    }

    class ViewHolder(rootView: View) {
        val imgPlay: ImageView = rootView.findViewById(R.id.imgPlay)
        val seekBar: SeekBar = rootView.findViewById(R.id.seekBar)
        val tvCurrentProgress: TextView = rootView.findViewById(R.id.tvCurrentProgress)
        val tvTotalProgress: TextView = rootView.findViewById(R.id.tvTotalProgress)
    }


}