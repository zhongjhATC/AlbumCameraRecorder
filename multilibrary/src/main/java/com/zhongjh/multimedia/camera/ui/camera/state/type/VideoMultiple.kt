package com.zhongjh.multimedia.camera.ui.camera.state.type

import android.util.Log
import com.zhongjh.circularprogressview.CircularProgressState
import com.zhongjh.multimedia.camera.ui.camera.BaseCameraFragment
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraPictureViewManager
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraVideoViewManager
import com.zhongjh.multimedia.camera.ui.camera.state.CameraStateManager
import com.zhongjh.multimedia.camera.ui.camera.state.type.impl.StateMode

/**
 * 视频模式
 *
 * @param cameraFragment     主要是多个状态围绕着cameraLayout进行相关处理
 * @param cameraStateManager 可以让状态更改别的状态
 *
 * @author zhongjh
 * @date 2021/11/29
 */
class VideoMultiple(cameraFragment: BaseCameraFragment<out CameraStateManager, out CameraPictureViewManager, out CameraVideoViewManager>, cameraStateManager: CameraStateManager) :
    StateMode(cameraFragment, cameraStateManager) {
    override fun getName(): String {
        return "VideoMultiple"
    }

    override fun onActivityPause() {
        cameraFragment.cameraVideoViewManager.videoTime = 0L
        // 重置所有
        cameraFragment.resetStateAll()
        // 恢复预览状态
        stateManagerRef.get()?.let { stateManager ->
            stateManager.state = stateManager.preview
        }
    }

    override fun pvLayoutCommit() {
        if (cameraFragment.photoVideoLayout.photoVideoLayoutViewHolder.btnConfirm.mState == CircularProgressState.PLAY) {
            // 完成录制
            Log.d(tag, "pvLayoutCommit完成录制")
            cameraFragment.cameraManage.stopVideo()
        } else {
            // 中断操作
            Log.d(tag, "pvLayoutCommit中断操作")
            stopProgress()
        }
    }

    override fun pauseRecord() {
        cameraFragment.cameraManage.pauseVideo()
    }

    override fun onLongClickFinish() {
        cameraFragment.cameraManage.stopVideo()
    }
}
