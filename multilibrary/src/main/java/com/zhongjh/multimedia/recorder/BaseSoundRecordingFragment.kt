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
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.enums.MediaType
import com.zhongjh.common.enums.MimeType
import com.zhongjh.common.utils.FileUtils.copy
import com.zhongjh.common.utils.MediaStoreCompat
import com.zhongjh.common.utils.StatusBarUtils.getStatusBarHeight
import com.zhongjh.common.utils.request
import com.zhongjh.multimedia.BaseFragment
import com.zhongjh.multimedia.MainActivity
import com.zhongjh.multimedia.R
import com.zhongjh.multimedia.camera.listener.ClickOrLongListener
import com.zhongjh.multimedia.model.SelectedData
import com.zhongjh.multimedia.recorder.impl.ISoundRecordingView
import com.zhongjh.multimedia.recorder.widget.SoundRecordingLayout
import com.zhongjh.multimedia.settings.RecordeSpec
import com.zhongjh.multimedia.utils.FileMediaUtil.createCacheFile
import com.zhongjh.multimedia.widget.BaseOperationLayout
import com.zhongjh.multimedia.widget.clickorlongbutton.ClickOrLongButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import kotlin.coroutines.resumeWithException

abstract class BaseSoundRecordingFragment : BaseFragment(), ISoundRecordingView {

    /**
     * 使用弱引用持有 Activity
     */
    private var mainActivityRef: WeakReference<MainActivity>? = null

    /**
     * 安全获取 Activity
     */
    val mainActivity: MainActivity?
        get() = mainActivityRef?.get()

    lateinit var myContext: Context
        private set

    /**
     * 完成压缩-复制的异步线程
     */
    private var moveRecordFileJob: Job? = null

    private var stopRecordingJob: Job? = null

    /**
     * 是否正在播放中
     */
    private var isPlaying = false

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
    private val file: File by lazy { createCacheFile(myContext, MediaType.TYPE_AUDIO) }

    private var recorder: MediaRecorder? = null

    private var startingTimeMillis: Long = 0

    // endregion

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = setContentView(inflater, container)
        // 拦截返回键事件
        view.setOnKeyListener { _: View, keyCode: Int, _: KeyEvent -> keyCode == KeyEvent.KEYCODE_BACK }
        initView(view, savedInstanceState)
        initData()
        setView()
        initListener()
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            this.mainActivityRef = WeakReference(context)
            this.myContext = context.applicationContext
        }
    }

    override fun onDetach() {
        super.onDetach()
        mainActivityRef = null
    }

    /**
     * 初始化相关数据
     */
    private fun initData() {
    }

    /**
     * 设置相关view，由子类赋值
     */
    private fun setView() {
        // 处理图片、视频等需要进度显示
        soundRecordingLayout.soundRecordingLayoutViewHolder.btnConfirm.setProgressMode(true)

        // 初始化设置
        val mRecordSpec = RecordeSpec
        // 提示文本
        soundRecordingLayout.setTip(resources.getString(R.string.z_multi_library_long_press_sound_recording))
        // 设置录制时间
        soundRecordingLayout.setDuration(mRecordSpec.maxDuration)
        soundRecordingLayout.setReadinessDuration(mRecordSpec.readinessDuration)
        // 设置只能长按
        soundRecordingLayout.setButtonFeatures(ClickOrLongButton.BUTTON_STATE_ONLY_LONG_CLICK)

        // 兼容沉倾状态栏
        mainActivity?.let { mainActivity ->
            val statusBarHeight = getStatusBarHeight(mainActivity)
            val layoutParams = chronometer.layoutParams as RelativeLayout.LayoutParams
            layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin + statusBarHeight, layoutParams.rightMargin, layoutParams.bottomMargin)
        }
    }

    /**
     * 初始化相关事件
     */
    protected open fun initListener() {
        // 录音等事件
        initPvLayoutPhotoVideoListener()
        // 播放事件
        initRlSoundRecordingClickListener()
        // 确认和取消
        initPvLayoutOperateListener()
    }

    override fun onBackPressed(): Boolean {
        // 判断当前状态是否休闲
        if (soundRecordingLayout.state == SoundRecordingLayout.STATE_PREVIEW) {
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
     * 录音等事件
     */
    private fun initPvLayoutPhotoVideoListener() {
        soundRecordingLayout.setPhotoVideoListener(object : ClickOrLongListener {
            override fun actionDown() {
                // 母窗体禁止滑动
                mainActivity?.showHideTableLayout(false)
            }

            override fun onClick() {
            }

            override fun onLongClick() {
                Log.d(TAG, "onLongClick")
                // 录音开启
                onRecord(true)
            }

            override fun onLongClickEnd(time: Long) {
                Log.d(TAG, "onLongClickEnd")
                recordEnd()
            }

            override fun onLongClickFinish() {
                Log.d(TAG, "onLongClickFinish")
                recordEnd()
            }

            override fun onLongClickError() {
                Log.d(TAG, "onLongClickError")
            }

            override fun onBanClickTips() {
                Log.d(TAG, "onBanClickTips")
            }

            override fun onClickStopTips() {
                Log.d(TAG, "onClickStopTips")
            }
        })
    }

    /**
     * 录音结束
     */
    private fun recordEnd() {
        soundRecordingLayout.hideBtnClickOrLong()
        soundRecordingLayout.startShowLeftRightButtonsAnimator(true)
        Log.d(TAG, "onLongClickEnd")
        // 录音结束
        onRecord(false)
        showRecordEndView()
    }

    /**
     * 播放事件
     */
    private fun initRlSoundRecordingClickListener() {
        soundRecordingLayout.soundRecordingLayoutViewHolder.rlEdit.setOnClickListener { view: View? ->
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
        soundRecordingLayout.setOperateListener(object : BaseOperationLayout.OperateListener {
            /** @noinspection unused
             */
            override fun beforeConfirm(): Boolean {
                return true
            }

            /** @noinspection unused
             */
            override fun cancel() {
                // 母窗体启动滑动
                mainActivity?.showHideTableLayout(true)
                // 重置取消确认按钮
                soundRecordingLayout.reset()
                // 重置时间
                chronometer.base = SystemClock.elapsedRealtime()
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
        mainActivity?.let { mainActivity ->
            val sharePreferences = mainActivity.getSharedPreferences("sp_name_audio", Context.MODE_PRIVATE)
            val filePath = sharePreferences?.getString("audio_path", "") as String
            val elapsed = sharePreferences.getLong("elapsed", 0)
            val file = File(filePath)
            localMedia.absolutePath = filePath
            localMedia.uri = MediaStoreCompat.getUri(myContext, filePath).toString()
            localMedia.duration = elapsed
            localMedia.size = File(filePath).length()
            localMedia.mimeType = MimeType.AAC.mimeTypeName
            localMedia.fileName = file.name
            localMedia.parentFolderName = file.parentFile?.name
        }
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
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 移除pvLayout的所有监听器（需SoundRecordingLayout提供移除方法）
        soundRecordingLayout.soundRecordingLayoutViewHolder.rlEdit.setOnClickListener(null)
        // 移除所有监听器，避免SoundRecordingLayout持有Fragment引用
        soundRecordingLayout.onDestroy()
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
            val folder = File(mainActivity?.getExternalFilesDir(null).toString() + "/SoundRecorder")
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
            mainActivity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            chronometer.stop()
            timeWhenPaused = 0

            stopRecording()
            // allow the screen to turn off again once recording is finished
            mainActivity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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
        soundRecordingLayout.soundRecordingLayoutViewHolder.ivRecord.setImageResource(R.drawable.ic_pause_white_24dp)
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

            mainActivity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    /**
     * 恢复播放
     */
    private fun resumePlaying() {
        mediaPlayer?.let { mediaPlayer ->
            // 暂停图
            soundRecordingLayout.soundRecordingLayoutViewHolder.ivRecord.setImageResource(R.drawable.ic_pause_white_24dp)
            mediaPlayer.start()
        }
    }

    /**
     * 暂停播放
     */
    private fun pausePlaying() {
        mediaPlayer?.let { mediaPlayer ->
            // 设置成播放的图片
            soundRecordingLayout.soundRecordingLayoutViewHolder.ivRecord.setImageResource(R.drawable.ic_play_arrow_white_24dp)
            mediaPlayer.pause()
        }
    }

    /**
     * 停止播放
     */
    private fun stopPlaying() {
        mediaPlayer?.let { mediaPlayer ->
            // 设置成播放的图片
            soundRecordingLayout.soundRecordingLayoutViewHolder.ivRecord.setImageResource(R.drawable.ic_play_arrow_white_24dp)
            // 停止mediaPlayer
            mediaPlayer.stop()
            mediaPlayer.reset()
            mediaPlayer.release()
            this.mediaPlayer = null

            isPlaying = !isPlaying

            // 一旦音频播放完毕，保持屏幕常亮 这个设置关闭
            mainActivity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    /**
     * 显示录音后的界面
     */
    private fun showRecordEndView() {
        // 录音按钮转变成播放按钮，播放录音
        soundRecordingLayout.soundRecordingLayoutViewHolder.ivRecord.setImageResource(R.drawable.ic_play_arrow_white_24dp)
    }

    /**
     * 迁移语音文件
     */
    private fun moveRecordFile() {
        // 执行等待动画
        soundRecordingLayout.soundRecordingLayoutViewHolder.btnConfirm.setProgress(1)
        // 开始迁移文件
        moveRecordFileJob()
    }

    /**
     * 迁移语音的异步线程
     */
    private fun moveRecordFileJob() {
        moveRecordFileJob?.cancel()
        moveRecordFileJob = lifecycleScope.request {
            // 用弱引用持有Fragment，避免强引用
            val fragmentRef = WeakReference(this@BaseSoundRecordingFragment)
            val fragment = fragmentRef.get()
            // 检查Fragment是否处于有效状态（未销毁、已添加到Activity）
            if (!isFragmentValid(fragment)) {
                // 无效则终止协程
                return@request false
            }
            fragment?.let {
                // 初始化保存好的音频文件
                fragment.initAudio()
                val newFile = createCacheFile(myContext, MediaType.TYPE_AUDIO)
                val sourceFile = File(fragment.localMedia.absolutePath)
                // 用挂起函数包装复制操作，支持协程取消
                val copySuccess = copyFileWithProgress(sourceFile, newFile, fragment)
                return@request copySuccess
            }
            return@request false
        }.onSuccess { copySuccess ->
            val fragmentRef = WeakReference(this@BaseSoundRecordingFragment)
            val fragment = fragmentRef.get()
            if (copySuccess) {
                // 再次检查Fragment状态（避免在UI线程执行时Fragment已销毁）
                if (isFragmentValid(fragment)) {
                    fragment?.let {
                        val result = Intent().apply {
                            putParcelableArrayListExtra(SelectedData.STATE_SELECTION, arrayListOf(fragment.localMedia))
                        }
                        fragment.mainActivity?.apply {
                            setResult(Activity.RESULT_OK, result)
                            finish()
                        }
                    }
                }
            }
        }.onFail {
            // 清理可能的残留文件
            val targetFile = File(localMedia.absolutePath)
            if (targetFile.exists()) {
                targetFile.delete()
            }
        }.launch()
    }

    /**
     * 包装文件复制操作，支持进度回调和协程取消
     * 注意：将回调转为挂起函数风格，确保与协程生命周期同步
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun copyFileWithProgress(source: File, dest: File, fragment: BaseSoundRecordingFragment): Boolean = suspendCancellableCoroutine { continuation ->

        // 1. 保存当前协程作用域，用于在回调中启动子协程
        val coroutineScope = CoroutineScope(continuation.context)
        try {
            // 执行复制操作
            copy(source, dest, null) { ioProgress: Double, _: File? ->
                // 检查协程是否已取消（如页面销毁），若取消则终止回调
                if (continuation.isCancelled) {
                    return@copy
                }

                // 计算进度并更新UI（确保在主线程）
                val progress = (ioProgress * FULL).toInt()
                coroutineScope.launch(Dispatchers.Main) {
                    updateProgressUI(progress, fragment)
                    // 复制完成后恢复协程（可选，根据业务是否需要等待完成）
                    if (progress >= FULL) {
                        Log.d(TAG, "UI进度更新continuation.resume完成：$progress%")
                        continuation.resume(true) {}
                    }
                }
            }
        } catch (e: Exception) {
            if (!continuation.isCancelled) {
                continuation.resumeWithException(e)
            }
        }

        // 协程被取消时的回调（清理资源）
        continuation.invokeOnCancellation {
            // 取消时删除目标文件（避免残留）
            if (dest.exists()) dest.delete()
        }
    }

    /**
     * 挂起函数：更新UI进度，确保更新完成后再返回
     * @return 是否成功更新（Fragment是否有效）
     */
    private suspend fun updateProgressUI(progress: Int, fragment: BaseSoundRecordingFragment): Boolean =
        withContext(Dispatchers.Main) { // 切换到主线程并挂起，等待执行完成
            if (isFragmentValid(fragment)) {
                // 更新进度条
                soundRecordingLayout.soundRecordingLayoutViewHolder.btnConfirm.addProgress(progress)
                Log.d(TAG, "UI进度更新完成：$progress%")
                // 更新成功
                true
            } else {
                Log.e(TAG, "Fragment已无效，无法更新UI")
                // 更新失败
                false
            }
        }

    /**
     * 检查Fragment是否处于有效状态
     */
    private fun isFragmentValid(fragment: Fragment?): Boolean {
        return fragment?.let {
            fragment.isAdded && !fragment.isDetached && fragment.activity != null && fragment.activity?.isFinishing == false && fragment.activity?.isDestroyed == false
        } ?: let { false }
    }

    /**
     * 停止录音的异步线程
     */
    private fun stopRecordingJob() {
        stopRecordingJob?.cancel()
        stopRecordingJob = lifecycleScope.request {
            // 用弱引用持有Fragment，避免强引用
            val fragmentRef = WeakReference(this@BaseSoundRecordingFragment)
            val fragment = fragmentRef.get()
            // 检查 Fragment 是否有效（未销毁、未脱离 Activity）
            if (fragment == null || !fragment.isAdded || fragment.isDetached || fragment.activity == null || fragment.activity?.isFinishing == true) {
                return@request false
            }
            val mElapsedMillis = (System.currentTimeMillis() - startingTimeMillis)
            // 存储到缓存的文件地址
            fragment.mainActivity?.getSharedPreferences("sp_name_audio", Context.MODE_PRIVATE)
                ?.edit()
                ?.putString("audio_path", file.path)
                ?.putLong("elapsed", mElapsedMillis)
                ?.apply()
            recorder?.let {
                try {
                    it.stop()
                } catch (ignored: RuntimeException) {
                    // 防止立即录音完成
                }
                it.release()
                recorder = null
            }
            return@request true
        }.onSuccess {
            soundRecordingLayout.isEnabled = true
        }.onFail {
            soundRecordingLayout.isEnabled = true
        }.onCancel {
            soundRecordingLayout.isEnabled = true
        }.launch()
    }

    // region 有关录音相关方法
    /**
     * 开始录音
     */
    private fun startRecording() {
        // 设置音频路径
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(myContext)
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
                chronometer.base = SystemClock.elapsedRealtime()
                chronometer.start()
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
        soundRecordingLayout.isEnabled = false
        stopRecordingJob()
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
    }

}