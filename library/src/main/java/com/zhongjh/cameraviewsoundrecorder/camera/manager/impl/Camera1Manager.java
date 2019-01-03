package com.zhongjh.cameraviewsoundrecorder.camera.manager.impl;

import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.zhongjh.cameraviewsoundrecorder.camera.config.CameraConfig;
import com.zhongjh.cameraviewsoundrecorder.camera.entity.Size;
import com.zhongjh.cameraviewsoundrecorder.camera.listener.OnCameraResultListener;
import com.zhongjh.cameraviewsoundrecorder.camera.manager.listener.CameraCloseListener;
import com.zhongjh.cameraviewsoundrecorder.camera.manager.listener.CameraOpenListener;
import com.zhongjh.cameraviewsoundrecorder.camera.manager.listener.CameraPictureListener;
import com.zhongjh.cameraviewsoundrecorder.camera.manager.listener.CameraVideoListener;
import com.zhongjh.cameraviewsoundrecorder.camera.util.CameraUtils;

import java.io.File;
import java.util.List;

/**
 * Created by zhongjh on 2019/1/3.
 */

public class Camera1Manager extends BaseCameraManager<Integer, SurfaceHolder.Callback> {

    private static final String TAG = "Camera1Manager";

    private Camera camera;
    private Surface surface;

    private int orientation;
    private int displayRotation = 0;

    private File outputPath;
    private CameraVideoListener videoListener;
    private CameraPictureListener photoListener;

    private Integer futurFlashMode;

    @Override
    public void openCamera(Integer cameraId, CameraOpenListener<Integer, SurfaceHolder.Callback> cameraOpenListener) {
        this.mCameraId = cameraId;
        mBackgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    camera = Camera.open(cameraId);
                    prepareCameraOutputs();
                    if (futurFlashMode != null) {
                        setFlashMode(futurFlashMode);
                        futurFlashMode = null;
                    }
                    if (cameraOpenListener != null) {
                        mUiiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                cameraOpenListener.onCameraOpened(cameraId, mPreviewSize, new SurfaceHolder.Callback() {
                                    @Override
                                    public void surfaceCreated(SurfaceHolder surfaceHolder) {
                                        if (surfaceHolder.getSurface() == null) {
                                            return;
                                        }

                                        surface = surfaceHolder.getSurface();

                                        try {
                                            camera.stopPreview();
                                        } catch (Exception ignore) {
                                        }

                                        startPreview(surfaceHolder);
                                    }

                                    @Override
                                    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
                                        if (surfaceHolder.getSurface() == null) {
                                            return;
                                        }

                                        surface = surfaceHolder.getSurface();

                                        try {
                                            camera.stopPreview();
                                        } catch (Exception ignore) {
                                        }

                                        startPreview(surfaceHolder);
                                    }

                                    @Override
                                    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

                                    }
                                });
                            }
                        });
                    }
                }
            }
        })


    }

    @Override
    public void closeCamera(CameraCloseListener<Integer> cameraCloseListener) {

    }

    @Override
    public void takePicture(File photoFile, CameraPictureListener cameraPictureListener, OnCameraResultListener callback) {

    }

    @Override
    public void startVideoRecord(File videoFile, CameraVideoListener cameraVideoListener) {

    }

    @Override
    public void stopVideoRecord(OnCameraResultListener callback) {

    }

    @Override
    public void setFlashMode(int flashMode) {
        if (camera != null) {
            setFlashMode(camera, camera.getParameters(), flashMode);
        } else {
            futurFlashMode = flashMode;
        }
    }

    @Override
    public Size getPictureSizeForQuality(int mediaQuality) {
        return null;
    }

    @Override
    public CharSequence[] getPictureQualityOptions() {
        return new CharSequence[0];
    }

    @Override
    public CharSequence[] getVideoQualityOptions() {
        return new CharSequence[0];
    }

    @Override
    protected void prepareCameraOutputs() {
        try {
            if (cameraConfigProvider.getMediaQuality() == CameraConfig.MEDIA_QUALITY_AUTO) {
                mCamcorderProfile = CameraUtils.getCamcorderProfile(mCameraId, cameraConfigProvider.getVideoFileSize(), cameraConfigProvider.getMinimumVideoDuration());
            } else
                mCamcorderProfile = CameraUtils.getCamcorderProfile(cameraConfigProvider.getMediaQuality(), mCameraId);

            final List<Size> previewSizes = Size.fromList(camera.getParameters().getSupportedPreviewSizes());
            final List<Size> pictureSizes = Size.fromList(camera.getParameters().getSupportedPictureSizes());
            List<Size> videoSizes;
            if (Build.VERSION.SDK_INT > 10)
                videoSizes = Size.fromList(camera.getParameters().getSupportedVideoSizes());
            else videoSizes = previewSizes;

            mVideoSize = CameraUtils.getSizeWithClosestRatio(
                    (videoSizes == null || videoSizes.isEmpty()) ? previewSizes : videoSizes,
                    mCamcorderProfile.videoFrameWidth, mCamcorderProfile.videoFrameHeight);

            mPhotoSize = CameraUtils.getPictureSize(
                    (pictureSizes == null || pictureSizes.isEmpty()) ? previewSizes : pictureSizes,
                    cameraConfigProvider.getMediaQuality() == CameraConfig.MEDIA_QUALITY_AUTO
                            ? CameraConfig.MEDIA_QUALITY_HIGHEST : cameraConfigProvider.getMediaQuality());

            if (cameraConfigProvider.getMediaAction() == CameraConfig.MEDIA_ACTION_PHOTO
                    || cameraConfigProvider.getMediaAction() == CameraConfig.MEDIA_ACTION_UNSPECIFIED) {
                mPreviewSize = CameraUtils.getSizeWithClosestRatio(previewSizes, mPhotoSize.getWidth(), mPhotoSize.getHeight());
            } else {
                mPreviewSize = CameraUtils.getSizeWithClosestRatio(previewSizes, mVideoSize.getWidth(), mVideoSize.getHeight());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while setup camera sizes.");
        }
    }

    @Override
    protected boolean prepareVideoRecorder() {
        return false;
    }

    @Override
    protected void onMaxDurationReached() {

    }

    @Override
    protected void onMaxFileSizeReached() {

    }

    @Override
    protected int getPhotoOrientation(int sensorPosition) {
        return 0;
    }

    @Override
    protected int getVideoOrientation(int sensorPosition) {
        return 0;
    }

    /**
     * 启动预览
     * @param surfaceHolder holder
     */
    private void startPreview(SurfaceHolder surfaceHolder){
        try {
            final Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraId, cameraInfo);
            int cameraRotationOffset = cameraInfo.orientation;

            final Camera.Parameters parameters = camera.getParameters();
            setAutoFocus(camera, parameters);
            setFlashMode(cameraConfigProvider.getFlashMode());

            if (cameraConfigProvider.getMediaAction() == CameraConfig.MEDIA_ACTION_PHOTO
                    || cameraConfigProvider.getMediaAction() == CameraConfig.MEDIA_ACTION_UNSPECIFIED)
                turnPhotoCameraFeaturesOn(camera, parameters);
            else if (cameraConfigProvider.getMediaAction() == CameraConfig.MEDIA_ACTION_PHOTO)
                turnVideoCameraFeaturesOn(camera, parameters);



        }
    }

    /**
     * 设置用于拍照的连续自动对焦模式。
     */
    private void turnPhotoCameraFeaturesOn(Camera camera, Camera.Parameters parameters) {
        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        camera.setParameters(parameters);
    }

    /**
     * 设置用于视频记录的连续自动对焦模式。
     */
    private void turnVideoCameraFeaturesOn(Camera camera, Camera.Parameters parameters) {
        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        camera.setParameters(parameters);
    }

    /**
     * 设置自动对焦
     * @param camera 相机
     * @param parameters 参数
     */
    private void setAutoFocus(Camera camera, Camera.Parameters parameters) {
        try {
            if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                camera.setParameters(parameters);
            }
        } catch (Exception ignore) {
        }
    }

    private void setFlashMode(Camera camera, Camera.Parameters parameters, @CameraConfig.FlashMode int flashMode) {
        try {
            switch (flashMode) {
                case CameraConfig.FLASH_MODE_AUTO:
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                    break;
                case CameraConfig.FLASH_MODE_ON:
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    break;
                case CameraConfig.FLASH_MODE_OFF:
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    break;
                default:
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                    break;
            }
            camera.setParameters(parameters);
        } catch (Exception ignore) {
        }
    }


}
