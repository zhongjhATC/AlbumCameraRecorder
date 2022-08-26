package com.zhongjh.albumcamerarecorder.camera.ui.camera.state.type;

import static android.view.View.INVISIBLE;

import android.view.View;

import com.zhongjh.albumcamerarecorder.camera.ui.camera.BaseCameraFragment;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.presenter.BaseCameraVideoPresenter;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.CameraStateManagement;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.StateMode;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.presenter.BaseCameraPicturePresenter;

/**
 * 单图完成状态的相关处理
 *
 * @author zhongjh
 * @date 2021/11/26
 */
public class PictureComplete extends StateMode {

    /**
     * @param cameraFragment        主要是多个状态围绕着CameraFragment进行相关处理
     * @param cameraStateManagement 可以让状态更改别的状态
     */
    public PictureComplete(BaseCameraFragment<? extends CameraStateManagement,
            ? extends BaseCameraPicturePresenter,
            ? extends BaseCameraVideoPresenter> cameraFragment, CameraStateManagement cameraStateManagement) {
        super(cameraFragment, cameraStateManagement);
    }

    @Override
    public void resetState() {
        // 重新启用cameraView
        if (!getCameraFragment().getCameraView().isOpened()) {
            getCameraFragment().getCameraView().open();
        }

        // 隐藏图片view
        getCameraFragment().getSinglePhotoView().setVisibility(INVISIBLE);

        // 删除图片
        getCameraFragment().getCameraPicturePresenter().deletePhotoFile();

        getCameraFragment().getPhotoVideoLayout().getViewHolder().btnClickOrLong.setVisibility(View.VISIBLE);

        getCameraFragment().getPhotoVideoLayout().reset();

        // 恢复预览状态
        getCameraStateManagement().setState(getCameraStateManagement().getPreview());
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

        // 恢复预览状态
        getCameraStateManagement().setState(getCameraStateManagement().getPreview());
    }

    @Override
    public void pvLayoutCancel() {
        getCameraFragment().cancelOnResetBySinglePicture();
        getCameraFragment().getCameraView().open();
        // 恢复预览状态
        getCameraStateManagement().setState(getCameraStateManagement().getPreview());
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
