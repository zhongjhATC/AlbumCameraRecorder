package com.zhongjh.multimedia.camera.ui.camera.state.type;

import com.zhongjh.multimedia.camera.ui.camera.BaseCameraFragment;
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraVideoManager;
import com.zhongjh.multimedia.camera.ui.camera.state.CameraStateManager;
import com.zhongjh.multimedia.camera.ui.camera.state.StateMode;
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraPictureManager;
import com.zhongjh.multimedia.camera.ui.camera.BaseCameraFragment;
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraPictureManager;
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraVideoManager;
import com.zhongjh.multimedia.camera.ui.camera.state.CameraStateManager;
import com.zhongjh.multimedia.camera.ui.camera.state.StateMode;

/**
 * 预览状态的相关处理，默认状态
 *
 * @author zhongjh
 * @date 2021/11/26
 */
public class Preview extends StateMode {

    /**
     * @param cameraFragment          主要是多个状态围绕着cameraLayout进行相关处理
     * @param cameraStateManager 可以让状态更改别的状态
     */
    public Preview(BaseCameraFragment<? extends CameraStateManager,
                ? extends CameraPictureManager,
                ? extends CameraVideoManager> cameraFragment, CameraStateManager cameraStateManager) {
        super(cameraFragment, cameraStateManager);
    }

    @Override
    public String getName() {
        return "Preview";
    }

    @Override
    public Boolean onBackPressed() {
        // 如果是预览状态直接退出当前界面
        return false;
    }
}
