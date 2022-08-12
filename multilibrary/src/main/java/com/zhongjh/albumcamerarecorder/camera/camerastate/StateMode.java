package com.zhongjh.albumcamerarecorder.camera.camerastate;

import com.zhongjh.albumcamerarecorder.camera.BaseCameraFragment;

/**
 * 状态模式
 *
 * @author zhongjh
 * @date 2021/11/25
 */
public abstract class StateMode implements StateInterface {

    protected final String TAG = CameraStateManagement.class.getSimpleName();

    CameraStateManagement cameraStateManagement;
    BaseCameraFragment cameraFragment;

    public BaseCameraFragment getCameraFragment() {
        return cameraFragment;
    }

    public void setCameraFragment(BaseCameraFragment cameraLayout) {
        this.cameraFragment = cameraLayout;
    }

    public CameraStateManagement getCameraStateManagement() {
        return cameraStateManagement;
    }

    public void setCameraStateManagement(CameraStateManagement cameraStateManagement) {
        this.cameraStateManagement = cameraStateManagement;
    }

    /**
     * @param cameraStateManagement 可以让状态更改别的状态
     * @param cameraFragment       主要是多个状态围绕着cameraLayout进行相关处理
     */
    public StateMode(BaseCameraFragment cameraFragment, CameraStateManagement cameraStateManagement) {
        this.cameraFragment = cameraFragment;
        this.cameraStateManagement = cameraStateManagement;
    }
}
