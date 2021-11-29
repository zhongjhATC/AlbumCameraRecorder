package com.zhongjh.albumcamerarecorder.camera.camerastate;

import android.util.Log;

import com.zhongjh.albumcamerarecorder.camera.CameraLayout;
import com.zhongjh.albumcamerarecorder.camera.adapter.PhotoAdapter;
import com.zhongjh.albumcamerarecorder.camera.camerastate.state.PictureComplete;
import com.zhongjh.albumcamerarecorder.camera.camerastate.state.PictureMultiple;
import com.zhongjh.albumcamerarecorder.camera.camerastate.state.Preview;
import com.zhongjh.albumcamerarecorder.camera.camerastate.state.VideoComplete;
import com.zhongjh.albumcamerarecorder.camera.camerastate.state.VideoIn;
import com.zhongjh.albumcamerarecorder.camera.camerastate.state.VideoMultiple;
import com.zhongjh.albumcamerarecorder.camera.camerastate.state.VideoMultipleIn;

/**
 * CameraLayout涉及到状态改变的事件都在这里
 *
 * @author zhongjh
 * @date 2021/11/25
 */
public class CameraStateManagement implements StateInterface {

    private final String TAG = CameraStateManagement.class.getSimpleName();

    CameraLayout mCameraLayout;
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

    public CameraStateManagement(CameraLayout cameraLayout) {
        mCameraLayout = cameraLayout;
        preview = new Preview(cameraLayout, this);
        videoComplete = new VideoComplete(cameraLayout, this);
        pictureComplete = new PictureComplete(cameraLayout, this);
        pictureMultiple = new PictureMultiple(cameraLayout, this);
        videoMultiple = new VideoMultiple(cameraLayout, this);
        videoIn = new VideoIn(cameraLayout, this);
        videoMultipleIn = new VideoMultipleIn(cameraLayout, this);
        state = preview;
    }

    @Override
    public void resetState() {
        state.resetState();
    }

    @Override
    public Boolean onBackPressed() {
        return state.onBackPressed();
    }

    @Override
    public void pvLayoutCommit() {
        state.pvLayoutCommit();
    }

    @Override
    public void pvLayoutCancel() {
        state.pvLayoutCancel();
    }


    public StateInterface getState() {
        return state;
    }

    public void setState(StateInterface state) {
        if (state.equals(getVideoIn())) {
            this.state = state;
            return;
        }
        this.state = state;
        Log.d(TAG, state.toString());
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
