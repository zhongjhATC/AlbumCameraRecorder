package com.zhongjh.albumcamerarecorder.camera;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import com.zhongjh.albumcamerarecorder.camera.listener.ErrorListener;
import com.zhongjh.albumcamerarecorder.camera.util.Camera2Util;
import com.zhongjh.albumcamerarecorder.camera.util.CompareSizeByArea;
import com.zhongjh.albumcamerarecorder.settings.CameraSpec;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.albumcamerarecorder.settings.MediaStoreCompat;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 5.0版本以上推荐使用camera2
 * Created by zhongjh on 2018/12/29.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class CameraOperation2 implements CameraInterface {

    private static final String TAG = "CameraOperation2";
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

    Context mContext;
    CameraLayout.ViewHolder mViewHolder;

    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();

    private boolean mIsRecordingVideo = false;//是否正在录制视频
    private boolean isStop = false;//是否停止过了MediaRecorder
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    //region 摄像头相关

    private CameraDevice mCameraDevice; // 是连接在安卓设备上的单个相机的抽象表示
    private CameraCaptureSession mPreviewSession;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCharacteristics characteristics;  // 关于这个设备的一些特性参数，比如输出图像的大小，是否支持闪光灯等信息
    private ImageReader mImageReader;   // 拍照后返回的图片，包含各种数据，回调
    public String picSavePath;//图片保存路径
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

    private String mNextVideoAbsolutePath;//视频路径

    private Rect zoom;

    private Intent resultIntent;

    private boolean isPreviewing;//处于预览状态

    CameraCallback.TakePictureCallback mTakePictureCallback; // 拍照回调事件

    public CameraOperation2(Context context, CameraLayout cameraLayout, CameraCallback.TakePictureCallback takePictureCallback) {
        mContext = context;
        mTakePictureCallback = takePictureCallback;
        mViewHolder = cameraLayout.mViewHolder;
        GlobalSpec globalSpec = GlobalSpec.getInstance();
        CameraSpec cameraSpec = CameraSpec.getInstance();
        mMediaStoreCompat = new MediaStoreCompat(context);
        mMediaStoreCompat.setCaptureStrategy(cameraSpec.captureStrategy == null ? globalSpec.captureStrategy : cameraSpec.captureStrategy);
    }

    // region 对外开放的API

    @Override
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

    @Override
    public void takePicture() {
        mIsRecordingVideo = false;
        if (null == mCameraDevice || !mViewHolder.texture.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            CaptureRequest.Builder mCaptureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            //获取屏幕方向
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            int rotation = wm.getDefaultDisplay().getRotation();
            mCaptureBuilder.addTarget(mImageReader.getSurface());
            //isCameraFront是自定义的一个boolean值，用来判断是不是前置摄像头，是的话需要旋转180°，不然拍出来的照片会歪了
            if (isCameraFront) {
                mCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, DEFAULT_ORIENTATIONS.get(Surface.ROTATION_180));
            } else {
                mCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, DEFAULT_ORIENTATIONS.get(rotation));
            }

            //锁定焦点
            mCaptureBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);

            //判断预览的时候是否两指缩放过,是的话需要保持当前的缩放比例
            mCaptureBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);

            // 拍照的回调
            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    //拍完照unLockFocus
                    unLockFocus();
                }
            };
            mPreviewSession.stopRepeating();
            //咔擦拍照
            mPreviewSession.capture(mCaptureBuilder.build(), CaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void recordStart() {
        mIsRecordingVideo = true;
        startRecordingVideo();
        mMediaRecorder.start();
        isStop = false;
    }

    @Override
    public void recordEnd() {
        mIsRecordingVideo = false;
        if (!isStop)
            stopRecordingVideo();
    }

    @Override
    public void recordZoom(Rect zoom) {
        mPreviewBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);
        try {
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setErrorLinsenter(ErrorListener errorLisenter) {

    }

    // endregion 对外开放的API

    /**
     * ******************************** 配置摄像头参数 *********************************
     * Tries to open a {@link CameraDevice}. The result is listened by mStateCallback`.
     */
    private void setupCamera(int width, int height) {
        CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
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
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            int displayRotation = wm.getDefaultDisplay().getRotation();
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
            wm.getDefaultDisplay().getSize(displaySize);
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
            Toast.makeText(mContext, "无法使用摄像头.", Toast.LENGTH_SHORT).show();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            //UIUtil.toastByText("This device doesn't support Camera2 API.", Toast.LENGTH_SHORT);
            Toast.makeText(mContext, "设备不支持 Camera2 API.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 预览界面的一些画面角度配置
     *
     * @param viewWidth  预览界面宽度
     * @param viewHeight 预览界面高度
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == mViewHolder.texture || null == mPreviewSize) {
            return;
        }
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        int rotation = wm.getDefaultDisplay().getRotation();
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
        mImageReader.setOnImageAvailableListener(reader -> {
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
        }, mBackgroundHandler);

        // 处理图片ui
        mBackgroundHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

            }
        };


    }

    /**
     * ******************************openCamera(打开Camera)*****************************************
     */
    private void openCamera(String CameraId) {
        //获取摄像头的管理者CameraManager
        CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        //检查权限
        try {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //打开相机，第一个参数指示打开哪个摄像头，第二个参数stateCallback为相机的状态回调接口，第三个参数用来确定Callback在哪个线程执行，为null的话就在当前线程执行
            manager.openCamera(CameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // 摄像头状态回调
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            startPreview();
            if (null != mViewHolder.texture) {
                configureTransform(mViewHolder.texture.getWidth(), mViewHolder.texture.getHeight());
            }
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            cameraDevice.close();
            mCameraDevice = null;
        }
    };

    /**
     * ************************************* 启动预览. **********************************
     */
    private void startPreview() {
        if (null == mCameraDevice || !mViewHolder.texture.isAvailable() || null == mPreviewSize) {
            return;
        }
        SurfaceTexture texture = mViewHolder.texture.getSurfaceTexture();
        if (texture == null) {
            return;
        }
        try {
            //closePreviewSession();
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            Surface previewSurface = new Surface(texture);
            mPreviewBuilder.addTarget(previewSurface);

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    //创建捕获请求
                    mPreviewSession = cameraCaptureSession;
                    mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                    //不停的发送获取图像请求，完成连续预览
                    try {
                        mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(mContext, "Failed", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void unLockFocus() {
        try {
            // 构建失能AF的请求
            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            // 闪光灯重置为未开启状态
            mPreviewBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
            // 继续开启预览
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, mBackgroundHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始录像
     */
    private void startRecordingVideo() {
        if (null == mCameraDevice || !mViewHolder.texture.isAvailable() || null == mPreviewSize) {
            Toast.makeText(mContext, "录制失败", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            //closePreviewSession();
            setUpMediaRecorder();
            SurfaceTexture texture = mViewHolder.texture.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();

            // Set up Surface for the camera preview
            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);

            // Set up Surface for the MediaRecorder
            Surface mRecorderSurface = mMediaRecorder.getSurface();
            surfaces.add(mRecorderSurface);
            mPreviewBuilder.addTarget(mRecorderSurface);

            // Start a capture session
            // Once the session starts, we can update the UI and start recording
            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mPreviewSession = cameraCaptureSession;
                    try {
                        mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(mContext, "Failed", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 结束录像
     */
    private void stopRecordingVideo() {
        mIsRecordingVideo = false;
        try {
            if (mPreviewSession != null) {
                mPreviewSession.stopRepeating();
                mPreviewSession.abortCaptures();
            }
            mBackgroundHandler.removeCallbacksAndMessages(null);
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.setOnInfoListener(null);
            mMediaRecorder.setPreviewDisplay(null);
            mMediaRecorder.stop();
            // Stop recording
            mMediaRecorder.reset();
            isStop = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "录制成功");
        resultIntent = new Intent();
        resultIntent.putExtra("videoPath", mNextVideoAbsolutePath);
        boolean isOK = getVideoWH(mNextVideoAbsolutePath, resultIntent);
        if (isOK) {
            isPreviewing = true;
//            video_preview.setVisibility(View.VISIBLE);
//            iv_preview.setVisibility(View.GONE);
//            video_preview.setVideoPath(mNextVideoAbsolutePath);
//            video_preview.requestFocus();
//            video_preview.start();
//            rl_preview.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(mContext, "录制失败，需要重启手机才能进行录制", Toast.LENGTH_SHORT).show();
//            invokeResetDelay();
        }
    }

    /**
     * 录像配置
     */
    private void setUpMediaRecorder() throws IOException {
        mMediaRecorder.reset();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        if (mNextVideoAbsolutePath == null || mNextVideoAbsolutePath.isEmpty()) {
            mNextVideoAbsolutePath = mMediaStoreCompat.getFilePath(1);
        }

        // 这里有点投机取巧的方式，不过证明方法也是不错的
        // 录制出来10S的视频，大概1.2M，清晰度不错，而且避免了因为手动设置参数导致无法录制的情况
        // 手机一般都有这个格式CamcorderProfile.QUALITY_480P,因为单单录制480P的视频还是很大的，所以我们在手动根据预览尺寸配置一下videoBitRate,值越高越大
        // QUALITY_QVGA清晰度一般，不过视频很小，一般10S才几百K
        // 判断有没有这个手机有没有这个参数
        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P)) {
            CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
            profile.videoBitRate = mPreviewSize.getWidth() * mPreviewSize.getHeight();
            mMediaRecorder.setProfile(profile);
            mMediaRecorder.setPreviewDisplay(new Surface(mViewHolder.texture.getSurfaceTexture()));
        } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P)) {
            CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
            profile.videoBitRate = mPreviewSize.getWidth() * mPreviewSize.getHeight();
            mMediaRecorder.setProfile(profile);
            mMediaRecorder.setPreviewDisplay(new Surface(mViewHolder.texture.getSurfaceTexture()));
        } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_QVGA)) {
            mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_QVGA));
            mMediaRecorder.setPreviewDisplay(new Surface(mViewHolder.texture.getSurfaceTexture()));
        } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_CIF)) {
            mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_CIF));
            mMediaRecorder.setPreviewDisplay(new Surface(mViewHolder.texture.getSurfaceTexture()));
        } else {
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setVideoEncodingBitRate(1200 * 1280);
            mMediaRecorder.setVideoFrameRate(30);
            mMediaRecorder.setVideoSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        }

        mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);

        //判断是不是前置摄像头,是的话需要旋转对应的角度
        if (isCameraFront) {
            mMediaRecorder.setOrientationHint(270);
        } else {
            mMediaRecorder.setOrientationHint(90);
        }

        mMediaRecorder.prepare();
    }

    private boolean getVideoWH(String path, Intent intent) {
        MediaMetadataRetriever retr = new MediaMetadataRetriever();
        retr.setDataSource(path);
        String height = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT); // 视频高度
        String width = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH); // 视频宽度
        String rotation = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION); // 视频旋转方向
        String duration = retr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
        if (isNumeric(width) && isNumeric(height)) {
            if (rotation.equals("90") || rotation.equals("270")) {
                intent.putExtra("videoWidth", Integer.parseInt(height));
                intent.putExtra("videoHeight", Integer.parseInt(width));
                intent.putExtra("imaWidth", Integer.parseInt(height));
                intent.putExtra("imaHeght", Integer.parseInt(width));
                intent.putExtra("w", Integer.parseInt(height));
                intent.putExtra("h", Integer.parseInt(width));
            } else {
                intent.putExtra("videoWidth", Integer.parseInt(width));
                intent.putExtra("videoHeight", Integer.parseInt(height));
                intent.putExtra("imaWidth", Integer.parseInt(width));
                intent.putExtra("imaHeght", Integer.parseInt(height));
                intent.putExtra("w", Integer.parseInt(width));
                intent.putExtra("h", Integer.parseInt(height));
            }
        } else {
            return false;
        }
        if (isNumeric(duration)) {
            intent.putExtra("totalTime", Long.parseLong(duration) / 1000);
        }
        return true;
    }

    private boolean isNumeric(String str) {
        try {
            if (TextUtils.isEmpty(str)) {
                return false;
            }
            for (int i = str.length(); --i >= 0; ) {
                if (!Character.isDigit(str.charAt(i))) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static class BackgroundHandler extends Handler {
        private final WeakReference<CameraOperation2> mCameraOperation2;

        public BackgroundHandler(CameraOperation2 sameraOperation2) {
            mCameraOperation2 = new WeakReference<>(sameraOperation2);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
//                        //这里拍照保存完成，可以进行相关的操作
//                        rl_preview.setVisibility(View.VISIBLE);
//                    //展示图片
//                    try {
//                        File captureImage = new File(mCameraOperation2.get().picSavePath);
//                        Bitmap bitmap = BitmapDecoder.decodeSampledBitmapFromFile(captureImage, new BitmapSize(DisplayMetricsSPUtils.getScreenWidth(mCameraOperation2.get().mContext), DisplayMetricsSPUtils.getScreenHeight(mCameraOperation2.get().mContext)), Bitmap.Config.RGB_565);
//                        if (mCameraOperation2.get().mTakePictureCallback != null) {
//                            if (mPictureAngle == 90 || mPictureAngle == 270) {
//                                mCameraOperation2.get().mTakePictureCallback.captureResult(bitmap, true);
//                            } else {
//                                mCameraOperation2.get().mTakePictureCallback.captureResult(bitmap, false);
//                            }
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                        iv_preview.setVisibility(View.VISIBLE);
//                        video_preview.setVisibility(View.GONE);
//                        //设置发送图片参数
//                        resultIntent = new Intent();
//                        resultIntent.putExtra("isPhoto",true);
//                        resultIntent.putExtra("imageUrl",picSavePath);
//
//                        mCaptureLayout.startAlphaAnimation();
//                        mCaptureLayout.startTypeBtnAnimator();
//
//                        isPreviewing = true;
//                        Log.d(TAG,"保存图片成功");
//                        break;
            }
        }
    }

}
