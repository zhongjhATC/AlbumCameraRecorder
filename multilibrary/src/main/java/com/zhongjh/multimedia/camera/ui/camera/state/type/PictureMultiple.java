package com.zhongjh.multimedia.camera.ui.camera.state.type;

import android.util.Log;

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
    public String getName() {
        return "PictureMultiple";
    }

    @Override
    public void pvLayoutCommit() {
        getCameraFragment().setUiEnableFalse();
        // 拍照完成,移动文件
        getCameraFragment().movePictureFile();
    }

    @Override
    public void stopProgress() {
        getCameraFragment().setUiEnableTrue();
        // 取消线程
        getCameraFragment().getCameraPictureManager().cancelMovePictureFileTask();
    }

}
