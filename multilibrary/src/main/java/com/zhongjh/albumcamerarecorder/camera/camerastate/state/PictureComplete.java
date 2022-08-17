package com.zhongjh.albumcamerarecorder.camera.camerastate.state;

import static android.view.View.INVISIBLE;

import android.view.View;

import com.zhongjh.albumcamerarecorder.camera.BaseCameraFragment;
import com.zhongjh.albumcamerarecorder.camera.camerastate.CameraStateManagement;
import com.zhongjh.albumcamerarecorder.camera.camerastate.StateMode;
import com.zhongjh.albumcamerarecorder.camera.util.FileUtil;

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
    public PictureComplete(BaseCameraFragment cameraFragment, CameraStateManagement cameraStateManagement) {
        super(cameraFragment, cameraStateManagement);
    }

    @Override
    public void resetState() {
        // 重新启用cameraView
        if (!getCameraFragment().mViewHolder.cameraView.isOpened()) {
            getCameraFragment().mViewHolder.cameraView.open();
        }

        // 隐藏图片view
        getCameraFragment().mViewHolder.imgPhoto.setVisibility(INVISIBLE);
        getCameraFragment().mViewHolder.flShow.setVisibility(INVISIBLE);

        // 删除图片
        if (getCameraFragment().mPhotoFile != null) {
            FileUtil.deleteFile(getCameraFragment().mPhotoFile);
        }

        getCameraFragment().mViewHolder.pvLayout.getViewHolder().btnClickOrLong.setVisibility(View.VISIBLE);

        getCameraFragment().mViewHolder.pvLayout.reset();

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
        getCameraFragment().mViewHolder.cameraView.open();
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
        if (getCameraFragment().mMovePictureFileTask != null) {
            getCameraFragment().mMovePictureFileTask.cancel();
        }
    }
}
