package com.zhongjh.multimedia.recorder

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Chronometer
import android.widget.RelativeLayout
import android.widget.Toast
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.enums.MediaType
import com.zhongjh.common.enums.MimeType
import com.zhongjh.common.listener.OnProgressUpdateListener
import com.zhongjh.common.utils.FileUtils.copy
import com.zhongjh.common.utils.StatusBarUtils.getStatusBarHeight
import com.zhongjh.common.utils.ThreadUtils
import com.zhongjh.multimedia.BaseFragment
import com.zhongjh.multimedia.MainActivity
import com.zhongjh.multimedia.R
import com.zhongjh.multimedia.camera.listener.ClickOrLongListener
import com.zhongjh.multimedia.model.SelectedData
import com.zhongjh.multimedia.recorder.widget.SoundRecordingLayout
import com.zhongjh.multimedia.settings.RecordeSpec
import com.zhongjh.multimedia.utils.FileMediaUtil.createCacheFile
import com.zhongjh.multimedia.widget.BaseOperationLayout
import com.zhongjh.multimedia.widget.clickorlongbutton.ClickOrLongButton
import java.io.File
import java.io.IOException

/**
 * 录音
 *
 * @author zhongjh
 * @date 2018/8/22
 */
class SoundRecordingFragment : BaseFragment() {
    private lateinit var mContext: Context

    /**
     * 是否正在播放中
     */
    private var isPlaying = false
    private var mViewHolder: ViewHolder? = null

    /**
     * 存储用户单击暂停按钮的时间
     */
    var timeWhenPaused: Long = 0

    private var mMediaPlayer: MediaPlayer? = null

    /**
     * 存储的数据
     */
    var localMedia: LocalMedia? = null

    /**
     * 声明一个long类型变量：用于存放上一点击“返回键”的时刻
     */
    private var mExitTime: Long = 0

    // region 有关录音配置
    private var mFile: File? = null

    private var mRecorder: MediaRecorder? = null

    private var mStartingTimeMillis: Long = 0

    // endregion
    /**
     * 停止录音时的异步线程
     */
    var mStopRecordingTask: ThreadUtils.SimpleTask<Boolean>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context.applicationContext
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mViewHolder = ViewHolder(inflater.inflate(R.layout.fragment_soundrecording_zjh, container, false))

        // 处理图片、视频等需要进度显示
        mViewHolder!!.pvLayout.soundRecordingLayoutViewHolder.btnConfirm.setProgressMode(true)

        // 初始化设置
        val mRecordSpec = RecordeSpec
        // 提示文本
        mViewHolder!!.pvLayout.setTip(resources.getString(R.string.z_multi_library_long_press_sound_recording))
        // 设置录制时间
        mViewHolder!!.pvLayout.setDuration(mRecordSpec.duration * 1000)
        mViewHolder!!.pvLayout.setReadinessDuration(mRecordSpec.readinessDuration)
        // 设置只能长按
        mViewHolder!!.pvLayout.setButtonFeatures(ClickOrLongButton.BUTTON_STATE_ONLY_LONG_CLICK)

        // 兼容沉倾状态栏
        val statusBarHeight = getStatusBarHeight(requireActivity())
        val layoutParams = mViewHolder!!.chronometer.layoutParams as RelativeLayout.LayoutParams
        layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin + statusBarHeight, layoutParams.rightMargin, layoutParams.bottomMargin)

        initListener()
        return mViewHolder!!.rootView
    }

    override fun onBackPressed(): Boolean {
        // 判断当前状态是否休闲
        if (mViewHolder!!.pvLayout.state == SoundRecordingLayout.STATE_PREVIEW) {
            return false
        } else {
            // 与上次点击返回键时刻作差
            if ((System.currentTimeMillis() - mExitTime) > AGAIN_TIME) {
                // 大于2000ms则认为是误操作，使用Toast进行提示
                Toast.makeText(mContext, resources.getString(R.string.z_multi_library_press_confirm_again_to_close), Toast.LENGTH_SHORT).show()
                // 并记录下本次点击“返回键”的时刻，以便下次进行判断
                mExitTime = System.currentTimeMillis()
                return true
            } else {
                return false
            }
        }
    }

    /**
     * 事件
     */
    private fun initListener() {
        // 录音等事件
        initPvLayoutPhotoVideoListener()
        // 播放事件
        initRlSoundRecordingClickListener()

        // 确认和取消
        initPvLayoutOperateListener()
    }

    /**
     * 录音等事件
     */
    private fun initPvLayoutPhotoVideoListener() {
        mViewHolder!!.pvLayout.setPhotoVideoListener(object : ClickOrLongListener {
            override fun actionDown() {
                // 母窗体禁止滑动
                (requireActivity() as MainActivity).showHideTableLayout(false)
            }

            override fun onClick() {
            }

            override fun onLongClick() {
                Log.d(TAG, "onLongClick")
                // 录音开启
                onRecord(true, false)
            }

            override fun onLongClickEnd(time: Long) {
                mViewHolder!!.pvLayout.hideBtnClickOrLong()
                mViewHolder!!.pvLayout.startShowLeftRightButtonsAnimator(true)
                Log.d(TAG, "onLongClickEnd")
                // 录音结束
                onRecord(false, false)
                showRecordEndView()
            }

            override fun onLongClickFinish() {
            }

            override fun onLongClickError() {
            }

            override fun onBanClickTips() {
            }

            override fun onClickStopTips() {
            }
        })
    }

    /**
     * 播放事件
     */
    private fun initRlSoundRecordingClickListener() {
        mViewHolder!!.pvLayout.soundRecordingLayoutViewHolder.rlSoundRecording.setOnClickListener { view: View? ->
            initAudio()
            // 播放
            onPlay(isPlaying)
            isPlaying = !isPlaying
        }
    }

    /**
     * 确认和取消
     */
    private fun initPvLayoutOperateListener() {
        mViewHolder!!.pvLayout.setOperateListener(object : BaseOperationLayout.OperateListener {
            /** @noinspection unused
             */
            override fun beforeConfirm(): Boolean {
                return true
            }

            /** @noinspection unused
             */
            override fun cancel() {
                // 母窗体启动滑动
                (requireActivity() as MainActivity).showHideTableLayout(true)
                // 重置取消确认按钮
                mViewHolder!!.pvLayout.reset()
                // 重置时间
                mViewHolder!!.chronometer.base = SystemClock.elapsedRealtime()
            }

            /** @noinspection unused
             */
            override fun startProgress() {
                moveRecordFile()
            }

            /** @noinspection unused
             */
            override fun stopProgress() {
            }

            /** @noinspection unused
             */
            override fun doneProgress() {
            }
        })
    }

    /**
     * 初始化音频的数据
     */
    private fun initAudio() {
        // 获取service存储的数据
        localMedia = LocalMedia()
        val sharePreferences = requireActivity().getSharedPreferences("sp_name_audio", Context.MODE_PRIVATE)
        val filePath = sharePreferences.getString("audio_path", "")
        val elapsed = sharePreferences.getLong("elapsed", 0)
        localMedia!!.path = filePath!!
        localMedia!!.duration = elapsed
        localMedia!!.size = File(filePath).length()
        localMedia!!.mimeType = MimeType.AAC.mimeTypeName
    }

    override fun onPause() {
        super.onPause()
        if (mMediaPlayer != null) {
            stopPlaying()
        }
    }

    override fun onDestroy() {
        mViewHolder!!.pvLayout.onDestroy()
        if (mMediaPlayer != null) {
            stopPlaying()
        }
        mMoveRecordFileTask.cancel()
        if (mStopRecordingTask != null) {
            mStopRecordingTask!!.cancel()
        }
        super.onDestroy()
    }


    /**
     * 录音开始或者停止
     * // recording pause
     *
     * @param start   录音开始或者停止
     * @param isShort 短时结束不算
     * @noinspection SameParameterValue
     */
    private fun onRecord(start: Boolean, isShort: Boolean) {
        if (start) {
            // 创建文件
            val folder = File(requireActivity().getExternalFilesDir(null).toString() + "/SoundRecorder")
            if (!folder.exists()) {
                // folder /SoundRecorder doesn't exist, create the folder
                val wasSuccessful = folder.mkdir()
                if (!wasSuccessful) {
                    println("was not successful.")
                }
            }
            Log.d(TAG, "onRecord")

            // start RecordingService
            startRecording()
            // keep screen on while recording
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            mViewHolder!!.chronometer.stop()
            timeWhenPaused = 0

            stopRecording(isShort)
            // allow the screen to turn off again once recording is finished
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    /**
     * 播放开始或者停止
     * // Play start/stop
     *
     * @param isPlaying 播放或者停止
     */
    private fun onPlay(isPlaying: Boolean) {
        if (!isPlaying) {
            // currently MediaPlayer is not playing audio
            if (mMediaPlayer == null) {
                startPlaying() // 第一次播放
            } else {
                resumePlaying() // 恢复当前暂停的媒体播放器
            }
        } else {
            // 暂停播放
            pausePlaying()
        }
    }

    /**
     * 播放MediaPlayer
     */
    private fun startPlaying() {
        // 变成等待的图标
        mViewHolder!!.pvLayout.soundRecordingLayoutViewHolder.ivRecord.setImageResource(R.drawable.ic_pause_white_24dp)
        mMediaPlayer = MediaPlayer()

        try {
            // 文件地址
            mMediaPlayer!!.setDataSource(localMedia!!.path)
            mMediaPlayer!!.prepare()

            mMediaPlayer!!.setOnPreparedListener { mp: MediaPlayer? -> mMediaPlayer!!.start() }
        } catch (e: IOException) {
            Log.e(TAG, "prepare() failed")
        }

        mMediaPlayer!!.setOnCompletionListener { mp: MediaPlayer? -> stopPlaying() }

        //keep screen on while playing audio
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    /**
     * 恢复播放
     */
    private fun resumePlaying() {
        // 暂停图
        mViewHolder!!.pvLayout.soundRecordingLayoutViewHolder.ivRecord.setImageResource(R.drawable.ic_pause_white_24dp)
        mMediaPlayer!!.start()
    }

    /**
     * 暂停播放
     */
    private fun pausePlaying() {
        // 设置成播放的图片
        mViewHolder!!.pvLayout.soundRecordingLayoutViewHolder.ivRecord.setImageResource(R.drawable.ic_play_arrow_white_24dp)
        mMediaPlayer!!.pause()
    }

    /**
     * 停止播放
     */
    private fun stopPlaying() {
        // 设置成播放的图片
        mViewHolder!!.pvLayout.soundRecordingLayoutViewHolder.ivRecord.setImageResource(R.drawable.ic_play_arrow_white_24dp)
        // 停止mediaPlayer
        mMediaPlayer!!.stop()
        mMediaPlayer!!.reset()
        mMediaPlayer!!.release()
        mMediaPlayer = null

        isPlaying = !isPlaying

        // 一旦音频播放完毕，保持屏幕常亮 这个设置关闭
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    /**
     * 显示录音后的界面
     */
    private fun showRecordEndView() {
        // 录音按钮转变成播放按钮，播放录音
        mViewHolder!!.pvLayout.soundRecordingLayoutViewHolder.ivRecord.setImageResource(R.drawable.ic_play_arrow_white_24dp)
    }

    /**
     * 迁移语音文件
     */
    private fun moveRecordFile() {
        // 执行等待动画
        mViewHolder!!.pvLayout.soundRecordingLayoutViewHolder.btnConfirm.setProgress(1)
        // 开始迁移文件
        ThreadUtils.executeByIo(mMoveRecordFileTask)
    }

    /**
     * 迁移语音的异步线程
     */
    private val mMoveRecordFileTask: ThreadUtils.SimpleTask<Void?> = object : ThreadUtils.SimpleTask<Void?>() {
        override fun doInBackground(): Void {
            if (localMedia == null) {
                initAudio()
            }
            localMedia!!.path
            // 初始化保存好的音频文件
            initAudio()
            // 获取文件名称
            val newFileName = localMedia!!.path.substring(localMedia!!.path.lastIndexOf(File.separator))
            val newFile = createCacheFile(mContext, MediaType.TYPE_AUDIO)
            Log.d(TAG, "newFile" + newFile.absolutePath)
            copy(File(localMedia!!.path), newFile, null) { ioProgress: Double, file: File? ->
                val progress = (ioProgress * FULL).toInt()
                ThreadUtils.runOnUiThread {
                    mViewHolder!!.pvLayout.soundRecordingLayoutViewHolder.btnConfirm.addProgress(progress)
                    localMedia!!.path = newFile.path
                    if (progress >= FULL) {
                        val result = Intent()
                        val localFiles = ArrayList<LocalMedia?>()
                        localFiles.add(localMedia)
                        result.putParcelableArrayListExtra(SelectedData.STATE_SELECTION, localFiles)
                        requireActivity().setResult(Activity.RESULT_OK, result)
                        requireActivity().finish()
                    }
                }
            }
            return null
        }

        /** @noinspection unused
         */
        override fun onSuccess(result: Void?) {
        }
    }

    // region 有关录音相关方法
    /**
     * 开始录音
     */
    private fun startRecording() {
        // 设置音频路径
        mFile = createCacheFile(mContext, MediaType.TYPE_AUDIO)

        mRecorder = MediaRecorder()
        mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
        mRecorder!!.setOutputFile(mFile!!.path)
        mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mRecorder!!.setAudioChannels(1)

        try {
            mRecorder!!.prepare()
            mRecorder!!.start()
            // 开始计时,从0秒开始算起
            mViewHolder!!.chronometer.base = SystemClock.elapsedRealtime()
            mViewHolder!!.chronometer.start()
            mStartingTimeMillis = System.currentTimeMillis()
        } catch (e: IOException) {
            Log.e(TAG, "prepare() failed")
        }
    }

    /**
     * 停止录音
     *
     * @param isShort 短时结束不算
     */
    private fun stopRecording(isShort: Boolean) {
        mViewHolder!!.pvLayout.isEnabled = false
        ThreadUtils.executeByIo(getStopRecordingTask(isShort))
    }

    /**
     * 停止录音的异步线程
     *
     * @param isShort 短时结束不算
     * @noinspection unused
     */
    private fun getStopRecordingTask(isShort: Boolean): ThreadUtils.SimpleTask<Boolean> {
        mStopRecordingTask = object : ThreadUtils.SimpleTask<Boolean?>() {
            override fun doInBackground(): Boolean {
                if (isShort) {
                    // 如果是短时间的，删除该文件
                    if (mFile!!.exists()) {
                        val delete = mFile!!.delete()
                        if (!delete) {
                            println("file not delete.")
                        }
                    }
                } else {
                    val mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis)
                    // 存储到缓存的文件地址
                    requireActivity().getSharedPreferences("sp_name_audio", Context.MODE_PRIVATE)
                        .edit()
                        .putString("audio_path", mFile!!.path)
                        .putLong("elapsed", mElapsedMillis)
                        .apply()
                }
                if (mRecorder != null) {
                    try {
                        mRecorder!!.stop()
                    } catch (ignored: RuntimeException) {
                        // 防止立即录音完成
                    }
                    mRecorder!!.release()
                    mRecorder = null
                }
                return true
            }

            override fun onSuccess(result: Boolean) {
                mViewHolder!!.pvLayout.isEnabled = true
            }

            override fun onCancel() {
                super.onCancel()
                mViewHolder!!.pvLayout.isEnabled = true
            }

            override fun onFail(t: Throwable) {
                super.onFail(t)
                mViewHolder!!.pvLayout.isEnabled = true
            }
        }
        return mStopRecordingTask!!
    }


    // endregion
    class ViewHolder(val rootView: View) {
        val chronometer: Chronometer = rootView.findViewById(R.id.chronometer)
        val pvLayout: SoundRecordingLayout = rootView.findViewById(R.id.pvLayout)
    }

    companion object {
        private val TAG: String = SoundRecordingFragment::class.java.simpleName

        /**
         * 再次确定的2秒时间
         */
        private const val AGAIN_TIME = 2000

        /**
         * 满进度
         */
        private const val FULL = 100
        fun newInstance(): SoundRecordingFragment {
            return SoundRecordingFragment()
        }
    }
}
