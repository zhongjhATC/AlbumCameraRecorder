package com.zhongjh.cameraviewsoundrecorder.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.zhongjh.cameraviewsoundrecorder.camera.other.CameraCallback;
import com.zhongjh.cameraviewsoundrecorder.camera.other.CameraOperation;
import com.zhongjh.cameraviewsoundrecorder.common.Constants;
import com.zhongjh.cameraviewsoundrecorder.listener.ErrorListener;
import com.zhongjh.cameraviewsoundrecorder.util.PermissionUtil;

import static com.zhongjh.cameraviewsoundrecorder.common.Constants.TYPE_SHORT;

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

    }

    @Override
    public void stop() {

    }

    @Override
    public void foucs(float x, float y, CameraCallback.FocusCallback callback) {

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
        mCameraOperation.takePicture(new CameraCallback.TakePictureCallback() {
            @Override
            public void captureResult(Bitmap bitmap, boolean isVertical) {
                // 显示图片
                mCameraView.showPicture(bitmap, isVertical);
                // 设置当前模式是图片模式
                mCameraView.setState(Constants.STATE_PICTURE);
            }
        });
    }

    @Override
    public void record(Surface surface, float screenProp) {
        mCameraOperation.startRecord(surface, screenProp);
    }

    @Override
    public void stopRecord(final boolean isShort, long time) {
        mCameraOperation.stopRecord(isShort, new CameraCallback.StopRecordCallback() {
            @Override
            public void recordResult(String url, Bitmap firstFrame) {
                if (isShort) {
                    // 如果视频过短就是录制不成功
                    mCameraView.resetState(TYPE_SHORT);
                } else {
                    // 设置成视频播放状态
                    mCameraView.setState(Constants.STATE_VIDEO);
                    // 如果录制结束，播放该视频
                    mCameraView.playVideo(firstFrame, url);
                }
            }
        });
    }

    @Override
    public void cancle(SurfaceHolder holder, float screenProp) {

    }

    @Override
    public void confirm() {

    }

    @Override
    public void zoom(float zoom, int type) {
        mCameraOperation.zoom(zoom,type);
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
        mCameraOperation.doOpenCamera(new CameraCallback.CameraOpenOverCallback() {
            @Override
            public void cameraHasOpened() {
                mCameraOperation.doStartPreview(mCameraView.getSurfaceHolder(), mCameraView.getScreenProp());
            }
        });
    }

    @Override
    public void doDestroyCamera() {
        mCameraOperation.doDestroyCamera();
    }

}
