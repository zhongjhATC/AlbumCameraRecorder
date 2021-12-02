package com.zhongjh.albumcamerarecorder.camera.camerastate.state;

import android.util.Log;

import com.zhongjh.albumcamerarecorder.camera.CameraLayout;
import com.zhongjh.albumcamerarecorder.camera.camerastate.CameraStateManagement;
import com.zhongjh.albumcamerarecorder.camera.camerastate.StateMode;
import com.zhongjh.albumcamerarecorder.utils.ViewBusinessUtils;

/**
 * 多个视频的状态录制中
 *
 * @author zhongjh
 * @date 2021/11/29
 */
public class VideoMultipleIn extends StateMode {

    /**
     * @param cameraLayout          主要是多个状态围绕着cameraLayout进行相关处理
     * @param cameraStateManagement 可以让状态更改别的状态
     */
    public VideoMultipleIn(CameraLayout cameraLayout, CameraStateManagement cameraStateManagement) {
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
        getCameraLayout().mViewHolder.pvLayout.resetBtnConfirm();
        getCameraLayout().mViewHolder.pvLayout.getViewHolder().btnClickOrLong.selectionRecordRollBack();

        if (getCameraLayout().mVideoPaths.size() <= 0) {
            // 如果没有视频节点则重置所有按钮
            getCameraLayout().mViewHolder.pvLayout.reset();
            // 恢复预览状态
            getCameraStateManagement().setState(getCameraStateManagement().getPreview());
        } else {
            // 如果有视频节点则中断中心按钮
            getCameraLayout().mViewHolder.pvLayout.getViewHolder().btnClickOrLong.breakOff();
            // 恢复预览状态
            getCameraStateManagement().setState(getCameraStateManagement().getVideoMultiple());
        }
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
        getCameraLayout().mViewHolder.pvLayout.getViewHolder().btnClickOrLong.selectionRecordRollBack();
        if (getCameraLayout().mVideoPaths.size() <= 0) {
            // 母窗体显示底部
            ViewBusinessUtils.setTabLayoutScroll(true, getCameraLayout().mMainActivity,
                    getCameraLayout().mViewHolder.pvLayout);
        }
    }

    @Override
    public void stopRecord(boolean isShort) {
        if (isShort) {
            // 如果没有视频数据
            if (getCameraLayout().mVideoPaths.size() <= 0) {
                // 则重置底部按钮
                getCameraLayout().mViewHolder.pvLayout.reset();
                // 恢复预览状态
                getCameraStateManagement().setState(getCameraStateManagement().getPreview());
            } else {
                // 设置成多个视频状态
                getCameraStateManagement().setState(getCameraStateManagement().getVideoMultiple());
            }
        } else {
            // 设置成多个视频状态
            getCameraStateManagement().setState(getCameraStateManagement().getVideoMultiple());
        }
    }

}
