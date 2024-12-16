package com.zhongjh.albumcamerarecorder.camera.ui.camera.state.type;

import android.util.Log;

import com.zhongjh.albumcamerarecorder.camera.ui.camera.BaseCameraFragment;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.manager.CameraVideoManager;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.CameraStateManager;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.StateMode;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.manager.CameraPictureManager;
import com.zhongjh.circularprogressview.CircularProgressState;

/**
 * 多个视频模式
 *
 * @author zhongjh
 * @date 2021/11/29
 */
public class VideoMultiple extends StateMode {

    /**
     * @param cameraFragment     主要是多个状态围绕着cameraLayout进行相关处理
     * @param cameraStateManager 可以让状态更改别的状态
     */
    public VideoMultiple(BaseCameraFragment<? extends CameraStateManager,
            ? extends CameraPictureManager,
            ? extends CameraVideoManager> cameraFragment, CameraStateManager cameraStateManager) {
        super(cameraFragment, cameraStateManager);
    }

    @Override
    public String getName() {
        return "VideoMultiple";
    }

    @Override
    public void onActivityPause() {
        getCameraFragment().getCameraVideoManager().getVideoTimes().clear();
        // 重置所有
        getCameraFragment().resetStateAll();
        // 恢复预览状态
        getCameraStateManagement().setState(getCameraStateManagement().getPreview());
    }

    @Override
    public boolean onActivityResult(int resultCode) {
        // 返回false处理视频
        return false;
    }

    @Override
    public void pvLayoutCommit() {
        if (getCameraFragment().getPhotoVideoLayout().getViewHolder().btnConfirm.mState == CircularProgressState.PLAY) {
            // 完成录制
            Log.d(TAG, "pvLayoutCommit完成录制");
            getCameraFragment().getCameraManage().stopVideo();
        } else {
            // 中断操作
            Log.d(TAG, "pvLayoutCommit中断操作");
            stopProgress();
        }
    }

    @Override
    public void stopProgress() {
        getCameraFragment().stopVideoMultiple();
    }

    @Override
    public void onLongClickFinish() {
        getCameraFragment().getCameraManage().stopVideo();
    }

}
