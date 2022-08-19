package com.zhongjh.albumcamerarecorder.camera.ui.camerastate;

import android.util.Log;
import android.view.View;

import com.zhongjh.albumcamerarecorder.camera.ui.BaseCameraFragment;
import com.zhongjh.albumcamerarecorder.camera.ui.camerastate.state.PictureComplete;
import com.zhongjh.albumcamerarecorder.camera.ui.camerastate.state.PictureMultiple;
import com.zhongjh.albumcamerarecorder.camera.ui.camerastate.state.Preview;
import com.zhongjh.albumcamerarecorder.camera.ui.camerastate.state.VideoComplete;
import com.zhongjh.albumcamerarecorder.camera.ui.camerastate.state.VideoIn;
import com.zhongjh.albumcamerarecorder.camera.ui.camerastate.state.VideoMultiple;
import com.zhongjh.albumcamerarecorder.camera.ui.camerastate.state.VideoMultipleIn;

/**
 * CameraLayout涉及到状态改变的事件都在这里
 * 录制视频：
 * 默认状态Preview - 录制中VideoIn - 录制完成VideoComplete - 关闭视频预览回到初始界面Preview
 * 录制多个视频：
 * 默认状态Preview - 录制中VideoMultipleIn - 录制完一小节VideoMultiple - 回退节点至没有视频节点Preview,如果有节点则是VideoMultiple - 即使点击录制完成依然保持VideoMultiple
 *
 * @author zhongjh
 * @date 2021/11/25
 */
public class CameraStateManagement implements StateInterface {

    private final String TAG = CameraStateManagement.class.getSimpleName();

    BaseCameraFragment mCameraFragment;
    /**
     * 当前状态
     */
    StateInterface state;
    /**
     * 预览状态
     */
    StateInterface preview;
    /**
     * 视频完成状态
     */
    StateInterface videoComplete;
    /**
     * 图片完成状态
     */
    StateInterface pictureComplete;
    /**
     * 多个图片状态，至少有一张图片情况
     */
    StateInterface pictureMultiple;
    /**
     * 多个视频状态，至少有一段视频情况
     */
    StateInterface videoMultiple;
    /**
     * 正在录制视频中的状态
     */
    StateInterface videoIn;
    /**
     * 正在录制多个视频中的状态
     */
    StateInterface videoMultipleIn;

    public CameraStateManagement(BaseCameraFragment cameraFragment) {
        mCameraFragment = cameraFragment;
        // 初始化相关状态逻辑
        preview = new Preview(cameraFragment, this);
        videoComplete = new VideoComplete(cameraFragment, this);
        pictureComplete = new PictureComplete(cameraFragment, this);
        pictureMultiple = new PictureMultiple(cameraFragment, this);
        videoMultiple = new VideoMultiple(cameraFragment, this);
        videoIn = new VideoIn(cameraFragment, this);
        videoMultipleIn = new VideoMultipleIn(cameraFragment, this);
        // 设置当前默认状态
        state = preview;
    }

    @Override
    public void resetState() {
        Log.d(TAG, "resetState");
        state.resetState();
    }

    @Override
    public Boolean onBackPressed() {
        Log.d(TAG, "onBackPressed");
        return state.onBackPressed();
    }

    @Override
    public boolean onActivityResult(int resultCode) {
        return state.onActivityResult(resultCode);
    }

    @Override
    public void pvLayoutCommit() {
        Log.d(TAG, "pvLayoutCommit");
        state.pvLayoutCommit();
    }

    @Override
    public void pvLayoutCancel() {
        Log.d(TAG, "pvLayoutCancel");
        state.pvLayoutCancel();
    }

    @Override
    public void longClickShort(long time) {
        Log.d(TAG, "longClickShort");
        state.longClickShort(time);
    }

    @Override
    public void stopRecord(boolean isShort) {
        Log.d(TAG, "stopRecord");
        mCameraFragment.mIsShort = isShort;
        mCameraFragment.getCameraView().stopVideo();
        // 显示菜单
        mCameraFragment.setMenuVisibility(View.VISIBLE);
        state.stopRecord(isShort);
    }

    @Override
    public void stopProgress() {
        state.stopProgress();
    }

    /**
     * @return 当前状态
     */
    public StateInterface getState() {
        Log.d(TAG, "getState" + state.toString());
        return state;
    }

    /**
     * 赋值当前状态
     */
    public void setState(StateInterface state) {
        Log.d(TAG, "setState" + state.toString());
        this.state = state;
    }

    public StateInterface getPreview() {
        return preview;
    }

    public StateInterface getVideoComplete() {
        return videoComplete;
    }

    public StateInterface getPictureComplete() {
        return pictureComplete;
    }

    public StateInterface getPictureMultiple() {
        return pictureMultiple;
    }

    public StateInterface getVideoMultiple() {
        return videoMultiple;
    }

    public StateInterface getVideoIn() {
        return videoIn;
    }

    public StateInterface getVideoMultipleIn() {
        return videoMultipleIn;
    }

}
