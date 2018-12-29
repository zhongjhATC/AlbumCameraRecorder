package com.zhongjh.cameraviewsoundrecorder.camera;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import com.zhongjh.cameraviewsoundrecorder.camera.util.Camera2Util;
import com.zhongjh.cameraviewsoundrecorder.camera.util.CompareSizeByArea;
import com.zhongjh.cameraviewsoundrecorder.settings.CameraSpec;
import com.zhongjh.cameraviewsoundrecorder.settings.MediaStoreCompat;
import com.zhongjh.cameraviewsoundrecorder.widget.clickorlongbutton.ClickOrLongButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

/**
 * 5.0版本以上推荐使用camera2
 * Created by zhongjh on 2018/12/29.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class CameraOperation2 {

    private static final String TAG = "CameraOperation2";
    private CameraSpec mCameraSpec;       // 拍摄配置
    private MediaStoreCompat mMediaStoreCompat; // 文件配置
    /**
     * Max preview width that is guaranteed by Camera2 API
     * Camera2 API保证的最大预览宽度
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;

    /**
     * Max preview height that is guaranteed by Camera2 API
     * Camera2 API保证的最大预览高度
     */
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    Activity mActivity;
    CameraLayout.ViewHolder mViewHolder;

    private boolean mIsRecordingVideo= false;//是否正在录制视频
    private boolean isStop = false;//是否停止过了MediaRecorder
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    //region 摄像头相关

    private CameraDevice mCameraDevice; // 是连接在安卓设备上的单个相机的抽象表示
    private CameraCharacteristics characteristics;  // 关于这个设备的一些特性参数，比如输出图像的大小，是否支持闪光灯等信息
    private ImageReader mImageReader;   // 拍照后返回的图片，包含各种数据，回调
    private String picSavePath;//图片保存路径
    private String videoSavePath;//视频保存路径
    // endregion

    private static final int CAPTURE_OK = 0;//拍照完成回调
    private int width;//TextureView的宽
    private int height;//TextureView的高
    private String mCameraId;//后置摄像头ID
    private String mCameraIdFront;//前置摄像头ID
    private boolean isCameraFront = false;//当前是否是前置摄像头

    private MediaRecorder mMediaRecorder;

    private Size mPreviewSize;//预览的Size
    private Size mCaptureSize;//拍照Size
    private Size mVideoSize;//视频size

    public CameraOperation2(Context context) {
        mCameraSpec = CameraSpec.getInstance();
        mMediaStoreCompat = new MediaStoreCompat(context);
        mMediaStoreCompat.setCaptureStrategy(mCameraSpec.captureStrategy);
    }

    /**
     * 切换摄像头
     */
    public void switchCamera() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }

        if (isCameraFront) {
            isCameraFront = false;
            setupCamera(width, height);
            openCamera(mCameraId);
        } else {
            isCameraFront = true;
            setupCamera(width, height);
            openCamera(mCameraIdFront);
        }
    }

    /**
     * ******************************** 配置摄像头参数 *********************************
     * Tries to open a {@link CameraDevice}. The result is listened by mStateCallback`.
     */
    private void setupCamera(int width, int height) {

        CameraManager manager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
        try {
            //0表示后置摄像头,1表示前置摄像头
            mCameraId = manager.getCameraIdList()[0];
            mCameraIdFront = manager.getCameraIdList()[1];

            //前置摄像头和后置摄像头的参数属性不同，所以这里要做下判断
            if (isCameraFront) {
                characteristics = manager.getCameraCharacteristics(mCameraIdFront);
            } else {
                characteristics = manager.getCameraCharacteristics(mCameraId);
            }
            mViewHolder.pvLayout.getViewHolder().btnClickOrLong.setCharacteristics(characteristics);

            // 找出是否需要交换尺寸以获得相对于传感器的预览尺寸Find out if we need to swap dimension to get the preview size relative to sensor
            // coordinate.
            int displayRotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
            //noinspection ConstantConditions
            int mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            boolean swappedDimensions = false;
            switch (displayRotation) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                        swappedDimensions = true;
                    }
                    break;
                case Surface.ROTATION_90:
                case Surface.ROTATION_270:
                    if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                        swappedDimensions = true;
                    }
                    break;
                default:
                    Log.e(TAG, "Display rotation is invalid: " + displayRotation);
            }

            Point displaySize = new Point();
            mActivity.getWindowManager().getDefaultDisplay().getSize(displaySize);
            // int rotatedPreviewWidth = width;
            // int rotatedPreviewHeight = height;
            int maxPreviewWidth = displaySize.x;
            int maxPreviewHeight = displaySize.y;
            if (swappedDimensions) {
                //rotatedPreviewWidth = height;
                //rotatedPreviewHeight = width;
                maxPreviewWidth = displaySize.y;
                maxPreviewHeight = displaySize.x;
            }
            if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                maxPreviewWidth = MAX_PREVIEW_WIDTH;
            }
            if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                maxPreviewHeight = MAX_PREVIEW_HEIGHT;
            }

            // 选择相机预览和视频录制的大小
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            // Integer mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            mVideoSize = Camera2Util.chooseVideoSize(map.getOutputSizes(MediaRecorder.class));

            // 获取相机支持的最大拍照尺寸
            mCaptureSize = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizeByArea());
            //mPreviewSize = Camera2Util.chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth, maxPreviewHeight, mCaptureSize);
            mPreviewSize = Camera2Util.chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), maxPreviewWidth, maxPreviewHeight, mVideoSize);

            configureTransform(width, height);

            //此ImageReader用于拍照所需
            setupImageReader();

            //MediaRecorder用于录像所需
            mMediaRecorder = new MediaRecorder();

        } catch (CameraAccessException e) {
            //UIUtil.toastByText("Cannot access the camera.", Toast.LENGTH_SHORT);
            Toast.makeText(mActivity, "无法使用摄像头.", Toast.LENGTH_SHORT).show();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            //UIUtil.toastByText("This device doesn't support Camera2 API.", Toast.LENGTH_SHORT);
            Toast.makeText(mActivity, "设备不支持 Camera2 API.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 预览界面的一些画面角度配置
     * @param viewWidth 预览界面宽度
     * @param viewHeight 预览界面高度
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == mViewHolder.texture || null == mPreviewSize ) {
            return;
        }
        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        mViewHolder.texture.setTransform(matrix);
    }

    /**
     * 配置ImageReader,用于图片处理
     */
    private void setupImageReader() {
        // 2代表ImageReader中最多可以获取两帧图像流
        mImageReader = ImageReader.newInstance(mCaptureSize.getWidth(), mCaptureSize.getHeight(), ImageFormat.JPEG, 2);

        // 处理图片事件
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image mImage = reader.acquireNextImage();
                ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);
                picSavePath = mMediaStoreCompat.getFilePath(1);
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(picSavePath);
                    fos.write(data, 0, data.length);//保存图片

                    Message msg = new Message();
                    msg.what = 0;
                    msg.obj = picSavePath;
                    mBackgroundHandler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                mImage.close();
            }
        }, mBackgroundHandler);

        // 处理图片ui
        mBackgroundHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        //这里拍照保存完成，可以进行相关的操作
                        rl_preview.setVisibility(View.VISIBLE);
                        //展示图片
                        try {
                            File captureImage = new File(picSavePath);
                            Bitmap bitmap = BitmapDecoder.decodeSampledBitmapFromFile(captureImage, new BitmapSize(widthPixels, heightPixels), Bitmap.Config.RGB_565);
                            iv_preview.setImageBitmap(bitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        iv_preview.setVisibility(View.VISIBLE);
                        video_preview.setVisibility(View.GONE);
                        //设置发送图片参数
                        resultIntent = new Intent();
                        resultIntent.putExtra("isPhoto",true);
                        resultIntent.putExtra("imageUrl",picSavePath);

                        mCaptureLayout.startAlphaAnimation();
                        mCaptureLayout.startTypeBtnAnimator();

                        isPreviewing = true;
                        Log.d(TAG,"保存图片成功");
                        break;
                }
            }
        };









    }

    static class BackgroundHandler extends Handler {

        private final WeakReference<CameraLayout> mCameraLayout;

        public BackgroundHandler(CashActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

    }


}
