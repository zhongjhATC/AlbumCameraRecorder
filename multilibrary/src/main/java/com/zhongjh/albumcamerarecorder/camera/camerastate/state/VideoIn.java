package com.zhongjh.albumcamerarecorder.camera.camerastate.state;

import android.util.Log;

import com.zhongjh.albumcamerarecorder.camera.CameraLayout;
import com.zhongjh.albumcamerarecorder.camera.camerastate.CameraStateManagement;
import com.zhongjh.albumcamerarecorder.camera.camerastate.StateMode;
import com.zhongjh.albumcamerarecorder.utils.ViewBusinessUtils;

/**
 * 正在录制视频中的状态
 *
 * @author zhongjh
 * @date 2021/11/25
 */
public class VideoIn extends StateMode {
    /**
     * @param cameraLayout          主要是多个状态围绕着cameraLayout进行相关处理
     * @param cameraStateManagement 可以让状态更改别的状态
     */
    public VideoIn(CameraLayout cameraLayout, CameraStateManagement cameraStateManagement) {
        super(cameraLayout, cameraStateManagement);
    }

    @Override
    public void resetState() {
        // 恢复预览状态
        getCameraStateManagement().setState(getCameraStateManagement().getPreview());
    }

    @Override
    public Boolean onBackPressed() {
        // 如果是录制中则暂停视频
        getCameraLayout().setBreakOff(true);
        getCameraLayout().mViewHolder.cameraView.stopVideo();
        // 重置按钮
        getCameraLayout().mViewHolder.pvLayout.reset();
        // 恢复预览状态
        getCameraStateManagement().setState(getCameraStateManagement().getPreview());
        return true;
    }

    @Override
    public boolean onActivityResult(int resultCode) {
        return true;
    }

    @Override
    public void pvLayoutCommit() {

    }

    @Override
    public void pvLayoutCancel() {

    }

    @Override
    public void longClickShort(long time) {
        // 母窗体显示底部
        ViewBusinessUtils.setTabLayoutScroll(true, getCameraLayout().mMainActivity,
                getCameraLayout().mViewHolder.pvLayout);
    }

    @Override
    public void stopRecord(boolean isShort) {
        if (isShort) {
            // 重置底部按钮
            getCameraLayout().mViewHolder.pvLayout.reset();
            getCameraStateManagement().setState(getCameraStateManagement().getPreview());
        } else {
            getCameraStateManagement().setState(getCameraStateManagement().getVideoComplete());
        }
    }
}
