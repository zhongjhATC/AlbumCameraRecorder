package com.zhongjh.albumcamerarecorder.camera.ui.camera.state.type;

import com.zhongjh.albumcamerarecorder.camera.ui.camera.BaseCameraFragment;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.presenter.BaseCameraVideoPresenter;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.CameraStateManagement;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.StateMode;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.presenter.BaseCameraPicturePresenter;

/**
 * 多个视频的状态录制中
 *
 * @author zhongjh
 * @date 2021/11/29
 */
public class VideoMultipleIn extends StateMode {

    /**
     * @param cameraFragment          主要是多个状态围绕着cameraLayout进行相关处理
     * @param cameraStateManagement 可以让状态更改别的状态
     */
    public VideoMultipleIn(BaseCameraFragment<? extends CameraStateManagement,
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
        getCameraFragment().getPhotoVideoLayout().resetConfirm();
        getCameraFragment().getPhotoVideoLayout().getViewHolder().btnClickOrLong.selectionRecordRollBack();

        if (getCameraFragment().getCameraVideoPresenter().getVideoPaths().size() <= 0) {
            // 如果没有视频节点则重置所有按钮
            getCameraFragment().getPhotoVideoLayout().reset();
            // 恢复预览状态
            getCameraStateManagement().setState(getCameraStateManagement().getPreview());
        } else {
            // 如果有视频节点则中断中心按钮
            getCameraFragment().getPhotoVideoLayout().getViewHolder().btnClickOrLong.breakOff();
            // 恢复预览状态
            getCameraStateManagement().setState(getCameraStateManagement().getVideoMultiple());
        }
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
        getCameraFragment().getPhotoVideoLayout().getViewHolder().btnClickOrLong.selectionRecordRollBack();
        if (getCameraFragment().getCameraVideoPresenter().getVideoPaths().size() <= 0) {
            // 母窗体显示底部
            getCameraFragment().getMainActivity().showHideTableLayout(true);
        }
    }

    @Override
    public void stopRecord(boolean isShort) {
        if (isShort) {
            // 如果没有视频数据
            if (getCameraFragment().getCameraVideoPresenter().getVideoPaths().size() <= 0) {
                // 则重置底部按钮
                getCameraFragment().getPhotoVideoLayout().reset();
                // 恢复预览状态
                getCameraStateManagement().setState(getCameraStateManagement().getPreview());
            } else {
                // 设置成多个视频状态
                getCameraStateManagement().setState(getCameraStateManagement().getVideoMultiple());
            }
        } else {
            // 设置成多个视频状态
            getCameraStateManagement().setState(getCameraStateManagement().getVideoMultiple());
        }
    }

    @Override
    public void stopProgress() {

    }

}
