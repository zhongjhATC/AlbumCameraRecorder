package com.zhongjh.multimedia.camera.ui.camera.state.type.impl

import com.zhongjh.multimedia.camera.ui.camera.BaseCameraFragment
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraPictureViewManager
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraVideoViewManager
import com.zhongjh.multimedia.camera.ui.camera.state.CameraStateManager
import java.lang.ref.WeakReference

/**
 * 状态模式
 *
 * @param cameraStateManager 可以让状态更改别的状态
 * @param cameraFragment       主要是多个状态围绕着cameraLayout进行相关处理
 *
 * @author zhongjh
 * @date 2021/11/25
 */
abstract class StateMode(
    var cameraFragment: BaseCameraFragment<out CameraStateManager, out CameraPictureViewManager, out CameraVideoViewManager>,
    private var cameraStateManager: CameraStateManager
) : IState {

    protected val tag: String = CameraStateManager::class.java.simpleName

    /**
     * 使用弱引用持有 Fragment 和 Manager
     */
    protected val fragmentRef = WeakReference(cameraFragment)
    protected val stateManagerRef = WeakReference(cameraStateManager)

    /**
     * 在 Fragment 销毁时调用，释放资源
     */
    open fun onDestroy() {
        fragmentRef.clear()
        stateManagerRef.clear()
    }

    override fun onActivityPause() {
    }

    override fun onBackPressed(): Boolean? {
        return null
    }

    override fun pvLayoutCommit() {
    }

    override fun pvLayoutCancel() {
    }

    override fun pauseRecord() {
    }

    override fun stopProgress() {
    }

    override fun onLongClickFinish() {
    }
}
