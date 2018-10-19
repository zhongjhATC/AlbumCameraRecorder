package com.zhongjh.cameraviewsoundrecorder.camera.widget.cameralayout;

import android.content.Context;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.widget.ImageView;

import com.zhongjh.cameraviewsoundrecorder.camera.CameraCallback;
import com.zhongjh.cameraviewsoundrecorder.camera.CameraContact;
import com.zhongjh.cameraviewsoundrecorder.camera.CameraOperation;
import com.zhongjh.cameraviewsoundrecorder.camera.common.Constants;
import com.zhongjh.cameraviewsoundrecorder.camera.listener.ErrorListener;

import static com.zhongjh.cameraviewsoundrecorder.camera.common.Constants.TYPE_PICTURE;
import static com.zhongjh.cameraviewsoundrecorder.camera.common.Constants.TYPE_SHORT;
import static com.zhongjh.cameraviewsoundrecorder.camera.common.Constants.TYPE_VIDEO;

/**
 * Created by zhongjh on 2018/8/7.
 */
public class CameraPresenter implements CameraContact.CameraPresenter {

    private Context mContext;
    private CameraContact.CameraView mCameraView;
    private CameraOperation mCameraOperation;

    public CameraPresenter(Context context, CameraContact.CameraView cameraView) {
        this.mContext = context;
        this.mCameraView = cameraView;
        this.mCameraOperation = new CameraOperation();
    }

    @Override
    public void start(SurfaceHolder holder, float screenProp) {
        mCameraOperation.doStartPreview(holder, screenProp);
    }

    @Override
    public void stop() {

    }

    @Override
    public void handleFocus(float x, float y, CameraCallback.FocusCallback callback) {
        mCameraOperation.handleFocus(mContext, x, y, callback);
    }

    @Override
    public void swtich(SurfaceHolder holder, float screenProp) {
        mCameraOperation.switchCamera(holder, screenProp);
    }

    @Override
    public void restart() {

    }

    @Override
    public void capture() {
        asdfsadfsf
        // 拍照，并且共享配置，
        mCameraOperation.takePicture((bitmap, isVertical) -> {
            // 显示图片
            mCameraView.showPicture(bitmap, isVertical);
        });
    }

    @Override
    public void record(Surface surface, float screenProp) {
        mCameraOperation.startRecord(surface, screenProp);
    }

    @Override
    public void stopRecord(final boolean isShort, long time) {
        mCameraOperation.stopRecord(isShort, (url, firstFrame) -> {
            if (isShort) {
                // 如果视频过短就是录制不成功
                mCameraView.resetState(TYPE_SHORT);
            } else {
                // 设置成视频播放状态
                mCameraView.setState(Constants.STATE_VIDEO);
                // 如果录制结束，播放该视频
                mCameraView.playVideo(firstFrame, url);
            }
        });
    }

    @Override
    public void cancle(SurfaceHolder holder, float screenProp) {
        // 根据不同状态处理相应的事件
        if (mCameraView.getState() == Constants.STATE_PICTURE){
            // 图片模式的取消
            mCameraOperation.doStartPreview(holder, screenProp); // 重新启动录像
            mCameraView.resetState(TYPE_PICTURE);   // 针对图片模式进行的重置
            mCameraView.setState(Constants.STATE_PREVIEW); // 设置空闲状态
        }else if(mCameraView.getState() == Constants.STATE_VIDEO){
            mCameraView.resetState(TYPE_VIDEO);     // 针对视频模式进行的重置
            mCameraView.setState(Constants.STATE_PREVIEW); // 设置空闲状态
        }
    }

    @Override
    public void confirm() {
        // 根据不同状态处理相应的事件
        if (mCameraView.getState() == Constants.STATE_PICTURE){
            // 图片模式的提交
            mCameraView.confirmState(TYPE_PICTURE);
            mCameraView.setState(Constants.STATE_PREVIEW); // 设置空闲状态
        }else if(mCameraView.getState() == Constants.STATE_VIDEO){
            mCameraView.confirmState(TYPE_VIDEO);
            mCameraView.setState(Constants.STATE_PREVIEW); // 设置空闲状态
        }
    }

    @Override
    public void zoom(float zoom, int type) {
        mCameraOperation.zoom(zoom, type);
    }

    @Override
    public void flash(String mode) {

    }

    @Override
    public void setErrorLinsenter(ErrorListener errorLisenter) {
        mCameraOperation.setErrorLinsenter(errorLisenter);
    }

    @Override
    public void doOpenCamera() {
        mCameraOperation.doOpenCamera(() -> mCameraOperation.doStartPreview(mCameraView.getSurfaceHolder(), mCameraView.getScreenProp()));
    }

    @Override
    public void doDestroyCamera() {
        mCameraOperation.doDestroyCamera();
    }

    @Override
    public void registerSensorManager(Context mContext) {
        mCameraOperation.registerSensorManager(mContext);
    }

    @Override
    public void unregisterSensorManager(Context mContext) {
        mCameraOperation.unregisterSensorManager(mContext);
    }

    @Override
    public void setImageViewSwitchAndFlash(ImageView imgSwitch, ImageView imgFlash) {
        mCameraOperation.setImageViewSwitchAndFlash(imgSwitch, imgFlash);
    }

    @Override
    public void isPreview(boolean b) {
        mCameraOperation.isPreview(b);
    }

    @Override
    public void setSaveVideoPath(String saveVideoPath) {
        mCameraOperation.setSaveVideoPath(saveVideoPath);
    }

    @Override
    public void setMediaQuality(int mediaQualityMiddle) {
        mCameraOperation.setMediaQuality(mediaQualityMiddle);
    }

    @Override
    public void setPictureMaxNumber(int i) {
        mCameraOperation.setPictureMaxNumber(i);
    }

}
