package com.zhongjh.albumcamerarecorder.camera.ui.camerastate.state;

import com.zhongjh.albumcamerarecorder.camera.ui.BaseCameraFragment;
import com.zhongjh.albumcamerarecorder.camera.ui.camerastate.CameraStateManagement;
import com.zhongjh.albumcamerarecorder.camera.ui.camerastate.StateMode;
import com.zhongjh.albumcamerarecorder.camera.ui.presenter.BaseCameraPicturePresenter;

/**
 * 多个图片状态，至少有一张图片情况
 *
 * @author zhongjh
 * @date 2021/11/29
 */
public class PictureMultiple extends StateMode {

    /**
     * @param cameraFragment        主要是多个状态围绕着cameraFragment进行相关处理
     * @param cameraStateManagement 可以让状态更改别的状态
     */
    public PictureMultiple(BaseCameraFragment<BaseCameraPicturePresenter> cameraFragment, CameraStateManagement cameraStateManagement) {
        super(cameraFragment, cameraStateManagement);
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
    public void stopRecord(boolean isShort) {

    }

    @Override
    public void stopProgress() {
        getCameraFragment().getCameraPicturePresenter().cancelMovePictureFileTask();
    }

}
