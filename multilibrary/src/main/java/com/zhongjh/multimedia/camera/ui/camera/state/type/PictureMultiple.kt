package com.zhongjh.multimedia.camera.ui.camera.state.type

import com.zhongjh.multimedia.camera.ui.camera.BaseCameraFragment
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraPictureViewManager
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraVideoViewManager
import com.zhongjh.multimedia.camera.ui.camera.state.CameraStateManager
import com.zhongjh.multimedia.camera.ui.camera.state.type.impl.StateMode

/**
 * 多个图片状态，至少有一张图片情况
 *
 *  @param cameraFragment        主要是多个状态围绕着cameraFragment进行相关处理
 *  @param cameraStateManager 可以让状态更改别的状态
 *
 * @author zhongjh
 * @date 2021/11/29
 */
class PictureMultiple(cameraFragment: BaseCameraFragment<out CameraStateManager, out CameraPictureViewManager, out CameraVideoViewManager>, cameraStateManager: CameraStateManager) :
    StateMode(cameraFragment, cameraStateManager) {

    override fun getName(): String {
        return "PictureMultiple"
    }

    override fun pvLayoutCommit() {
        fragmentRef.get()?.setUiEnableFalse()
        // 拍照完成,移动文件
        fragmentRef.get()?.movePictureFile()
    }

    override fun stopProgress() {
        fragmentRef.get()?.setUiEnableTrue()
        // 取消线程
        fragmentRef.get()?.cameraPictureViewManager?.cancelMovePictureFileTask()
    }
}
