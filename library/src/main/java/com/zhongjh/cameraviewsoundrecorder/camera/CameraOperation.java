package com.zhongjh.cameraviewsoundrecorder.camera;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.widget.ImageView;

import com.zhongjh.cameraviewsoundrecorder.camera.common.Constants;
import com.zhongjh.cameraviewsoundrecorder.camera.listener.ErrorListener;
import com.zhongjh.cameraviewsoundrecorder.camera.util.AngleUtil;
import com.zhongjh.cameraviewsoundrecorder.camera.util.CameraParamUtil;
import com.zhongjh.cameraviewsoundrecorder.camera.util.DeviceUtil;
import com.zhongjh.cameraviewsoundrecorder.camera.util.DisplayMetricsSPUtils;
import com.zhongjh.cameraviewsoundrecorder.camera.util.FileUtil;
import com.zhongjh.cameraviewsoundrecorder.camera.util.LogUtil;
import com.zhongjh.cameraviewsoundrecorder.camera.util.PermissionUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.graphics.Bitmap.createBitmap;
import static com.zhongjh.cameraviewsoundrecorder.camera.common.Constants.TYPE_CAPTURE;
import static com.zhongjh.cameraviewsoundrecorder.camera.common.Constants.TYPE_RECORDER;


/**
 * 关于照相机Camera的操作类
 * Created by zhongjh on 2018/8/10.
 */
public class CameraOperation implements Camera.PreviewCallback {

    private static final String TAG = "CameraOperation";

    private Camera mCamera;
    private Camera.Parameters mParams; // 相机的属性
    private boolean mIsPreviewing = false; // 目前是否处于录像状态
    private int mPreviewWidth; // 录像的宽度
    private int mPreviewHeight; // 录像的高度

    private int mSelectedCamera = -1;           // 当前摄像头是前置还是后置
    private int CAMERA_POST_POSITION;       // 摄像头后置,findAvailableCameras方法会赋值
    private int CAMERA_FRONT_POSITION;     // 摄像头前置,findAvailableCameras方法会赋值

    private SurfaceHolder mSurfaceHolder = null; // 显示一个surface的抽象接口，使你能够控制surface的大小和格式， 以及在surface上编辑像素，和监视surace的改变。
    private float mScreenProp = -1.0f; // 当前手机屏幕高宽比例，后面让摄像预览界面等也跟这个一样

    private boolean mIsRecorder = false;    // 录像中
    private MediaRecorder mMediaRecorder;   // 记录音频与视频
    private String mVideoFileName;           // 文件保存的file名称
//    private String mSaveVideoPath;          // 保存文件的路径
    private String mVideoFileAbsPath;       // 统一上面两个String的路径
    private Bitmap mVideoFirstFrame = null; // 录像的第一祯bitmap

    private int mNowScaleRate = 0;
    private int mRecordScleRate = 0;


    private int mMediaQuality = Constants.MEDIA_QUALITY_MIDDLE;  //视频质量
    private SensorManager mSensorManager = null;

    private ErrorListener mErrorLisenter; // 异常事件

    private ImageView mImgSwitch;
    private ImageView mImgFlash;

    private int mPhoneAngle = 0;        // 手机的角度，通过方向传感器获的
    private int mCameraAngle = 90;      //摄像头角度   默认为后置摄像头90度 前置摄像头180度 270度是
    private int mPictureAngle;          // 拍照后给予照片的角度，通过当前手机角度、摄像头角度计算
    private int mImageViewRotation = 0; // 用于判断当前图片的旋转角度跟mPhoneAngle是否一样，如果不一样就做相应操作

    private byte[] mPreviewFrameData; // 照相机返回的数据源

    private int mHandlerFocusTime;// 处理焦点

    public CameraOperation() {
        findAvailableCameras();
        mSelectedCamera = CAMERA_POST_POSITION; // 默认前摄像头
        mSaveVideoPath = "";
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        mPreviewFrameData = data;
    }

    /**
     * 获取前置和后置摄像头的值
     */
    private void findAvailableCameras() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        int cameraNum = Camera.getNumberOfCameras();
        for (int i = 0; i < cameraNum; i++) {
            Camera.getCameraInfo(i, info);
            switch (info.facing) {
                case Camera.CameraInfo.CAMERA_FACING_FRONT:
                    CAMERA_FRONT_POSITION = info.facing;
                    break;
                case Camera.CameraInfo.CAMERA_FACING_BACK:
                    CAMERA_POST_POSITION = info.facing;
                    break;
            }
        }
    }

    /**
     * 启动摄像头
     *
     * @param id 前置或者后置的
     */
    private synchronized void openCamera(int id) {
        try {
            // 打开摄像头
            this.mCamera = Camera.open(id);
        } catch (Exception var3) {
            var3.printStackTrace();
            if (this.mErrorLisenter != null) {
                this.mErrorLisenter.onError();
            }
        }

        // 添加一个开关来控制拍照声音
        if (Build.VERSION.SDK_INT > 17 && this.mCamera != null) {
            try {
                // 关闭声音
                this.mCamera.enableShutterSound(false);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("CJT", "enable shutter sound faild");
            }
        }
    }

    /**
     * 停止录像
     */
    private void doStopPreview() {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            try {
                // 这句要在stopPreview后执行，不然会卡顿或者花屏
                mCamera.setPreviewDisplay(null);
                mIsPreviewing = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 方向传感器的事件
     */
    private SensorEventListener sensorEventListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (Sensor.TYPE_ACCELEROMETER != event.sensor.getType()) {
                return;
            }
            float[] values = event.values;
            mPhoneAngle = AngleUtil.getSensorAngle(values[0], values[1]);
            rotationAnimation();
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    /**
     * 切换摄像头icon跟随手机角度进行旋转
     */
    private void rotationAnimation() {
        if (mImgSwitch == null) {
            return;
        }
        // 如果
        if (mImageViewRotation != mPhoneAngle) {
            // 确认从哪个角度旋转到哪个角度
            int startRotaion = 0;
            int endRotaion = 0;
            switch (mImageViewRotation) {
                case 0:
                    startRotaion = 0;
                    switch (mPhoneAngle) {
                        case 90:
                            endRotaion = -90;
                            break;
                        case 270:
                            endRotaion = 90;
                            break;
                    }
                    break;
                case 90:
                    startRotaion = -90;
                    switch (mPhoneAngle) {
                        case 0:
                            endRotaion = 0;
                            break;
                        case 180:
                            endRotaion = -180;
                            break;
                    }
                    break;
                case 180:
                    startRotaion = 180;
                    switch (mPhoneAngle) {
                        case 90:
                            endRotaion = 270;
                            break;
                        case 270:
                            endRotaion = 90;
                            break;
                    }
                    break;
                case 270:
                    startRotaion = 90;
                    switch (mPhoneAngle) {
                        case 0:
                            endRotaion = 0;
                            break;
                        case 180:
                            endRotaion = 180;
                            break;
                    }
                    break;
            }
            // 一起旋转
            ObjectAnimator animSwitch = ObjectAnimator.ofFloat(mImgSwitch, "rotation", startRotaion, endRotaion);
            ObjectAnimator animFlash = ObjectAnimator.ofFloat(mImgFlash, "rotation", startRotaion, endRotaion);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(animSwitch, animFlash);
            set.setDuration(500);
            set.start();
            mImageViewRotation = mPhoneAngle;
        }
    }

    private static Rect calculateTapArea(float x, float y, float coefficient, Context context) {
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        int centerX = (int) (x / DisplayMetricsSPUtils.getScreenWidth(context) * 2000 - 1000);
        int centerY = (int) (y / DisplayMetricsSPUtils.getScreenHeight(context) * 2000 - 1000);
        int left = clamp(centerX - areaSize / 2, -1000, 1000);
        int top = clamp(centerY - areaSize / 2, -1000, 1000);
        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF
                .bottom));
    }

    private static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    // region 对外开放的API

    /**
     * 摄像头启动录像
     *
     * @param surfaceHolder 显示一个surface的抽象接口，使你能够控制surface的大小和格式， 以及在surface上编辑像素，和监视surace的改变。
     * @param screenProp    当前手机屏幕高宽比例
     */
    public void doStartPreview(SurfaceHolder surfaceHolder, float screenProp) {
        if (mIsPreviewing)
            LogUtil.i("doStartPreview mIsPreviewing");

        if (this.mScreenProp < 0)
            this.mScreenProp = screenProp;

        if (surfaceHolder == null)
            return;

        this.mSurfaceHolder = surfaceHolder;

        if (mCamera != null) {
            mParams = mCamera.getParameters();
            // 这是预览时帧数据的尺寸
            Camera.Size previewSize = CameraParamUtil.getInstance().getPreviewSize(mParams
                    .getSupportedPreviewSizes(), 1000, screenProp);

            // 这是拍照后的PictureSize尺寸
            Camera.Size pictureSize = CameraParamUtil.getInstance().getPictureSize(mParams
                    .getSupportedPictureSizes(), 1200, screenProp);

            // 设置预览的宽度和高度
            mParams.setPreviewSize(previewSize.width, previewSize.height);

            mPreviewHeight = previewSize.height;
            mPreviewWidth = previewSize.width;

            // 设置图片的宽度和高度
            mParams.setPictureSize(pictureSize.width, pictureSize.height);

            // 判断该相机有没有自动对焦模式
            if (CameraParamUtil.getInstance().isSupportedFocusMode(mParams.getSupportedFocusModes(), Camera.Parameters.FOCUS_MODE_AUTO)) {
                // 如果有，就设置自动对焦
                mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }

            if (CameraParamUtil.getInstance().isSupportedPictureFormats(mParams.getSupportedPictureFormats(),
                    ImageFormat.JPEG)) {
                // 设置照片的输出格式
                mParams.setPictureFormat(ImageFormat.JPEG);
                // 照片质量
                mParams.setJpegQuality(100);
            }

            mCamera.setParameters(mParams);
            //SurfaceView
            try {
                mCamera.setPreviewDisplay(surfaceHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.setDisplayOrientation(mCameraAngle);//浏览角度
            mCamera.setPreviewCallback(this); //每一帧回调
            mCamera.startPreview();//启动录像
            mIsPreviewing = true; // 目前录像状态中
            Log.i(TAG, "=== Start Preview ===");
        }

    }

    /**
     * 切换摄像头
     *
     * @param surfaceHolder surfaceHolder 显示一个surface的抽象接口，使你能够控制surface的大小和格式， 以及在surface上编辑像素，和监视surace的改变。
     * @param screenProp    @@ 补充注释
     */
    public synchronized void switchCamera(SurfaceHolder surfaceHolder, float screenProp) {
        if (mSelectedCamera == CAMERA_POST_POSITION) {
            mSelectedCamera = CAMERA_FRONT_POSITION;
        } else {
            mSelectedCamera = CAMERA_POST_POSITION;
        }
        doDestroyCamera();
        openCamera(mSelectedCamera);
        doStartPreview(surfaceHolder, screenProp);
    }

    /**
     * 拍照
     *
     * @param callback 拍照后的回调
     */
    public void takePicture(final CameraCallback.TakePictureCallback callback) {
        if (mCamera == null) {
            return;
        }
        // 无论怎么旋转手机是正常的角度
        switch (mCameraAngle) {
            case 90:
                mPictureAngle = Math.abs(mPhoneAngle + mCameraAngle) % 360;
                break;
            case 270:
                mPictureAngle = Math.abs(mCameraAngle - mPhoneAngle);
                break;
        }

        Log.i("CJT", mPhoneAngle + " = " + mCameraAngle + " = " + mPictureAngle);
        // 相机调用拍照
        mCamera.takePicture(null, null, (data, camera) -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Matrix matrix = new Matrix();
            // 判断是前置还是后置
            if (mSelectedCamera == CAMERA_POST_POSITION) {
                matrix.setRotate(mPictureAngle);
            } else if (mSelectedCamera == CAMERA_FRONT_POSITION) {
                matrix.setRotate(360 - mPictureAngle);
                matrix.postScale(-1, 1);
            }

            bitmap = createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (callback != null) {
                if (mPictureAngle == 90 || mPictureAngle == 270) {
                    callback.captureResult(bitmap, true);
                } else {
                    callback.captureResult(bitmap, false);
                }
            }
        });
    }

    /**
     * 开始录像
     *
     * @param surface    用来画图的地方
     * @param screenProp 高/宽 比例
     */
    public void startRecord(Surface surface, float screenProp) {
        mCamera.setPreviewCallback(null);
        final int nowAngle = (mPhoneAngle + 90) % 360;
        // 获取第一帧图片
        Camera.Parameters parameters = mCamera.getParameters();
        int width = parameters.getPreviewSize().width;
        int height = parameters.getPreviewSize().height;
        YuvImage yuv = new YuvImage(mPreviewFrameData, parameters.getPreviewFormat(), width, height, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);
        byte[] bytes = out.toByteArray();
        mVideoFirstFrame = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Matrix matrix = new Matrix();
        if (mSelectedCamera == CAMERA_POST_POSITION) {
            matrix.setRotate(nowAngle);
        } else if (mSelectedCamera == CAMERA_FRONT_POSITION) {
            matrix.setRotate(270);
        }
        mVideoFirstFrame = createBitmap(mVideoFirstFrame, 0, 0, mVideoFirstFrame.getWidth(), mVideoFirstFrame
                .getHeight(), matrix, true);

        // 录像中则直接返回
        if (mIsRecorder)
            return;

        // 打开录像
        if (mCamera == null)
            openCamera(mSelectedCamera);

        // 实例化音频
        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();

        if (mParams == null) {
            mParams = mCamera.getParameters();
        }

        List<String> focusModes = mParams.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        mCamera.setParameters(mParams);
        mCamera.unlock();

        mMediaRecorder.reset();// 重置
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // 这将指定录制的文件为mpeg-4格式
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        Camera.Size videoSize;
        if (mParams.getSupportedVideoSizes() == null) {
            videoSize = CameraParamUtil.getInstance().getPreviewSize(mParams.getSupportedPreviewSizes(), 600,
                    screenProp);
        } else {
            videoSize = CameraParamUtil.getInstance().getPreviewSize(mParams.getSupportedVideoSizes(), 600,
                    screenProp);
        }
        Log.i(TAG, "setVideoSize    width = " + videoSize.width + "height = " + videoSize.height);

        if (videoSize.width == videoSize.height) {
            mMediaRecorder.setVideoSize(mPreviewWidth, mPreviewHeight);
        } else {
            mMediaRecorder.setVideoSize(videoSize.width, videoSize.height);
        }

        if (mSelectedCamera == CAMERA_FRONT_POSITION) {
            //手机预览倒立的处理
            if (mCameraAngle == 270) {
                //横屏
                if (nowAngle == 0) {
                    mMediaRecorder.setOrientationHint(180);
                } else if (nowAngle == 270) {
                    mMediaRecorder.setOrientationHint(270);
                } else {
                    mMediaRecorder.setOrientationHint(90);
                }
            } else {
                if (nowAngle == 90) {
                    mMediaRecorder.setOrientationHint(270);
                } else if (nowAngle == 270) {
                    mMediaRecorder.setOrientationHint(90);
                } else {
                    mMediaRecorder.setOrientationHint(nowAngle);
                }
            }
        } else {
            mMediaRecorder.setOrientationHint(nowAngle);
        }

        if (DeviceUtil.isHuaWeiRongyao()) {
            // 如果是华为荣耀，使用该质量
            mMediaRecorder.setVideoEncodingBitRate(Constants.MEDIA_QUALITY_FUNNY);
        } else {
            mMediaRecorder.setVideoEncodingBitRate(mMediaQuality);
        }
        mMediaRecorder.setPreviewDisplay(surface);

        mVideoFileName = "video_" + System.currentTimeMillis() + ".mp4";
        if (mSaveVideoPath.equals("")) {
            mSaveVideoPath = Environment.getExternalStorageDirectory().getPath();
        }
        mVideoFileAbsPath = mSaveVideoPath + File.separator + mVideoFileName;
        // 输出最终路径
        mMediaRecorder.setOutputFile(mVideoFileAbsPath);
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            mIsRecorder = true;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Log.i("CJT", "startRecord IllegalStateException");
            if (this.mErrorLisenter != null) {
                this.mErrorLisenter.onError();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("CJT", "startRecord IOException");
            if (this.mErrorLisenter != null) {
                this.mErrorLisenter.onError();
            }
        } catch (RuntimeException e) {
            Log.i("CJT", "startRecord RuntimeException");
        }
    }

    /**
     * 停止录像
     *
     * @param isShort  是否因为视频过短而停止
     * @param callback 回调事件
     */
    public void stopRecord(boolean isShort, CameraCallback.StopRecordCallback callback) {
        // 不是正在录像就返回
        if (!mIsRecorder)
            return;

        if (mMediaRecorder != null) {
            // 开始进行重置
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.setOnInfoListener(null);
            mMediaRecorder.setPreviewDisplay(null);
            try {
                mMediaRecorder.stop();
            } catch (RuntimeException e) {
                e.printStackTrace();
                mMediaRecorder = null;
                mMediaRecorder = new MediaRecorder();
            } finally {
                // 清空
                if (mMediaRecorder != null) {
                    mMediaRecorder.release();
                }
                mMediaRecorder = null;
                mIsRecorder = false;
            }

            if (isShort) {
                // 如果是短视频则删除文件，并且直接回调返回
                if (FileUtil.deleteFile(mVideoFileAbsPath)) {
                    // 回调
                    callback.recordResult(null, null);
                }
            } else {
                // 停止预览并且回调
                doStopPreview();
                String fileName = mSaveVideoPath + File.separator + mVideoFileName;
                callback.recordResult(fileName, mVideoFirstFrame);
            }


        }
    }

    /**
     * 销毁Camera
     */
    public void doDestroyCamera() {
        mErrorLisenter = null;
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mImgSwitch = null;
            mImgFlash = null;
            mCamera.stopPreview();
            //这句要在stopPreview后执行，不然会卡顿或者花屏
            try {
                mCamera.setPreviewDisplay(null);
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "=== Camera  Null===");
            }
            mSurfaceHolder = null;
            mIsPreviewing = false;
            mCamera.release();
            mCamera = null;
            Log.i(TAG, "=== Destroy Camera ===");
        }
    }

    /**
     * 缩放
     *
     * @param zoom 缩放数值
     * @param type 拍照或者录制模式
     */
    public void zoom(float zoom, int type) {
        if (mCamera == null) {
            return;
        }
        if (mParams == null) {
            mParams = mCamera.getParameters();
        }
        //isZoomSupported()是判断设备是否支持缩放，isSmoothZoomSupported()是判断是否支持平滑缩放，
        // android的部分机型这两个返回一个是：isZoomSupported()放回true，isSmoothZoomSupported()返回false，
        // 也就是说支持缩放，但是不支持平滑缩放，于是我把setZoom() 方法改成了：
//        if (!mParams.isZoomSupported() || !mParams.isSmoothZoomSupported()) {
//            return;
//        }
        if (!mParams.isZoomSupported()) {
            return;
        }
        switch (type) {
            case TYPE_RECORDER:
                // 如果不是录制视频中，上滑不会缩放
                if (!mIsRecorder) {
                    return;
                }
                if (zoom >= 0) {
                    // 每移动50个像素缩放一个级别
                    int scaleRate = (int) (zoom / 40);
                    if (scaleRate <= mParams.getMaxZoom() && scaleRate >= mNowScaleRate && mRecordScleRate != scaleRate) {
                        mParams.setZoom(scaleRate);
                        mCamera.setParameters(mParams);
                        mRecordScleRate = scaleRate;
                    }
                }
                break;
            case TYPE_CAPTURE:
                if (mIsRecorder) {
                    return;
                }
                // 每移动50个像素缩放一个级别
                int scaleRate = (int) (zoom / 50);
                if (scaleRate < mParams.getMaxZoom()) {
                    mNowScaleRate += scaleRate;
                    if (mNowScaleRate < 0) {
                        mNowScaleRate = 0;
                    } else if (mNowScaleRate > mParams.getMaxZoom()) {
                        mNowScaleRate = mParams.getMaxZoom();
                    }
                    mParams.setZoom(mNowScaleRate);
                    mCamera.setParameters(mParams);
                }
                LogUtil.i("setZoom = " + mNowScaleRate);
                break;
        }

    }

    /**
     * 设置摄像切换 和 闪关灯 控件
     *
     * @param imgSwitch 摄像切换控件
     * @param imgFlash  闪光灯控件
     */
    public void setImageViewSwitchAndFlash(ImageView imgSwitch, ImageView imgFlash) {
        this.mImgSwitch = imgSwitch;
        this.mImgFlash = imgFlash;
        if (mImgSwitch != null) {
            mCameraAngle = CameraParamUtil.getInstance().getCameraDisplayOrientation(mImgSwitch.getContext(),
                    mSelectedCamera);
        }
    }

    /**
     * 赋值异常事件
     */
    public void setErrorLinsenter(ErrorListener errorLisenter) {
        this.mErrorLisenter = errorLisenter;
    }

    /**
     * 打开camera
     *
     * @param cameraOpenOverCallback 回调事件
     */
    public void doOpenCamera(CameraCallback.CameraOpenOverCallback cameraOpenOverCallback) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (!PermissionUtil.isCameraUseable(mSelectedCamera) && this.mErrorLisenter != null) {
                this.mErrorLisenter.onError();
                return;
            }
        }
        if (mCamera == null) {
            openCamera(mSelectedCamera);
        }
        cameraOpenOverCallback.cameraHasOpened();

    }

    /**
     * 注册方向传感器
     *
     * @param context 上下文
     */
    public void registerSensorManager(Context context) {
        if (mSensorManager == null) {
            mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        }
        assert mSensorManager != null;
        mSensorManager.registerListener(sensorEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager
                .SENSOR_DELAY_NORMAL);
    }

    /**
     * 注销方向传感器
     *
     * @param context 上下文
     */
    public void unregisterSensorManager(Context context) {
        if (mSensorManager == null) {
            mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        }
        assert mSensorManager != null;
        mSensorManager.unregisterListener(sensorEventListener);
    }

    /**
     * 设置录像状态
     *
     * @param b 是否录像
     */
    public void isPreview(boolean b) {
        this.mIsPreviewing = b;
    }

    /**
     * 处理焦点，焦点所处变得清晰
     *
     * @param context  上下文
     * @param x        x坐标
     * @param y        y坐标
     * @param callback 焦点回调
     */
    public void handleFocus(final Context context, final float x, final float y, final CameraCallback.FocusCallback callback) {
        if (mCamera == null) {
            return;
        }
        final Camera.Parameters params = mCamera.getParameters();
        Rect focusRect = calculateTapArea(x, y, 1f, context);
        mCamera.cancelAutoFocus();
        if (params.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<>();
            focusAreas.add(new Camera.Area(focusRect, 800));
            params.setFocusAreas(focusAreas);
        } else {
            Log.i(TAG, "focus areas not supported");
            callback.focusSuccess();
            return;
        }
        final String currentFocusMode = params.getFocusMode();
        try {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            mCamera.setParameters(params);
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success || mHandlerFocusTime > 10) {
                        Camera.Parameters params = camera.getParameters();
                        params.setFocusMode(currentFocusMode);
                        camera.setParameters(params);
                        mHandlerFocusTime = 0;
                        callback.focusSuccess();
                    } else {
                        mHandlerFocusTime++;
                        handleFocus(context, x, y, callback);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "autoFocus failer");
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void setSaveVideoPath(String saveVideoPath) {
        this.mSaveVideoPath = saveVideoPath;
        File file = new File(saveVideoPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 设置视频比特率
     *
     * @param mediaQualityMiddle 比特率
     */
    public void setMediaQuality(int mediaQualityMiddle) {
        this.mMediaQuality = mediaQualityMiddle;
    }

    /**
     * 拍照是否允许拍多几张，只拍一张
     * @param i 数量
     */
    public void setPictureMaxNumber(int i) {
        this.PictureMaxNumber = i;
    }

    // endregion

}
