package com.zhongjh.albumcamerarecorder.camera.camerastate;

import com.zhongjh.albumcamerarecorder.camera.CameraLayout;
import com.zhongjh.albumcamerarecorder.camera.camerastate.state.PictureComplete;
import com.zhongjh.albumcamerarecorder.camera.camerastate.state.Preview;
import com.zhongjh.albumcamerarecorder.camera.camerastate.state.VideoComplete;
import com.zhongjh.albumcamerarecorder.camera.common.Constants;
import com.zhongjh.albumcamerarecorder.camera.util.FileUtil;

import static com.zhongjh.albumcamerarecorder.camera.common.Constants.TYPE_SHORT;

/**
 * CameraLayout涉及到状态改变的事件都在这里
 *
 * @author zhongjh
 * @date 2021/11/25
 */
public class CameraStateManagement implements StateInterface {

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


    public CameraStateManagement(CameraLayout cameraLayout) {
        mCameraLayout = cameraLayout;
        preview = new Preview(cameraLayout, this);
        videoComplete = new VideoComplete(cameraLayout, this);
        pictureComplete = new PictureComplete(cameraLayout, this);
        state = preview;
    }

    @Override
    public void resetState() {
        state.resetState();
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
    }

    public StateInterface getPreview() {
        return preview;
    }

    public void setPreview(StateInterface preview) {
        this.preview = preview;
    }

    public StateInterface getVideoComplete() {
        return videoComplete;
    }

    public void setVideoComplete(StateInterface videoComplete) {
        this.videoComplete = videoComplete;
    }
}
