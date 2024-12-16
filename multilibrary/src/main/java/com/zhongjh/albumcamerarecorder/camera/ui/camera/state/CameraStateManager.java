package com.zhongjh.albumcamerarecorder.camera.ui.camera.state;

import android.util.Log;
import android.view.View;

import com.zhongjh.albumcamerarecorder.camera.ui.camera.BaseCameraFragment;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.manager.CameraPictureManager;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.manager.CameraVideoManager;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.type.PictureComplete;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.type.PictureMultiple;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.type.Preview;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.type.VideoMultiple;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.type.VideoMultipleIn;

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
public class CameraStateManager implements IState {

    private final String TAG = CameraStateManager.class.getSimpleName();

    BaseCameraFragment<? extends CameraStateManager,
            ? extends CameraPictureManager,
            ? extends CameraVideoManager> mCameraFragment;
    /**
     * 当前状态
     */
    IState state;
    /**
     * 预览状态
     */
    IState preview;
    /**
     * 图片完成状态
     */
    IState pictureComplete;
    /**
     * 多个图片状态，至少有一张图片情况
     */
    IState pictureMultiple;
    /**
     * 多个视频状态，至少有一段视频情况
     */
    IState videoMultiple;
    /**
     * 正在录制多个视频中的状态
     */
    IState videoMultipleIn;

    public CameraStateManager(BaseCameraFragment<? extends CameraStateManager,
            ? extends CameraPictureManager,
            ? extends CameraVideoManager> cameraFragment) {
        mCameraFragment = cameraFragment;
        // 初始化相关状态逻辑
        preview = new Preview(cameraFragment, this);
        pictureComplete = new PictureComplete(cameraFragment, this);
        pictureMultiple = new PictureMultiple(cameraFragment, this);
        videoMultiple = new VideoMultiple(cameraFragment, this);
        videoMultipleIn = new VideoMultipleIn(cameraFragment, this);
        // 设置当前默认状态
        state = preview;
    }

    @Override
    public String getName() {
        return "CameraStateManager";
    }

    @Override
    public void onActivityPause() {
        Log.d(TAG, "onActivityPause " + state.getName());
        state.onActivityPause();
    }

    @Override
    public Boolean onBackPressed() {
        Log.d(TAG, "onBackPressed " + state.getName());
        return state.onBackPressed();
    }

    @Override
    public void pvLayoutCommit() {
        Log.d(TAG, "pvLayoutCommit " + state.getName());
        state.pvLayoutCommit();
    }

    @Override
    public void pvLayoutCancel() {
        Log.d(TAG, "pvLayoutCancel " + state.getName());
        state.pvLayoutCancel();
    }

    @Override
    public void pauseRecord() {
        Log.d(TAG, "pauseRecord " + state.getName());
        // 显示右上角菜单
        mCameraFragment.setMenuVisibility(View.VISIBLE);
        state.pauseRecord();
    }

    @Override
    public void stopProgress() {
        Log.d(TAG, "stopProgress " + state.getName());
        state.stopProgress();
    }

    @Override
    public void onLongClickFinish() {
        Log.d(TAG, "doneProgress " + state.getName());
        state.onLongClickFinish();
    }

    /**
     * @return 当前状态
     */
    public IState getState() {
        Log.d(TAG, "getState " + state.getName());
        return state;
    }

    /**
     * 赋值当前状态
     */
    public void setState(IState state) {
        Log.d(TAG, "setState " + state.getName());
        this.state = state;
    }

    public IState getPreview() {
        return preview;
    }

    public IState getPictureComplete() {
        return pictureComplete;
    }

    public IState getPictureMultiple() {
        return pictureMultiple;
    }

    public IState getVideoMultiple() {
        return videoMultiple;
    }

    public IState getVideoMultipleIn() {
        return videoMultipleIn;
    }

}
