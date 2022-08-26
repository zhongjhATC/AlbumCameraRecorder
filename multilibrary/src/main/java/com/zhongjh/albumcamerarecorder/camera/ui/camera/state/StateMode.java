package com.zhongjh.albumcamerarecorder.camera.ui.camera.state;

import com.zhongjh.albumcamerarecorder.camera.ui.camera.BaseCameraFragment;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.presenter.BaseCameraPicturePresenter;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.presenter.BaseCameraVideoPresenter;

/**
 * 状态模式
 *
 * @author zhongjh
 * @date 2021/11/25
 */
public abstract class StateMode implements IState {

    protected final String TAG = CameraStateManagement.class.getSimpleName();

    CameraStateManagement cameraStateManagement;
    BaseCameraFragment<? extends CameraStateManagement,
            ? extends BaseCameraPicturePresenter,
            ? extends BaseCameraVideoPresenter> cameraFragment;

    public BaseCameraFragment<? extends CameraStateManagement,
            ? extends BaseCameraPicturePresenter,
            ? extends BaseCameraVideoPresenter> getCameraFragment() {
        return cameraFragment;
    }

    public void setCameraFragment(BaseCameraFragment<? extends CameraStateManagement,
            ? extends BaseCameraPicturePresenter,
            ? extends BaseCameraVideoPresenter> cameraLayout) {
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
    public StateMode(BaseCameraFragment<? extends CameraStateManagement,
            ? extends BaseCameraPicturePresenter,
            ? extends BaseCameraVideoPresenter> cameraFragment, CameraStateManagement cameraStateManagement) {
        this.cameraFragment = cameraFragment;
        this.cameraStateManagement = cameraStateManagement;
    }
}
