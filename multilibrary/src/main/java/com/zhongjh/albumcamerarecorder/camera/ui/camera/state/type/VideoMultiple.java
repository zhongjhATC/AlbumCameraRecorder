package com.zhongjh.albumcamerarecorder.camera.ui.camera.state.type;

import com.zhongjh.albumcamerarecorder.camera.ui.camera.BaseCameraFragment;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.manager.CameraVideoManager;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.CameraStateManager;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.StateMode;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.manager.CameraPictureManager;
import com.zhongjh.circularprogressview.CircularProgressState;

/**
 * 多个视频模式
 *
 * @author zhongjh
 * @date 2021/11/29
 */
public class VideoMultiple extends StateMode {

    /**
     * @param cameraFragment        主要是多个状态围绕着cameraLayout进行相关处理
     * @param cameraStateManager 可以让状态更改别的状态
     */
    public VideoMultiple(BaseCameraFragment<? extends CameraStateManager,
            ? extends CameraPictureManager,
            ? extends CameraVideoManager> cameraFragment, CameraStateManager cameraStateManager) {
        super(cameraFragment, cameraStateManager);
    }

    @Override
    public void resetState() {
        // 重置所有
        getCameraFragment().resetStateAll();
        // 恢复预览状态
        getCameraStateManagement().setState(getCameraStateManagement().getPreview());
    }

    @Override
    public Boolean onBackPressed() {
        return null;
    }

    @Override
    public boolean onActivityResult(int resultCode) {
        // 返回false处理视频
        return false;
    }

    @Override
    public void pvLayoutCommit() {
        // 判断是否加载中,如果是《合成视频中》则取消《合成视频》,否则进行《合成视频》
        if (getCameraFragment().getPhotoVideoLayout().getViewHolder().btnConfirm.mState == CircularProgressState.STOP) {
            getCameraFragment().getCameraVideoManager().openPreviewVideoActivity();
        } else {
            stopProgress();
        }
    }

    @Override
    public void pvLayoutCancel() {
    }

    @Override
    public void longClickShort(long time) {
    }

    @Override
    public void pauseRecord(boolean isShort) {

    }

    @Override
    public void stopProgress() {
        getCameraFragment().stopVideoMultiple();
    }

}
