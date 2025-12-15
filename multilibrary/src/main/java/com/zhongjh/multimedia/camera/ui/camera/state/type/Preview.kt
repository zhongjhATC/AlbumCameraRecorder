package com.zhongjh.multimedia.camera.ui.camera.state.type

import com.zhongjh.multimedia.camera.ui.camera.BaseCameraFragment
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraPictureViewManager
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraVideoViewManager
import com.zhongjh.multimedia.camera.ui.camera.state.CameraStateManager
import com.zhongjh.multimedia.camera.ui.camera.state.type.impl.StateMode

/**
 * 预览状态的相关处理，默认状态
 *
 * @param cameraFragment          主要是多个状态围绕着cameraLayout进行相关处理
 * @param cameraStateManager 可以让状态更改别的状态
 *
 * @author zhongjh
 * @date 2021/11/26
 */
class Preview(cameraFragment: BaseCameraFragment<out CameraStateManager, out CameraPictureViewManager, out CameraVideoViewManager>, cameraStateManager: CameraStateManager) :
    StateMode(cameraFragment, cameraStateManager) {
    override fun getName(): String {
        return "Preview"
    }

    override fun onBackPressed(): Boolean {
        // 如果是预览状态直接退出当前界面
        return false
    }
}
