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
    public String getName() {
        return "VideoMultipleIn";
    }

    @Override
    public void onActivityPause() {
        getCameraFragment().getCameraVideoManager().getVideoTimes().clear();
        // 重置所有
        getCameraFragment().resetStateAll();
        // 恢复预览状态
        getCameraStateManagement().setState(getCameraStateManagement().getPreview());
    }

    @Override
    public Boolean onBackPressed() {
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
    public void pauseRecord() {
        // 切回非录制中的状态
        getCameraStateManagement().setState(getCameraStateManagement().getVideoMultiple());
        getCameraFragment().getCameraManage().pauseVideo();
    }

    @Override
    public void onLongClickFinish() {
        getCameraFragment().getCameraManage().stopVideo();
    }

}
