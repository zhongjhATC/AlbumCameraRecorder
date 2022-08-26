package com.zhongjh.albumcamerarecorder.camera.ui.camera.state.type;

import com.zhongjh.albumcamerarecorder.camera.ui.camera.BaseCameraFragment;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.presenter.BaseCameraVideoPresenter;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.CameraStateManagement;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.StateMode;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.presenter.BaseCameraPicturePresenter;

/**
 * 正在录制视频中的状态
 *
 * @author zhongjh
 * @date 2021/11/25
 */
public class VideoIn extends StateMode {

    /**
     * @param cameraFragment          主要是多个状态围绕着cameraLayout进行相关处理
     * @param cameraStateManagement 可以让状态更改别的状态
     */
    public VideoIn(BaseCameraFragment<? extends CameraStateManagement,
            ? extends BaseCameraPicturePresenter,
            ? extends BaseCameraVideoPresenter> cameraFragment, CameraStateManagement cameraStateManagement) {
        super(cameraFragment, cameraStateManagement);
    }

    @Override
    public void resetState() {
        // 恢复预览状态
        getCameraStateManagement().setState(getCameraStateManagement().getPreview());
    }

    @Override
    public Boolean onBackPressed() {
        // 如果是录制中则暂停视频
        getCameraFragment().getCameraVideoPresenter().setBreakOff(true);
        getCameraFragment().getCameraView().stopVideo();
        // 重置按钮
        getCameraFragment().getPhotoVideoLayout().reset();
        // 恢复预览状态
        getCameraStateManagement().setState(getCameraStateManagement().getPreview());
        return true;
    }

    @Override
    public boolean onActivityResult(int resultCode) {
        return true;
    }

    @Override
    public void pvLayoutCommit() {

    }

    @Override
    public void pvLayoutCancel() {

    }

    @Override
    public void longClickShort(long time) {
        // 母窗体显示底部
        getCameraFragment().getMainActivity().showHideTableLayout(true);
    }

    @Override
    public void stopRecord(boolean isShort) {
        if (isShort) {
            // 重置底部按钮
            getCameraFragment().getPhotoVideoLayout().reset();
            getCameraStateManagement().setState(getCameraStateManagement().getPreview());
        } else {
            getCameraStateManagement().setState(getCameraStateManagement().getVideoComplete());
        }
    }

    @Override
    public void stopProgress() {

    }
}
