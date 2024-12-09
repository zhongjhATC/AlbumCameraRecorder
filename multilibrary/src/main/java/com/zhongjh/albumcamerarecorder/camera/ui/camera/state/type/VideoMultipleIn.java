package com.zhongjh.albumcamerarecorder.camera.ui.camera.state.type;

import com.zhongjh.albumcamerarecorder.camera.ui.camera.BaseCameraFragment;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.manager.CameraVideoManager;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.CameraStateManager;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.StateMode;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.manager.CameraPictureManager;

/**
 * 多个视频的状态录制中
 *
 * @author zhongjh
 * @date 2021/11/29
 */
public class VideoMultipleIn extends StateMode {

    /**
     * @param cameraFragment     主要是多个状态围绕着cameraLayout进行相关处理
     * @param cameraStateManager 可以让状态更改别的状态
     */
    public VideoMultipleIn(BaseCameraFragment<? extends CameraStateManager,
            ? extends CameraPictureManager,
            ? extends CameraVideoManager> cameraFragment, CameraStateManager cameraStateManager) {
        super(cameraFragment, cameraStateManager);
    }

    @Override
    public void resetState() {
        // 恢复预览状态
        getCameraStateManagement().setState(getCameraStateManagement().getPreview());
    }

    @Override
    public Boolean onBackPressed() {
        // 如果是录制中则暂停视频
        getCameraFragment().getCameraVideoManager().setBreakOff(true);
        getCameraFragment().getCameraManage().closeVideo();
        getCameraFragment().getPhotoVideoLayout().resetConfirm();
        getCameraFragment().getPhotoVideoLayout().getViewHolder().btnClickOrLong.selectionRecordRollBack();

        if (getCameraFragment().getCameraVideoManager().getVideoTimes().isEmpty()) {
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
        if (getCameraFragment().getCameraVideoManager().getVideoTimes().isEmpty()) {
            // 母窗体显示底部
            getCameraFragment().getMainActivity().showHideTableLayout(true);
        }
    }

    @Override
    public void pauseRecord(boolean isShort) {
        if (isShort) {
            // 如果没有视频数据
            if (getCameraFragment().getCameraVideoManager().getVideoTimes().isEmpty()) {
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
