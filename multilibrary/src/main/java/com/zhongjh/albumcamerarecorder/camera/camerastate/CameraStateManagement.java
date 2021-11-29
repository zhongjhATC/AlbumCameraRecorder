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


    public CameraStateManagement(CameraLayout cameraLayout) {
        mCameraLayout = cameraLayout;
        preview = new Preview(cameraLayout, this);
        videoComplete = new VideoComplete(cameraLayout, this);
        pictureComplete = new PictureComplete(cameraLayout, this);
        pictureMultiple = new PictureMultiple(cameraLayout, this);
        videoMultiple = new VideoMultiple(cameraLayout, this);
        videoIn = new VideoIn(cameraLayout, this);
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

    /**
     * 停止录像并且完成它，如果是因为视频过短则清除冗余数据
     *
     * @param isShort 是否因为视频过短而停止
     */
    public void stopRecord(boolean isShort) {
        mCameraLayout.mIsShort = isShort;
        mCameraLayout.mViewHolder.cameraView.stopVideo();
        if (isShort) {
            // 如果视频过短就是录制不成功
            mCameraLayout.resetState();
            // 判断不是分段录制 并且 没有分段录制片段
            if (!mCameraLayout.mIsSectionRecord && mCameraLayout.mVideoPaths.size() <= 0) {
                mCameraLayout.mViewHolder.pvLayout.reset();
            }
        } else {
            // 设置成视频录制完的状态
            setState(videoComplete);
        }
    }


    public StateInterface getState() {
        return state;
    }

    public void setState(StateInterface state) {
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

}
