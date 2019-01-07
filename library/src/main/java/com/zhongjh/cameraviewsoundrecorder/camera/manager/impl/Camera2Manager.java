package com.zhongjh.cameraviewsoundrecorder.camera.manager.impl;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.TextureView;
import android.view.WindowManager;

import com.zhongjh.cameraviewsoundrecorder.camera.config.CameraConfig;
import com.zhongjh.cameraviewsoundrecorder.camera.config.CameraConfigProvider;
import com.zhongjh.cameraviewsoundrecorder.camera.entity.Size;
import com.zhongjh.cameraviewsoundrecorder.camera.listener.OnCameraResultListener;
import com.zhongjh.cameraviewsoundrecorder.camera.manager.listener.CameraCloseListener;
import com.zhongjh.cameraviewsoundrecorder.camera.manager.listener.CameraOpenListener;
import com.zhongjh.cameraviewsoundrecorder.camera.manager.listener.CameraPictureListener;
import com.zhongjh.cameraviewsoundrecorder.camera.manager.listener.CameraVideoListener;
import com.zhongjh.cameraviewsoundrecorder.camera.util.CameraUtils;
import com.zhongjh.cameraviewsoundrecorder.camera.util.ImageSaver;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.zhongjh.cameraviewsoundrecorder.camera.common.Constants.STATE_PREVIEW;

/**
 * Created by zhongjh on 2019/1/4.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class Camera2Manager extends BaseCameraManager<String, TextureView.SurfaceTextureListener>
        implements ImageReader.OnImageAvailableListener, TextureView.SurfaceTextureListener {

    private final static String TAG = "Camera2Manager";

    Context mContext;

    private static final int STATE_PREVIEW = 0;                     // 休闲状态
    private static final int STATE_WAITING_LOCK = 1;
    private static final int STATE_WAITING_PRE_CAPTURE = 2;
    private static final int STATE_WAITING_NON_PRE_CAPTURE = 3;
    private static final int STATE_PICTURE_TAKEN = 4;

    private CameraOpenListener<String, TextureView.SurfaceTextureListener> mCameraOpenListener;
    private CameraPictureListener mCameraPictureListener;
    private File mOutputPath;

    @CameraPreviewState
    private int mPreviewState = STATE_PREVIEW;

    private CameraManager mCameraManager;   // CameraManager是一个用于检测、连接和描述相机设备的系统服务,负责管理所有的CameraDevice相机设备
    private CameraDevice mCameraDevice;     // 代表系统摄像头。该类的功能类似于早期的Camera类。
    private CaptureRequest mPreviewRequest; // 需要构建CaptureRequest，它定义了用于拍摄的所有参数，包括聚焦、闪光灯、曝光率等等一切拍照可能需要的或者相机设备支持的参数
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CameraCaptureSession mCaptureSession;   // 这是一个非常重要的API，当程序需要预览、拍照时，都需要先通过该类的实例创建Session。而且不管预览还是拍照，也都是由该对象的方法进行控制的，其中控制预览的方法为setRepeatingRequest()；控制拍照的方法为capture()。
    private CameraCharacteristics mFrontCameraCharacteristics;      // 摄像头特性。该对象通过CameraManager来获取，用于描述特定摄像头所支持的各种特性
    private CameraCharacteristics mBackCameraCharacteristics;       // 摄像头特性。该对象通过CameraManager来获取，用于描述特定摄像头所支持的各种特性
    private StreamConfigurationMap mFrontCameraStreamConfigurationMap;  // 摄像头正面参数
    private StreamConfigurationMap mBackCameraStreamConfigurationMap;   // 摄像头背面参数

    private ImageReader mImageReader;
    private SurfaceTexture mSurfaceTexture;     // Camera 把视频采集的内容交给 SurfaceTexture， SurfaceTexture 在对内容做个美颜， 然后SurfaceTexture 再把内容交给 SurfaceView。这就是最后呈现给用户视觉上的美颜内容了

    private OnCameraResultListener mOnCameraResultListener;     // 拍照返回的Listener

    /**
     * 用于接收相机状态的更新和后续的处理
     */
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            Camera2Manager.this.mCameraDevice = cameraDevice;
            if (mCameraOpenListener != null) {
                mUiiHandler.post(() -> {
                    if (!TextUtils.isEmpty(mCameraId) && mPreviewSize != null)
                        mCameraOpenListener.onCameraOpened(mCameraId, mPreviewSize, Camera2Manager.this);
                });
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
            Camera2Manager.this.mCameraDevice = null;
            mUiiHandler.post(() -> mCameraOpenListener.onCameraOpenError());
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            cameraDevice.close();
            Camera2Manager.this.mCameraDevice = null;
            mUiiHandler.post(() -> mCameraOpenListener.onCameraOpenError());
        }
    };

    /**
     * 将处理预览和拍照图片的工作，需要重点对待
     */
    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
//            processCaptureResult(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
//            processCaptureResult(result);
        }

    };

    @Override
    public void initializeCameraManager(CameraConfigProvider cameraConfigProvider, Context context) {
        super.initializeCameraManager(cameraConfigProvider, context);
        final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        final Display display = windowManager.getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        mWindowSize = new Size(size.x, size.y);

        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            final String[] ids = mCameraManager.getCameraIdList();
            mNumberOfCameras = ids.length;
            for (String id : ids) {
                final CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(id);

                final int orientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (orientation == CameraCharacteristics.LENS_FACING_FRONT) {
                    mFaceFrontCameraId = id;
                    mFaceFrontCameraOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                    mFrontCameraCharacteristics = characteristics;
                } else {
                    mFaceBackCameraId = id;
                    mFaceBackCameraOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                    mBackCameraCharacteristics = characteristics;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during camera initialize");
        }
    }


    @Override
    public void openCamera(String cameraId, CameraOpenListener<String, TextureView.SurfaceTextureListener> cameraOpenListener) {
        this.mCameraId = cameraId;
        this.mCameraOpenListener = cameraOpenListener;
        mBackgroundHandler.post(() -> {
            if (mContext == null || cameraConfigProvider == null) {
                Log.e(TAG, "openCamera: ");
                if (cameraOpenListener != null) {
                    mUiiHandler.post(() -> cameraOpenListener.onCameraOpenError());
                }
                return;
            }
            prepareCameraOutputs();
            try {
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    if (cameraOpenListener != null) {
                        mUiiHandler.post(() -> cameraOpenListener.onCameraOpenError());
                    }
                    return;
                }
                mCameraManager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
            } catch (Exception e) {
                Log.e(TAG, "openCamera: ", e);
                if (cameraOpenListener != null) {
                    mUiiHandler.post(() -> cameraOpenListener.onCameraOpenError());
                }
            }
        });
    }

    @Override
    public void closeCamera(CameraCloseListener<String> cameraCloseListener) {
        mBackgroundHandler.post(() -> {
            closeCamera();
            if (cameraCloseListener != null) {
                mUiiHandler.post(() -> cameraCloseListener.onCameraClosed(mCameraId));
            }
        });
    }

    @Override
    public void setFlashMode(int flashMode) {
        setFlashModeAndBuildPreviewRequest(flashMode);
    }

    /**
     * imageReader处理器
     * @param imageReader 图片数据源
     */
    @Override
    public void onImageAvailable(ImageReader imageReader) {
        final File outputFile = mOutputPath;
        mBackgroundHandler.post(new ImageSaver(imageReader.acquireNextImage(), outputFile, new ImageSaver.ImageSaverCallback() {
            @Override
            public void onSuccessFinish(final byte[] bytes) {
                Log.d(TAG, "onPhotoSuccessFinish: ");
                if (mCameraPictureListener != null) {
                    mUiiHandler.post(() -> {
                        mCameraPictureListener.onPictureTaken(bytes, mOutputPath, mOnCameraResultListener);
                        mOnCameraResultListener = null;
                    });
                }
                unlockFocus();
            }

            @Override
            public void onError() {
                Log.d(TAG, "onPhotoError: ");
                mUiiHandler.post(() -> mCameraPictureListener.onPictureTakeError());
            }
        }));
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

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
            final CameraCharacteristics characteristics = mCameraId.equals(mFaceBackCameraId) ? mBackCameraCharacteristics : mFrontCameraCharacteristics;

            if (mCameraId.equals(mFaceFrontCameraId) && mFrontCameraStreamConfigurationMap == null)
                mFrontCameraStreamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            else if (mCameraId.equals(mFaceBackCameraId) && mBackCameraStreamConfigurationMap == null)
                mBackCameraStreamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            final StreamConfigurationMap map = mCameraId.equals(mFaceBackCameraId) ? mBackCameraStreamConfigurationMap : mFrontCameraStreamConfigurationMap;
            if (cameraConfigProvider.getMediaQuality() == CameraConfig.MEDIA_QUALITY_AUTO) {
                mCamcorderProfile = CameraUtils.getCamcorderProfile(mCameraId, cameraConfigProvider.getVideoFileSize(), cameraConfigProvider.getMinimumVideoDuration());
            } else
                mCamcorderProfile = CameraUtils.getCamcorderProfile(cameraConfigProvider.getMediaQuality(), mCameraId);

            mVideoSize = CameraUtils.chooseOptimalSize(Size.fromArray2(map.getOutputSizes(MediaRecorder.class)),
                    mWindowSize.getWidth(), mWindowSize.getHeight(), new Size(mCamcorderProfile.videoFrameWidth, mCamcorderProfile.videoFrameHeight));

            if (mVideoSize == null || mVideoSize.getWidth() > mCamcorderProfile.videoFrameWidth
                    || mVideoSize.getHeight() > mCamcorderProfile.videoFrameHeight)
                mVideoSize = CameraUtils.getSizeWithClosestRatio(Size.fromArray2(map.getOutputSizes(MediaRecorder.class)), mCamcorderProfile.videoFrameWidth, mCamcorderProfile.videoFrameHeight);
            else if (mVideoSize == null || mVideoSize.getWidth() > mCamcorderProfile.videoFrameWidth
                    || mVideoSize.getHeight() > mCamcorderProfile.videoFrameHeight)
                mVideoSize = CameraUtils.getSizeWithClosestRatio(Size.fromArray2(map.getOutputSizes(MediaRecorder.class)), mCamcorderProfile.videoFrameWidth, mCamcorderProfile.videoFrameHeight);

            mPhotoSize = CameraUtils.getPictureSize(Size.fromArray2(map.getOutputSizes(ImageFormat.JPEG)),
                    cameraConfigProvider.getMediaQuality() == CameraConfig.MEDIA_QUALITY_AUTO
                            ? CameraConfig.MEDIA_QUALITY_HIGHEST : cameraConfigProvider.getMediaQuality());

            mImageReader = ImageReader.newInstance(mPhotoSize.getWidth(), mPhotoSize.getHeight(),
                    ImageFormat.JPEG, 2);
            mImageReader.setOnImageAvailableListener(this, mBackgroundHandler);

            if (cameraConfigProvider.getMediaAction() == CameraConfig.MEDIA_ACTION_PHOTO
                    || cameraConfigProvider.getMediaAction() == CameraConfig.MEDIA_ACTION_UNSPECIFIED) {

                if (mWindowSize.getHeight() * mWindowSize.getWidth() > mPhotoSize.getWidth() * mPhotoSize.getHeight()) {
                    mPreviewSize = CameraUtils.getOptimalPreviewSize(Size.fromArray2(map.getOutputSizes(SurfaceTexture.class)), mPhotoSize.getWidth(), mPhotoSize.getHeight());
                } else {
                    mPreviewSize = CameraUtils.getOptimalPreviewSize(Size.fromArray2(map.getOutputSizes(SurfaceTexture.class)), mWindowSize.getWidth(), mWindowSize.getHeight());
                }

                if (mPreviewSize == null)
                    mPreviewSize = CameraUtils.chooseOptimalSize(Size.fromArray2(map.getOutputSizes(SurfaceTexture.class)), mWindowSize.getWidth(), mWindowSize.getHeight(), mPhotoSize);

            } else {
                if (mWindowSize.getHeight() * mWindowSize.getWidth() > mVideoSize.getWidth() * mVideoSize.getHeight()) {
                    mPreviewSize = CameraUtils.getOptimalPreviewSize(Size.fromArray2(map.getOutputSizes(SurfaceTexture.class)), mVideoSize.getWidth(), mVideoSize.getHeight());
                } else {
                    mPreviewSize = CameraUtils.getOptimalPreviewSize(Size.fromArray2(map.getOutputSizes(SurfaceTexture.class)), mWindowSize.getWidth(), mWindowSize.getHeight());
                }

                if (mPreviewSize == null)
                    mPreviewSize = CameraUtils.getSizeWithClosestRatio(Size.fromArray2(map.getOutputSizes(SurfaceTexture.class)), mVideoSize.getWidth(), mVideoSize.getHeight());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while setup camera sizes.", e);
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
     * 关闭相机
     */
    private void closeCamera() {
        closePreviewSession();
        releaseTexture();
        closeCameraDevice();
        closeImageReader();
        releaseVideoRecorder();
    }

    /**
     * 关闭预览
     */
    private void closePreviewSession() {
        if (mCaptureSession != null) {
            mCaptureSession.close();
            try {
                mCaptureSession.abortCaptures();
            } catch (Exception ignore) {
            } finally {
                mCaptureSession = null;
            }
        }
    }

    /**
     * 关闭GL外部纹理
     */
    private void releaseTexture() {
        if (null != mSurfaceTexture) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
    }

    /**
     * 关闭摄像头
     */
    private void closeCameraDevice() {
        if (null != mCameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    /**
     * 关闭图片数据源
     */
    private void closeImageReader() {
        if (null != mImageReader) {
            mImageReader.close();
            mImageReader = null;
        }
    }

    /**
     * 解锁焦点
     */
    private void unlockFocus() {
        try {
            // 关闭触发对焦
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            mCaptureSession.capture(mPreviewRequestBuilder.build(), captureCallback, mBackgroundHandler);
            mPreviewState = STATE_PREVIEW;
            // 重新打开预览
            mCaptureSession.setRepeatingRequest(mPreviewRequest, captureCallback, mBackgroundHandler);
        } catch (Exception e) {
            Log.e(TAG, "Error during focus unlocking");
        }
    }

    /**
     * 设置闪光灯模式
     * @param flashMode 闪光灯模式
     */
    private void setFlashModeAndBuildPreviewRequest(@CameraConfig.FlashMode int flashMode) {
        try {

            switch (flashMode) {
                case CameraConfig.FLASH_MODE_AUTO:
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                    mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_SINGLE);
                    break;
                case CameraConfig.FLASH_MODE_ON:
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                    mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_SINGLE);
                    break;
                case CameraConfig.FLASH_MODE_OFF:
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                    mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                    break;
                default:
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                    mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_SINGLE);
                    break;
            }

            mPreviewRequest = mPreviewRequestBuilder.build();

            try {
                mCaptureSession.setRepeatingRequest(mPreviewRequest, captureCallback, mBackgroundHandler);
            } catch (Exception e) {
                Log.e(TAG, "Error updating preview: ", e);
            }
        } catch (Exception ignore) {
            Log.e(TAG, "Error setting flash: ", ignore);
        }
    }

    @IntDef({STATE_PREVIEW, STATE_WAITING_LOCK, STATE_WAITING_PRE_CAPTURE, STATE_WAITING_NON_PRE_CAPTURE, STATE_PICTURE_TAKEN})
    @Retention(RetentionPolicy.SOURCE)
    @interface CameraPreviewState {
    }

}
