package com.zhongjh.cameraviewsoundrecorder.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.zhongjh.cameraviewsoundrecorder.camera.other.CameraCallback;
import com.zhongjh.cameraviewsoundrecorder.camera.other.CameraOperation;
import com.zhongjh.cameraviewsoundrecorder.common.Constants;

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
    public void stopRecord(boolean isShort, long time) {
        mCameraOperation.stopRecord(isShort, new CameraInterface.StopRecordCallback() {
            @Override
            public void recordResult(String url, Bitmap firstFrame) {
                if (isShort) {
                    machine.getView().resetState(JCameraView.TYPE_SHORT);
                } else {
                    machine.getView().playVideo(firstFrame, url);
                    machine.setState(machine.getBorrowVideoState());
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

    }

    @Override
    public void flash(String mode) {

    }

}
