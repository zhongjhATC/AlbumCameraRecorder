package com.zhongjh.multimedia.camera.ui.camera.manager

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.zhongjh.multimedia.camera.ui.camera.BaseCameraFragment
import com.zhongjh.multimedia.camera.ui.camera.impl.ICameraVideo
import com.zhongjh.multimedia.camera.ui.camera.state.CameraStateManager
import com.zhongjh.multimedia.camera.ui.preview.video.PreviewVideoActivity.Companion.startActivity
import java.lang.ref.WeakReference

/**
 * 这是专门处理视频的有关逻辑
 *
 * @author zhongjh
 * @date 2022/8/23
 */
class CameraVideoManager(baseCameraFragment: BaseCameraFragment<out CameraStateManager, out CameraPictureManager, out CameraVideoManager>) : ICameraVideo {

    /**
     * 使用弱引用持有 Fragment
     */
    val fragmentRef = WeakReference(baseCameraFragment)

    /**
     * 从视频预览界面回来
     */
    private var previewVideoActivityResult: ActivityResultLauncher<Intent>? = null

    /**
     * 当前录制视频的时间
     */
    @JvmField
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
            fragment.photoVideoLayout.viewHolder.btnClickOrLong.isStartTicking = true
        }
    }

    /**
     * 视频录制成功
     */
    @SuppressLint("LongLogTag")
    override fun onRecordSuccess(path: String) {
        val fragment = fragmentRef.get() ?: return
        val previewVideoActivityResult = previewVideoActivityResult ?: return
        fragment.photoVideoLayout.reset()
        startActivity(fragment, previewVideoActivityResult, path, true)
        fragment.photoVideoLayout.isEnabled = true
    }
}
