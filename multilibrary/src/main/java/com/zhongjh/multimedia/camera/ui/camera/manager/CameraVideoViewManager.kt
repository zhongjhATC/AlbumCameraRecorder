package com.zhongjh.multimedia.camera.ui.camera.manager

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.zhongjh.multimedia.camera.ui.camera.BaseCameraFragment
import com.zhongjh.multimedia.camera.ui.camera.impl.ICameraVideo
import com.zhongjh.multimedia.camera.ui.camera.motion.FrameExporter
import com.zhongjh.multimedia.camera.ui.camera.motion.model.CapturedFrame
import com.zhongjh.multimedia.camera.ui.camera.motion.model.ColorSpaceInfo
import com.zhongjh.multimedia.camera.ui.camera.motion.model.ExportConfig
import com.zhongjh.multimedia.camera.ui.camera.motion.model.ExportFormat
import com.zhongjh.multimedia.camera.ui.camera.motion.model.ExportResult
import com.zhongjh.multimedia.camera.ui.camera.motion.model.VideoMetadata
import com.zhongjh.multimedia.camera.ui.camera.state.CameraStateManager
import com.zhongjh.multimedia.camera.ui.preview.video.PreviewVideoActivity.Companion.startActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

/**
 * 这是专门处理视频的有关逻辑
 *
 * @author zhongjh
 * @date 2022/8/23
 */
open class CameraVideoViewManager(baseCameraFragment: BaseCameraFragment<out CameraStateManager, out CameraPictureViewManager, out CameraVideoViewManager>) : ICameraVideo {

    /**
     * 使用弱引用持有 Fragment
     */
    private val fragmentRef = WeakReference(baseCameraFragment)

    /**
     * 从视频预览界面回来
     */
    private var previewVideoActivityResult: ActivityResultLauncher<Intent>? = null

    /**
     * 当前录制视频的时间
     */
    var videoTime: Long = 0L

    /**
     * 初始化Activity的有关视频回调
     */
    fun initActivityResult() {
        fragmentRef.get()?.let { fragment ->
            // 从视频预览界面回来
            previewVideoActivityResult = fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.let { data ->
                        fragment.commitVideoSuccess(data)
                    }
                }
            }
        }
    }

    /**
     * 生命周期onDestroy
     */
    override fun onDestroy() {
        // 释放资源
        previewVideoActivityResult = null
    }

    /**
     * 录制视频
     */
    override fun recordVideo() {
        fragmentRef.get()?.cameraManage?.takeVideo()
    }

    /**
     * 录像暂停
     *
     * @param recordedDurationNanos 当前视频持续时间：纳米单位
     */
    override fun onRecordPause(recordedDurationNanos: Long) {
        fragmentRef.get()?.let { fragment ->
            fragment.setShortTipLongRecording()
            // 如果已经有录像正在录制中，那么就不执行这个动作了
            if (videoTime == 0L) {
                fragment.photoVideoLayout.startShowLeftRightButtonsAnimator(false)
            }
            videoTime = recordedDurationNanos / 1000000
            // 显示当前进度
            fragment.photoVideoLayout.setData(videoTime)
            // 如果是在已经合成的情况下继续拍摄，那就重置状态
            if (!fragment.photoVideoLayout.progressMode) {
                fragment.photoVideoLayout.resetConfirm()
            }
            fragment.photoVideoLayout.isEnabled = true
        }
    }

    /**
     * 视频开始录制
     */
    override fun onRecordStart() {
        fragmentRef.get()?.let { fragment ->
            fragment.photoVideoLayout.photoVideoLayoutViewHolder.btnClickOrLong.isStartTicking = true
        }
    }

    /**
     * 视频录制成功
     */
    @SuppressLint("LongLogTag")
    override fun onRecordSuccess(path: String, uri: String) {
        val fragment = fragmentRef.get() ?: return
        val previewVideoActivityResult = previewVideoActivityResult ?: return
        fragment.photoVideoLayout.reset()
        startActivity(fragment, previewVideoActivityResult, path, uri, true)
        fragment.photoVideoLayout.isEnabled = true
    }

    private fun test(uri: String) {
        val activity = fragmentRef.get()?.activity ?: return
        activity.lifecycleScope.launch(Dispatchers.IO) {
            val frameExporter = FrameExporter(activity.applicationContext)
            // ========== 前置步骤：解析视频，拿到封面帧、元数据 ==========
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(activity, uri.toUri())

            // 获取视频总时长（毫秒 → 转微秒），我们取视频中间时间点作为封面
            val durationMsStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationMsStr?.toLongOrNull() ?: 0L
            val durationUs = durationMs * 1000
            // 封面取视频中点
            val coverTimestampUs = durationUs / 2

            // 抽取封面Bitmap（OPTION_CLOSEST 取最接近该时间的帧）
            val coverBitmap: Bitmap? = retriever.getFrameAtTime(
                coverTimestampUs,
                MediaMetadataRetriever.OPTION_CLOSEST
            )
            retriever.release()

            if (coverBitmap == null) {
                // 抽帧失败
                return@launch
            }

            // ----------------------
            // 1.组装 CapturedFrame
            // 注意：真实项目这里要用MediaExtractor解析视频流，拿到准确ColorSpaceInfo；
            // 这里简易默认SDR；如果是HDR视频，必须解析流得到正确ColorSpaceInfo，否则导出色彩异常
            // ----------------------
            val frame = CapturedFrame(
                timestampUs = coverTimestampUs,
                width = coverBitmap.width,
                height = coverBitmap.height,
                colorSpace = ColorSpaceInfo(), // 默认SDR；HDR需要MediaExtractor解析MediaFormat
                metadata = VideoMetadata(
                    rotation = 0, // 视频旋转角度，需要解析视频流获取
                    videoWidth = coverBitmap.width,
                    videoHeight = coverBitmap.height,
                    durationMs = durationMs
                    // dateTime、gps、make、model 录制时如果有就填充，没有留null
                )
            )

            // ----------------------
            // 2.组装导出配置
            // ----------------------
            val exportConfig = ExportConfig(
                format = ExportFormat.JPEG,
                quality = 95,
                preserveMetadata = true, // 保留EXIF元数据
                motionPhoto = true, // 开启动态照片
                motionDurationBeforeS = 1.5f, // 封面往前1.5秒短视频
                motionDurationAfterS = 1.5f, // 封面往后1.5秒短视频
                muteAudio = false, // 保留音频
                maxResolution = 4096 // 最大输出边长限制
            )

            // ----------------------
            // 3.调用 FrameExporter 核心导出方法
            // customExportTreeUri = null → 输出到系统图库；传SAF Uri则输出到自定义文件夹
            // ----------------------
            val exportResult: ExportResult = frameExporter.exportMotionPhoto(
                videoUri = uri.toUri(),
                bitmap = coverBitmap,
                frame = frame,
                config = exportConfig,
                customExportTreeUri = null
            )

            // 用完手动回收封面bitmap（FrameExporter内部会回收内部临时bitmap，但外部传入的原图需要自己管理）
            coverBitmap.recycle()

            // ----------------------
            // 4.处理导出结果，切回主线程更新UI
            // ----------------------
            withContext(Dispatchers.Main) {
                when (exportResult) {
                    is ExportResult.Success -> {
                        val outputUri = exportResult.outputPath
                        // 成功，相册出现MVIMG_xxx.jpeg动态照片
                        if (exportResult.audioDropped) {
                            // 警告：用户没有开静音，但是音频丢失了，UI提示用户
                        }
                    }

                    is ExportResult.Error -> {
                        val errorMsg = exportResult.message
                        // UI提示失败日志 exportResult.cause
                    }
                }
            }
        }
    }
}
