package com.zhongjh.multimedia.camera.ui.camera.state.type

import com.zhongjh.multimedia.camera.ui.camera.BaseCameraFragment
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraPictureManager
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraVideoManager
import com.zhongjh.multimedia.camera.ui.camera.state.CameraStateManager
import com.zhongjh.multimedia.camera.ui.camera.state.type.impl.StateMode

/**
 * 视频录制中
 *
 * @param cameraFragment     主要是多个状态围绕着cameraLayout进行相关处理
 * @param cameraStateManager 可以让状态更改别的状态
 *
 * @author zhongjh
 * @date 2021/11/29
 */
class VideoMultipleIn(cameraFragment: BaseCameraFragment<out CameraStateManager, out CameraPictureManager, out CameraVideoManager>, cameraStateManager: CameraStateManager) :
    StateMode(cameraFragment, cameraStateManager) {
    override fun getName(): String {
        return "VideoMultipleIn"
    }

    override fun onActivityPause() {
        cameraFragment.cameraVideoManager.videoTime = 0L
        // 重置所有
        cameraFragment.resetStateAll()
        // 恢复预览状态
        stateManagerRef.get()?.let { stateManager ->
            stateManager.state = stateManager.preview
        }
    }

    override fun onBackPressed(): Boolean {
        cameraFragment.cameraManage.closeVideo()
        cameraFragment.photoVideoLayout.resetConfirm()

        if (cameraFragment.cameraVideoManager.videoTime == 0L) {
            // 如果没有视频节点则重置所有按钮
            cameraFragment.photoVideoLayout.reset()
            // 恢复预览状态
            stateManagerRef.get()?.let { stateManager ->
                stateManager.state = stateManager.preview
            }
        } else {
            // 如果有视频节点则中断中心按钮
            cameraFragment.photoVideoLayout.viewHolder.btnClickOrLong.breakOff()
            // 恢复录制状态
            stateManagerRef.get()?.let { stateManager ->
                stateManager.state = stateManager.videoMultiple
            }
        }
        return true
    }

    override fun pauseRecord() {
        // 切回非录制中的状态
        stateManagerRef.get()?.let { stateManager ->
            stateManager.state = stateManager.videoMultiple
        }
        cameraFragment.cameraManage.pauseVideo()
    }

    override fun onLongClickFinish() {
        cameraFragment.cameraManage.stopVideo()
    }
}
