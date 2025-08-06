package com.zhongjh.multimedia.recorder

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
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
import com.zhongjh.multimedia.utils.FileMediaUtil
import com.zhongjh.multimedia.utils.FileMediaUtil.createCacheFile
import com.zhongjh.multimedia.widget.BaseOperationLayout
import com.zhongjh.multimedia.widget.clickorlongbutton.ClickOrLongButton
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference

/**
 * 录音
 *
 * @author zhongjh
 * @date 2018/8/22
 */
class SoundRecordingFragment : BaseFragment() {
    private lateinit var context: Context

    /**
     * 是否正在播放中
     */
    private var isPlaying = false
    private lateinit var viewHolder: ViewHolder

    /**
     * 存储用户单击暂停按钮的时间
     */
    private var timeWhenPaused: Long = 0

    private var mediaPlayer: MediaPlayer? = null

    /**
     * 存储的数据
     */
    val localMedia: LocalMedia = LocalMedia()

    /**
     * 声明一个long类型变量：用于存放上一点击“返回键”的时刻
     */
    private var exitTime: Long = 0

    // region 有关录音配置
    private val file: File by lazy { createCacheFile(context, MediaType.TYPE_AUDIO) }

    private var recorder: MediaRecorder? = null

    private var startingTimeMillis: Long = 0

    // endregion
    /**
     * 停止录音时的异步线程
     */
    private var stopRecordingTask: ThreadUtils.SimpleTask<Boolean>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context.applicationContext
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewHolder = ViewHolder(inflater.inflate(R.layout.fragment_soundrecording_zjh, container, false))

        // 处理图片、视频等需要进度显示
        viewHolder.pvLayout.soundRecordingLayoutViewHolder.btnConfirm.setProgressMode(true)

        // 初始化设置
        val mRecordSpec = RecordeSpec
        // 提示文本
        viewHolder.pvLayout.setTip(resources.getString(R.string.z_multi_library_long_press_sound_recording))
        // 设置录制时间
        viewHolder.pvLayout.setDuration(mRecordSpec.duration * 1000)
        viewHolder.pvLayout.setReadinessDuration(mRecordSpec.readinessDuration)
        // 设置只能长按
        viewHolder.pvLayout.setButtonFeatures(ClickOrLongButton.BUTTON_STATE_ONLY_LONG_CLICK)

        // 兼容沉倾状态栏
        val statusBarHeight = getStatusBarHeight(requireActivity())
        val layoutParams = viewHolder.chronometer.layoutParams as RelativeLayout.LayoutParams
        layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin + statusBarHeight, layoutParams.rightMargin, layoutParams.bottomMargin)

        initListener()
        return viewHolder.rootView
    }

    override fun onBackPressed(): Boolean {
        // 判断当前状态是否休闲
        if (viewHolder.pvLayout.state == SoundRecordingLayout.STATE_PREVIEW) {
            return false
        } else {
            // 与上次点击返回键时刻作差
            if ((System.currentTimeMillis() - exitTime) > AGAIN_TIME) {
                // 大于2000ms则认为是误操作，使用Toast进行提示
                Toast.makeText(context, resources.getString(R.string.z_multi_library_press_confirm_again_to_close), Toast.LENGTH_SHORT).show()
                // 并记录下本次点击“返回键”的时刻，以便下次进行判断
                exitTime = System.currentTimeMillis()
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
        viewHolder.pvLayout.setPhotoVideoListener(object : ClickOrLongListener {
            override fun actionDown() {
                // 母窗体禁止滑动
                (requireActivity() as MainActivity).showHideTableLayout(false)
            }

            override fun onClick() {
            }

            override fun onLongClick() {
                Log.d(TAG, "onLongClick")
                // 录音开启
                onRecord(true)
            }

            override fun onLongClickEnd(time: Long) {
                viewHolder.pvLayout.hideBtnClickOrLong()
                viewHolder.pvLayout.startShowLeftRightButtonsAnimator(true)
                Log.d(TAG, "onLongClickEnd")
                // 录音结束
                onRecord(false)
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
        viewHolder.pvLayout.soundRecordingLayoutViewHolder.rlSoundRecording.setOnClickListener { view: View? ->
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
        viewHolder.pvLayout.setOperateListener(object : BaseOperationLayout.OperateListener {
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
                viewHolder.pvLayout.reset()
                // 重置时间
                viewHolder.chronometer.base = SystemClock.elapsedRealtime()
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
        val sharePreferences = requireActivity().getSharedPreferences("sp_name_audio", Context.MODE_PRIVATE)
        val filePath = sharePreferences.getString("audio_path", "") as String
        val elapsed = sharePreferences.getLong("elapsed", 0)
        val file = File(filePath)
        localMedia.absolutePath = filePath
        localMedia.uri = FileMediaUtil.getUri(context, filePath).toString()
        localMedia.duration = elapsed
        localMedia.size = File(filePath).length()
        localMedia.mimeType = MimeType.AAC.mimeTypeName
        localMedia.fileName = file.name
        localMedia.parentFolderName = file.parentFile?.name
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.let {
            stopPlaying()
        }
    }

    override fun onDestroy() {
        mediaPlayer?.let {
            stopPlaying()
        }
        // 释放MediaRecorder
        recorder?.let {
            try {
                it.stop()
            } catch (e: RuntimeException) {
                // 捕获异常，避免崩溃
            }
            it.release()
            recorder = null // 置空引用
        }
        mMoveRecordFileTask.cancel()
        stopRecordingTask?.cancel()
        // 清理视图引用
        viewHolder.pvLayout.onDestroy()
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 移除pvLayout的所有监听器（需SoundRecordingLayout提供移除方法）
        viewHolder.pvLayout.soundRecordingLayoutViewHolder.rlSoundRecording.setOnClickListener(null)
    }

    /**
     * 录音开始或者停止
     * // recording pause
     *
     * @param start   录音开始或者停止
     * @noinspection SameParameterValue
     */
    private fun onRecord(start: Boolean) {
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
            viewHolder.chronometer.stop()
            timeWhenPaused = 0

            stopRecording()
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
            mediaPlayer?.let {
                // 恢复当前暂停的媒体播放器
                resumePlaying()
            } ?: startPlaying()
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
        viewHolder.pvLayout.soundRecordingLayoutViewHolder.ivRecord.setImageResource(R.drawable.ic_pause_white_24dp)
        mediaPlayer = MediaPlayer()
        mediaPlayer?.let { mediaPlayer ->
            try {
                // 文件地址
                mediaPlayer.setDataSource(localMedia.uri)
                mediaPlayer.prepare()

                mediaPlayer.setOnPreparedListener { mediaPlayer.start() }
            } catch (e: IOException) {
                Log.e(TAG, "prepare() failed")
            }

            mediaPlayer.setOnCompletionListener { stopPlaying() }

            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    /**
     * 恢复播放
     */
    private fun resumePlaying() {
        mediaPlayer?.let { mediaPlayer ->
            // 暂停图
            viewHolder.pvLayout.soundRecordingLayoutViewHolder.ivRecord.setImageResource(R.drawable.ic_pause_white_24dp)
            mediaPlayer.start()
        }
    }

    /**
     * 暂停播放
     */
    private fun pausePlaying() {
        mediaPlayer?.let { mediaPlayer ->
            // 设置成播放的图片
            viewHolder.pvLayout.soundRecordingLayoutViewHolder.ivRecord.setImageResource(R.drawable.ic_play_arrow_white_24dp)
            mediaPlayer.pause()
        }
    }

    /**
     * 停止播放
     */
    private fun stopPlaying() {
        mediaPlayer?.let { mediaPlayer ->
            // 设置成播放的图片
            viewHolder.pvLayout.soundRecordingLayoutViewHolder.ivRecord.setImageResource(R.drawable.ic_play_arrow_white_24dp)
            // 停止mediaPlayer
            mediaPlayer.stop()
            mediaPlayer.reset()
            mediaPlayer.release()
            this.mediaPlayer = null

            isPlaying = !isPlaying

            // 一旦音频播放完毕，保持屏幕常亮 这个设置关闭
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    /**
     * 显示录音后的界面
     */
    private fun showRecordEndView() {
        // 录音按钮转变成播放按钮，播放录音
        viewHolder.pvLayout.soundRecordingLayoutViewHolder.ivRecord.setImageResource(R.drawable.ic_play_arrow_white_24dp)
    }

    /**
     * 迁移语音文件
     */
    private fun moveRecordFile() {
        // 执行等待动画
        viewHolder.pvLayout.soundRecordingLayoutViewHolder.btnConfirm.setProgress(1)
        // 开始迁移文件
        ThreadUtils.executeByIo(mMoveRecordFileTask)
    }

    /**
     * 迁移语音的异步线程
     */
    private val mMoveRecordFileTask: ThreadUtils.SimpleTask<Unit> = object : ThreadUtils.SimpleTask<Unit>() {
        // 用弱引用持有Fragment，避免强引用
        private val fragmentRef = WeakReference(this@SoundRecordingFragment)
        override fun doInBackground() {
            val fragment = fragmentRef.get()
            // 检查Fragment是否已销毁或脱离Activity
            if (fragment == null || fragment.isDetached || fragment.activity == null) {
                return
            }
            // 初始化保存好的音频文件
            fragment.initAudio()
            val context = fragment.context
            val newFile = createCacheFile(context, MediaType.TYPE_AUDIO)
            copy(File(fragment.localMedia.absolutePath), newFile, null) { ioProgress: Double, _: File? ->
                val progress = (ioProgress * FULL).toInt()
                ThreadUtils.runOnUiThread {
                    if (isAdded) {
                        fragment.viewHolder.pvLayout.soundRecordingLayoutViewHolder.btnConfirm.addProgress(progress)
                        fragment.localMedia.absolutePath = newFile.path
                        if (progress >= FULL) {
                            val result = Intent()
                            val localFiles = ArrayList<LocalMedia>()
                            localFiles.add(localMedia)
                            result.putParcelableArrayListExtra(SelectedData.STATE_SELECTION, localFiles)
                            fragment.requireActivity().setResult(Activity.RESULT_OK, result)
                            fragment.requireActivity().finish()
                        }
                    }
                }
            }
        }

        /** @noinspection unused
         */
        override fun onSuccess(result: Unit) {
        }
    }

    // region 有关录音相关方法
    /**
     * 开始录音
     */
    private fun startRecording() {
        // 设置音频路径
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }
        recorder?.let { recorder ->
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            recorder.setOutputFile(file.path)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setAudioChannels(1)

            try {
                recorder.prepare()
                recorder.start()
                // 开始计时,从0秒开始算起
                viewHolder.chronometer.base = SystemClock.elapsedRealtime()
                viewHolder.chronometer.start()
                startingTimeMillis = System.currentTimeMillis()
            } catch (e: IOException) {
                Log.e(TAG, "prepare() failed")
            }
        }
    }

    /**
     * 停止录音
     */
    private fun stopRecording() {
        viewHolder.pvLayout.isEnabled = false
        stopRecordingTask = getStopRecordingTask()
        ThreadUtils.executeByIo(stopRecordingTask)
    }

    /**
     * 停止录音的异步线程
     */
    private fun getStopRecordingTask(): ThreadUtils.SimpleTask<Boolean> {
        return object : ThreadUtils.SimpleTask<Boolean>() {
            override fun doInBackground(): Boolean {
                val mElapsedMillis = (System.currentTimeMillis() - startingTimeMillis)
                // 存储到缓存的文件地址
                requireActivity().getSharedPreferences("sp_name_audio", Context.MODE_PRIVATE)
                    .edit()
                    .putString("audio_path", file.path)
                    .putLong("elapsed", mElapsedMillis)
                    .apply()
                recorder?.let {
                    try {
                        it.stop()
                    } catch (ignored: RuntimeException) {
                        // 防止立即录音完成
                    }
                    it.release()
                    recorder = null
                }
                return true
            }

            override fun onSuccess(result: Boolean) {
                viewHolder.pvLayout.isEnabled = true
            }

            override fun onCancel() {
                super.onCancel()
                viewHolder.pvLayout.isEnabled = true
            }

            override fun onFail(t: Throwable) {
                super.onFail(t)
                viewHolder.pvLayout.isEnabled = true
            }
        }
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
