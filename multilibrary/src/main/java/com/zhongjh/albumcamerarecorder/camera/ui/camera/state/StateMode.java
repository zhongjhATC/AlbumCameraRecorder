package com.zhongjh.albumcamerarecorder.camera.ui.camera.state;

import com.zhongjh.albumcamerarecorder.camera.ui.camera.BaseCameraFragment;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.manager.CameraPictureManager;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.manager.CameraVideoManager;

/**
 * 状态模式
 *
 * @author zhongjh
 * @date 2021/11/25
 */
public abstract class StateMode implements IState {

    protected final String TAG = CameraStateManager.class.getSimpleName();

    CameraStateManager cameraStateManager;
    BaseCameraFragment<? extends CameraStateManager,
            ? extends CameraPictureManager,
            ? extends CameraVideoManager> cameraFragment;

    public BaseCameraFragment<? extends CameraStateManager,
            ? extends CameraPictureManager,
            ? extends CameraVideoManager> getCameraFragment() {
        return cameraFragment;
    }

    public void setCameraFragment(BaseCameraFragment<? extends CameraStateManager,
            ? extends CameraPictureManager,
            ? extends CameraVideoManager> cameraLayout) {
        this.cameraFragment = cameraLayout;
    }

    public CameraStateManager getCameraStateManagement() {
        return cameraStateManager;
    }

    public void setCameraStateManagement(CameraStateManager cameraStateManager) {
        this.cameraStateManager = cameraStateManager;
    }

    /**
     * @param cameraStateManager 可以让状态更改别的状态
     * @param cameraFragment       主要是多个状态围绕着cameraLayout进行相关处理
     */
    public StateMode(BaseCameraFragment<? extends CameraStateManager,
            ? extends CameraPictureManager,
            ? extends CameraVideoManager> cameraFragment, CameraStateManager cameraStateManager) {
        this.cameraFragment = cameraFragment;
        this.cameraStateManager = cameraStateManager;
    }

    @Override
    public void onActivityPause() {

    }

    @Override
    public Boolean onBackPressed() {
        return null;
    }

    @Override
    public void pvLayoutCommit() {

    }

    @Override
    public void pvLayoutCancel() {

    }

    @Override
    public void pauseRecord() {

    }

    @Override
    public void stopProgress() {

    }

    @Override
    public void onLongClickFinish() {

    }
}
