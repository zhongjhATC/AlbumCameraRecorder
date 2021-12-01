package com.zhongjh.albumcamerarecorder.camera.camerastate;

import com.zhongjh.albumcamerarecorder.camera.CameraLayout;

/**
 * 状态模式
 *
 * @author zhongjh
 * @date 2021/11/25
 */
public abstract class StateMode implements StateInterface {

    protected final String TAG = CameraStateManagement.class.getSimpleName();

    CameraStateManagement cameraStateManagement;
    CameraLayout cameraLayout;

    public CameraLayout getCameraLayout() {
        return cameraLayout;
    }

    public void setCameraLayout(CameraLayout cameraLayout) {
        this.cameraLayout = cameraLayout;
    }

    public CameraStateManagement getCameraStateManagement() {
        return cameraStateManagement;
    }

    public void setCameraStateManagement(CameraStateManagement cameraStateManagement) {
        this.cameraStateManagement = cameraStateManagement;
    }

    /**
     * @param cameraStateManagement 可以让状态更改别的状态
     * @param cameraLayout       主要是多个状态围绕着cameraLayout进行相关处理
     */
    public StateMode(CameraLayout cameraLayout, CameraStateManagement cameraStateManagement) {
        this.cameraLayout = cameraLayout;
        this.cameraStateManagement = cameraStateManagement;
    }
}
