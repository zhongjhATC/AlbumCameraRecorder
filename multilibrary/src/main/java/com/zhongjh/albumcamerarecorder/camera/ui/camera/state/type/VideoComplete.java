package com.zhongjh.albumcamerarecorder.camera.ui.camera.state.type;

import static android.app.Activity.RESULT_OK;

import com.zhongjh.albumcamerarecorder.camera.ui.camera.BaseCameraFragment;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.presenter.BaseCameraVideoPresenter;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.CameraStateManagement;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.StateMode;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.presenter.BaseCameraPicturePresenter;
import com.zhongjh.albumcamerarecorder.camera.util.FileUtil;

/**
 * 单视频完成状态的相关处理
 *
 * @author zhongjh
 * @date 2021/11/25
 */
public class VideoComplete extends StateMode {

    /**
     * @param cameraFragment          主要是多个状态围绕着CameraFragment进行相关处理
     * @param cameraStateManagement 可以让状态更改别的状态
     */
    public VideoComplete(BaseCameraFragment<? extends CameraStateManagement,
            ? extends BaseCameraPicturePresenter,
            ? extends BaseCameraVideoPresenter> cameraFragment, CameraStateManagement cameraStateManagement) {
        super(cameraFragment, cameraStateManagement);
    }

    @Override
    public void resetState() {
        // 取消视频删除文件
        FileUtil.deleteFile(getCameraFragment().getCameraVideoPresenter().getVideoFile());
        // 恢复预览状态
        getCameraStateManagement().setState(getCameraStateManagement().getPreview());
        getCameraFragment().resetStateAll();
    }

    @Override
    public Boolean onBackPressed() {
        return null;
    }

    @Override
    public boolean onActivityResult(int resultCode) {
        if (resultCode != RESULT_OK) {
            getCameraFragment().showBottomMenu();
            // 如果是从视频界面回来，就重置状态
            getCameraStateManagement().setState(getCameraStateManagement().getPreview());
            return true;
        }
        return false;
    }

    @Override
    public void pvLayoutCommit() {
        // 恢复预览状态
        getCameraStateManagement().setState(getCameraStateManagement().getPreview());
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

    }
}
