package com.zhongjh.albumcamerarecorder.camera.ui.camera.state.type;

import com.zhongjh.albumcamerarecorder.camera.ui.camera.BaseCameraFragment;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.manager.CameraVideoManager;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.CameraStateManager;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.StateMode;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.manager.CameraPictureManager;

/**
 * 多个图片状态，至少有一张图片情况
 *
 * @author zhongjh
 * @date 2021/11/29
 */
public class PictureMultiple extends StateMode {

    /**
     * @param cameraFragment        主要是多个状态围绕着cameraFragment进行相关处理
     * @param cameraStateManager 可以让状态更改别的状态
     */
    public PictureMultiple(BaseCameraFragment<? extends CameraStateManager,
            ? extends CameraPictureManager,
            ? extends CameraVideoManager> cameraFragment, CameraStateManager cameraStateManager) {
        super(cameraFragment, cameraStateManager);
    }

    @Override
    public void resetState() {

    }

    @Override
    public Boolean onBackPressed() {
        return null;
    }

    @Override
    public boolean onActivityResult(int resultCode) {
        return false;
    }

    @Override
    public void pvLayoutCommit() {
        getCameraFragment().setUiEnableFalse();
        // 拍照完成,移动文件
        getCameraFragment().movePictureFile();
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
        getCameraFragment().getCameraPictureManager().cancelMovePictureFileTask();
    }

}
