package com.zhongjh.multimedia.camera.ui.camera.state.type;

import static android.view.View.INVISIBLE;

import android.util.Log;
import android.view.View;

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
 * 单图完成状态的相关处理
 *
 * @author zhongjh
 * @date 2021/11/26
 */
public class PictureComplete extends StateMode {

    private static final String TAG = PictureComplete.class.getSimpleName();

    /**
     * @param cameraFragment     主要是多个状态围绕着CameraFragment进行相关处理
     * @param cameraStateManager 可以让状态更改别的状态
     */
    public PictureComplete(BaseCameraFragment<? extends CameraStateManager,
                ? extends CameraPictureManager,
                ? extends CameraVideoManager> cameraFragment, CameraStateManager cameraStateManager) {
        super(cameraFragment, cameraStateManager);
    }

    @Override
    public String getName() {
        return "PictureComplete";
    }

    @Override
    public void pvLayoutCommit() {
        getCameraFragment().setUiEnableFalse();
        // 拍照完成,移动文件
        getCameraFragment().movePictureFile();
    }

    @Override
    public void pvLayoutCancel() {
        // 取消线程
        getCameraFragment().getCameraPictureManager().cancelMovePictureFileTask();
        getCameraFragment().setUiEnableTrue();
        // 取消单图后的重置
        getCameraFragment().cancelOnResetBySinglePicture();
        // 恢复预览状态
        getCameraStateManagement().setState(getCameraStateManagement().getPreview());
    }

    @Override
    public void stopProgress() {
        // 取消线程
        getCameraFragment().getCameraPictureManager().cancelMovePictureFileTask();
        getCameraFragment().setUiEnableTrue();
    }
}
