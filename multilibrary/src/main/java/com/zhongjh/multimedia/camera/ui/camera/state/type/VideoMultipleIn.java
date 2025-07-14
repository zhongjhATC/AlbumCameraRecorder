package com.zhongjh.multimedia.camera.ui.camera.state.type;

import com.zhongjh.multimedia.camera.ui.camera.BaseCameraFragment;
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraVideoManager;
import com.zhongjh.multimedia.camera.ui.camera.state.CameraStateManager;
import com.zhongjh.multimedia.camera.ui.camera.state.StateMode;
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraPictureManager;

/**
 * 视频录制中
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
        getCameraFragment().getCameraVideoManager().videoTime = 0L;
        // 重置所有
        getCameraFragment().resetStateAll();
        // 恢复预览状态
        getCameraStateManagement().setState(getCameraStateManagement().getPreview());
    }

    @Override
    public Boolean onBackPressed() {
        getCameraFragment().cameraManage.closeVideo();
        getCameraFragment().getPhotoVideoLayout().resetConfirm();

        if (getCameraFragment().getCameraVideoManager().videoTime == 0L) {
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
    public void pauseRecord() {
        // 切回非录制中的状态
        getCameraStateManagement().setState(getCameraStateManagement().getVideoMultiple());
        getCameraFragment().cameraManage.pauseVideo();
    }

    @Override
    public void onLongClickFinish() {
        getCameraFragment().cameraManage.stopVideo();
    }

}
