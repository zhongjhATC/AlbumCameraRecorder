package com.zhongjh.multimedia.camera.ui.camera.state.type

import com.zhongjh.multimedia.camera.ui.camera.BaseCameraFragment
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraPictureManager
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraVideoManager
import com.zhongjh.multimedia.camera.ui.camera.state.CameraStateManager
import com.zhongjh.multimedia.camera.ui.camera.state.type.impl.StateMode

/**
 * 单图相关处理
 *
 *  @param cameraFragment     主要是多个状态围绕着CameraFragment进行相关处理
 *  @param cameraStateManager 可以让状态更改别的状态
 *
 * @author zhongjh
 * @date 2021/11/26
 */
class PictureSingle(cameraFragment: BaseCameraFragment<out CameraStateManager, out CameraPictureManager, out CameraVideoManager>, cameraStateManager: CameraStateManager) :
    StateMode(cameraFragment, cameraStateManager) {

    override fun getName(): String {
        return "PictureComplete"
    }

    override fun pvLayoutCommit() {
        fragmentRef.get()?.setUiEnableFalse()
        // 拍照完成,移动文件
        fragmentRef.get()?.movePictureFile()
    }

    override fun pvLayoutCancel() {
        // 取消线程
        fragmentRef.get()?.cameraPictureManager?.cancelMovePictureFileTask()
        fragmentRef.get()?.setUiEnableTrue()
        // 取消单图后的重置
        fragmentRef.get()?.cancelOnResetBySinglePicture()
        // 恢复预览状态
        stateManagerRef.get()?.let { stateManager ->
            stateManager.state = stateManager.preview
        }
    }

    override fun stopProgress() {
        // 取消线程
        fragmentRef.get()?.cameraPictureManager?.cancelMovePictureFileTask()
        fragmentRef.get()?.setUiEnableTrue()
    }
}
