package com.zhongjh.albumcamerarecorder.camera.ui.camera.state.type;

import static android.app.Activity.RESULT_OK;

import com.zhongjh.albumcamerarecorder.camera.ui.camera.BaseCameraFragment;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.manager.CameraVideoManager;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.CameraStateManager;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.StateMode;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.manager.CameraPictureManager;
import com.zhongjh.common.utils.FileUtils;

/**
 * 单视频完成状态的相关处理
 *
 * @author zhongjh
 * @date 2021/11/25
 */
public class VideoComplete extends StateMode {

    /**
     * @param cameraFragment          主要是多个状态围绕着CameraFragment进行相关处理
     * @param cameraStateManager 可以让状态更改别的状态
     */
    public VideoComplete(BaseCameraFragment<? extends CameraStateManager,
            ? extends CameraPictureManager,
            ? extends CameraVideoManager> cameraFragment, CameraStateManager cameraStateManager) {
        super(cameraFragment, cameraStateManager);
    }

    @Override
    public String getName() {
        return "VideoComplete";
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
}
