package com.zhongjh.albumcamerarecorder.camera.manager.impl;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.zhongjh.albumcamerarecorder.camera.config.CameraConfig;
import com.zhongjh.albumcamerarecorder.camera.config.CameraConfigProvider;
import com.zhongjh.albumcamerarecorder.camera.entity.Size;
import com.zhongjh.albumcamerarecorder.camera.listener.OnCameraResultListener;
import com.zhongjh.albumcamerarecorder.camera.manager.listener.CameraCloseListener;
import com.zhongjh.albumcamerarecorder.camera.manager.listener.CameraOpenListener;
import com.zhongjh.albumcamerarecorder.camera.manager.listener.CameraPictureListener;
import com.zhongjh.albumcamerarecorder.camera.manager.listener.CameraVideoListener;
import com.zhongjh.albumcamerarecorder.camera.util.CameraUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
        mBackgroundHandler.post(() -> {
            try {
                camera = Camera.open(cameraId);
                prepareCameraOutputs();
                if (futurFlashMode != null) {
                    setFlashMode(futurFlashMode);
                    futurFlashMode = null;
                }
                if (cameraOpenListener != null) {
                    // 重新启动浏览
                    mUiiHandler.post(() -> cameraOpenListener.onCameraOpened(cameraId, mPreviewSize, new SurfaceHolder.Callback() {
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
                    }));
                }
            } catch (Exception error) {
                Log.d(TAG, "Can't open camera: " + error.getMessage());
                if (cameraOpenListener != null) {
                    mUiiHandler.post(cameraOpenListener::onCameraOpenError);
                }
            }
        });


    }

    @Override
    public void closeCamera(CameraCloseListener<Integer> cameraCloseListener) {
        mBackgroundHandler.post(() -> {
            if (camera != null) {
                camera.release();
                camera = null;
                if (cameraCloseListener != null) {
                    mUiiHandler.post(() -> cameraCloseListener.onCameraClosed(mCameraId));
                }
            }
        });
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
    public void takePicture(File photoFile, CameraPictureListener cameraPictureListener, OnCameraResultListener callback) {
        this.outputPath = photoFile;
        this.photoListener = cameraPictureListener;
        mBackgroundHandler.post(() -> {
            setCameraPhotoQuality(camera);
            camera.takePicture(null, null, (bytes, camera) -> Camera1Manager.this.onPictureTaken(bytes, camera, callback));
        });
    }

    @Override
    public void startVideoRecord(File videoFile, CameraVideoListener cameraVideoListener) {
        if (mIsVideoRecording) return;

        this.outputPath = videoFile;
        this.videoListener = cameraVideoListener;

        if (videoListener != null)
            mBackgroundHandler.post(() -> {
                if (mContext == null) return;

                if (prepareVideoRecorder()) {
                    mMediaRecorder.start();
                    mIsVideoRecording = true;
                    mUiiHandler.post(() -> videoListener.onVideoRecordStarted(mVideoSize));
                }
            });
    }

    @Override
    public void stopVideoRecord(OnCameraResultListener callback) {
        if (mIsVideoRecording)
            mBackgroundHandler.post(() -> {

                try {
                    if (mMediaRecorder != null) mMediaRecorder.stop();
                } catch (Exception ignore) {
                }

                mIsVideoRecording = false;
                releaseVideoRecorder();

                if (videoListener != null) {
                    mUiiHandler.post(() -> videoListener.onVideoRecordStopped(outputPath, callback));
                }
            });
    }

    @Override
    public void initializeCameraManager(CameraConfigProvider cameraConfigProvider, Context context) {
        super.initializeCameraManager(cameraConfigProvider, context);

        mNumberOfCameras = Camera.getNumberOfCameras();

        for (int i = 0; i < mNumberOfCameras; ++i) {
            final Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mFaceBackCameraId = i;
                mFaceBackCameraOrientation = cameraInfo.orientation;
            } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mFaceFrontCameraId = i;
                mFaceFrontCameraOrientation = cameraInfo.orientation;
            }
        }
    }

    @Override
    public Size getPictureSizeForQuality(int mediaQuality) {
        return CameraUtils.getPictureSize(Size.fromList(camera.getParameters().getSupportedPictureSizes()), mediaQuality);
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
        mMediaRecorder = new MediaRecorder();
        try {
            camera.lock();
            camera.unlock();
            mMediaRecorder.setCamera(camera);

            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

            mMediaRecorder.setOutputFormat(mCamcorderProfile.fileFormat);
            mMediaRecorder.setVideoFrameRate(mCamcorderProfile.videoFrameRate);
            mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
            mMediaRecorder.setVideoEncodingBitRate(mCamcorderProfile.videoBitRate);
            mMediaRecorder.setVideoEncoder(mCamcorderProfile.videoCodec);

            mMediaRecorder.setAudioEncodingBitRate(mCamcorderProfile.audioBitRate);
            mMediaRecorder.setAudioChannels(mCamcorderProfile.audioChannels);
            mMediaRecorder.setAudioSamplingRate(mCamcorderProfile.audioSampleRate);
            mMediaRecorder.setAudioEncoder(mCamcorderProfile.audioCodec);

            mMediaRecorder.setOutputFile(outputPath.toString());

            if (cameraConfigProvider.getVideoFileSize() > 0) {
                mMediaRecorder.setMaxFileSize(cameraConfigProvider.getVideoFileSize());

                mMediaRecorder.setOnInfoListener(this);
            }
            if (cameraConfigProvider.getVideoDuration() > 0) {
                mMediaRecorder.setMaxDuration(cameraConfigProvider.getVideoDuration());

                mMediaRecorder.setOnInfoListener(this);
            }

            mMediaRecorder.setOrientationHint(getVideoOrientation(cameraConfigProvider.getSensorPosition()));
            mMediaRecorder.setPreviewDisplay(surface);

            mMediaRecorder.prepare();

            return true;
        } catch (IllegalStateException error) {
            Log.e(TAG, "IllegalStateException preparing MediaRecorder: " + error.getMessage());
        } catch (IOException error) {
            Log.e(TAG, "IOException preparing MediaRecorder: " + error.getMessage());
        } catch (Throwable error) {
            Log.e(TAG, "Error during preparing MediaRecorder: " + error.getMessage());
        }

        releaseVideoRecorder();
        return false;
    }

    @Override
    protected void onMaxDurationReached() {
        stopVideoRecord(null);
    }

    @Override
    protected void onMaxFileSizeReached() {
        stopVideoRecord(null);
    }

    @Override
    protected int getPhotoOrientation(int sensorPosition) {
        final int rotate;
        if (mCameraId.equals(mFaceFrontCameraId)) {
            rotate = (360 + mFaceFrontCameraOrientation + cameraConfigProvider.getDegrees()) % 360;
        } else {
            rotate = (360 + mFaceBackCameraOrientation - cameraConfigProvider.getDegrees()) % 360;
        }

        if (rotate == 0) {
            orientation = ExifInterface.ORIENTATION_NORMAL;
        } else if (rotate == 90) {
            orientation = ExifInterface.ORIENTATION_ROTATE_90;
        } else if (rotate == 180) {
            orientation = ExifInterface.ORIENTATION_ROTATE_180;
        } else if (rotate == 270) {
            orientation = ExifInterface.ORIENTATION_ROTATE_270;
        }

        return orientation;
    }

    @Override
    protected int getVideoOrientation(int sensorPosition) {
        int degrees = 0;
        switch (sensorPosition) {
            case CameraConfig.SENSOR_POSITION_UP:
                degrees = 0;
                break; // Natural orientation
            case CameraConfig.SENSOR_POSITION_LEFT:
                degrees = 90;
                break; // Landscape left
            case CameraConfig.SENSOR_POSITION_UP_SIDE_DOWN:
                degrees = 180;
                break;// Upside down
            case CameraConfig.SENSOR_POSITION_RIGHT:
                degrees = 270;
                break;// Landscape right
        }

        final int rotate;
        if (mCameraId.equals(mFaceFrontCameraId)) {
            rotate = (360 + mFaceFrontCameraOrientation + degrees) % 360;
        } else {
            rotate = (360 + mFaceBackCameraOrientation - degrees) % 360;
        }
        return rotate;
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

            // 设置呈现方式
            final int rotation = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0:
                    degrees = 0;
                    break; // Natural orientation
                case Surface.ROTATION_90:
                    degrees = 90;
                    break; // Landscape left
                case Surface.ROTATION_180:
                    degrees = 180;
                    break;// Upside down
                case Surface.ROTATION_270:
                    degrees = 270;
                    break;// Landscape right
            }

            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                displayRotation = (cameraRotationOffset + degrees) % 360;
                displayRotation = (360 - displayRotation) % 360; // compensate
            } else {
                displayRotation = (cameraRotationOffset - degrees + 360) % 360;
            }

            this.camera.setDisplayOrientation(displayRotation);

            if (Build.VERSION.SDK_INT > 14
                    && parameters.isVideoStabilizationSupported()
                    && (cameraConfigProvider.getMediaAction() == CameraConfig.MEDIA_ACTION_VIDEO
                    || cameraConfigProvider.getMediaAction() == CameraConfig.MEDIA_ACTION_UNSPECIFIED)) {
                parameters.setVideoStabilization(true);
            }

            parameters.setPreviewSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            parameters.setPictureSize(mPhotoSize.getWidth(), mPhotoSize.getHeight());

            camera.setParameters(parameters);
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        }catch (IOException error) {
            Log.d(TAG, "Error setting camera preview: " + error.getMessage());
        } catch (Exception ignore) {
            Log.d(TAG, "Error starting camera preview: " + ignore.getMessage());
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

    /**
     * 设置闪光灯模式
     * @param camera 相机
     * @param parameters 属性
     * @param flashMode 模式
     */
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

    /**
     * 设置图片质量
     * @param camera 相机
     */
    private void setCameraPhotoQuality(Camera camera) {
        final Camera.Parameters parameters = camera.getParameters();

        parameters.setPictureFormat(PixelFormat.JPEG);

        if (cameraConfigProvider.getMediaQuality() == CameraConfig.MEDIA_QUALITY_LOW) {
            parameters.setJpegQuality(50);
        } else if (cameraConfigProvider.getMediaQuality() == CameraConfig.MEDIA_QUALITY_MEDIUM) {
            parameters.setJpegQuality(75);
        } else if (cameraConfigProvider.getMediaQuality() == CameraConfig.MEDIA_QUALITY_HIGH) {
            parameters.setJpegQuality(100);
        } else if (cameraConfigProvider.getMediaQuality() == CameraConfig.MEDIA_QUALITY_HIGHEST) {
            parameters.setJpegQuality(100);
        }
        parameters.setPictureSize(mPhotoSize.getWidth(), mPhotoSize.getHeight());

        camera.setParameters(parameters);
    }

    /**
     * 拍照
     * @param bytes 拍摄后的数据源
     * @param camera
     * @param callback
     */
    protected void onPictureTaken(final byte[] bytes, Camera camera, final OnCameraResultListener callback) {
        final File pictureFile = outputPath;
        if (pictureFile == null) {
            Log.d(TAG, "Error creating media file, check storage permissions.");
            return;
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(pictureFile);
            fileOutputStream.write(bytes);
            fileOutputStream.close();
        } catch (FileNotFoundException error) {
            Log.e(TAG, "File not found: " + error.getMessage());
        } catch (IOException error) {
            Log.e(TAG, "Error accessing file: " + error.getMessage());
        } catch (Throwable error) {
            Log.e(TAG, "Error saving file: " + error.getMessage());
        }

        try {
            final ExifInterface exif = new ExifInterface(pictureFile.getAbsolutePath());
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, "" + getPhotoOrientation(cameraConfigProvider.getSensorPosition()));
            exif.saveAttributes();

            if (photoListener != null) {
                mUiiHandler.post(() -> photoListener.onPictureTaken(bytes, outputPath, callback));
            }
            camera.startPreview();
        } catch (Throwable error) {
            Log.e(TAG, "Can't save exif info: " + error.getMessage());
        }
    }

}
