package com.zhongjh.albumcamerarecorder.camera.camerastate.state;

import android.util.Log;

import com.zhongjh.albumcamerarecorder.camera.CameraLayout;
import com.zhongjh.albumcamerarecorder.camera.camerastate.CameraStateManagement;
import com.zhongjh.albumcamerarecorder.camera.camerastate.StateMode;
import com.zhongjh.albumcamerarecorder.utils.ViewBusinessUtils;

/**
 * 多个视频模式
 *
 * @author zhongjh
 * @date 2021/11/29
 */
public class VideoMultiple extends StateMode {

    /**
     * @param cameraLayout          主要是多个状态围绕着cameraLayout进行相关处理
     * @param cameraStateManagement 可以让状态更改别的状态
     */
    public VideoMultiple(CameraLayout cameraLayout, CameraStateManagement cameraStateManagement) {
        super(cameraLayout, cameraStateManagement);
    }

    @Override
    public void resetState() {
        // 重置所有
        getCameraLayout().resetStateAll();
        // 恢复预览状态
        getCameraStateManagement().setState(getCameraStateManagement().getPreview());
    }

    @Override
    public Boolean onBackPressed() {
        return null;
    }

    @Override
    public boolean onActivityResult(int resultCode) {
        return true;
    }

    @Override
    public void pvLayoutCommit() {
        getCameraLayout().openPreviewVideoActivity();
    }

    @Override
    public void pvLayoutCancel() {
        getCameraLayout().removeVideoMultiple();
    }

    @Override
    public void longClickShort(long time) {
    }

    @Override
    public void stopRecord(boolean isShort) {

    }

}
