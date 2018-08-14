package com.zhongjh.cameraviewsoundrecorder.camera.other;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.widget.ImageView;

import com.zhongjh.cameraviewsoundrecorder.common.Constants;
import com.zhongjh.cameraviewsoundrecorder.listener.ErrorListener;
import com.zhongjh.cameraviewsoundrecorder.util.CameraParamUtil;
import com.zhongjh.cameraviewsoundrecorder.util.DeviceUtil;
import com.zhongjh.cameraviewsoundrecorder.util.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static android.graphics.Bitmap.createBitmap;


/**
 * 关于照相机的操作类
 * Created by zhongjh on 2018/8/10.
 */
public class CameraOperation implements Camera.PreviewCallback {

    private static final String TAG = "CameraOperation";

    private Camera mCamera;
    private Camera.Parameters mParams; // 相机的属性
    private boolean mIsPreviewing = false; // 目前是否处于预览状态
    private int mPreviewWidth; // 预览的宽度
    private int mPreviewHeight; // 预览的高度

    private int mSelectedCamera;            // 当前摄像头是前置还是后置
    private int CAMERA_POST_POSITION;       // 摄像头后置
    private int CAMERA_FRONT_POSITION;      // 摄像头前置

    private SurfaceHolder mSurfaceHolder = null; // 显示一个surface的抽象接口，使你能够控制surface的大小和格式， 以及在surface上编辑像素，和监视surace的改变。
    private float mScreenProp = -1.0f; // 当前手机屏幕高宽比例，后面让摄像预览界面等也跟这个一样

    private boolean mIsRecorder = false;    // 录像中
    private MediaRecorder mMediaRecorder;   // 记录音频与视频
    private String mVideoFileName;           // 文件保存的file名称
    private String mSaveVideoPath;          // 保存文件的路径
    private String mVideoFileAbsPath;       // 统一上面两个String的路径
    private Bitmap mVideoFirstFrame = null; // 录像的第一祯bitmap
    private int mMediaQuality = Constants.MEDIA_QUALITY_MIDDLE;  //视频质量

    private ErrorListener mErrorLisenter; // 异常事件

    private ImageView mImgSwitch;
    private ImageView mImgFlash;


    private int mPhoneAngle = 0;    // 手机的角度，通过方向传感器获的
    private int mCameraAngle = 90;  //摄像头角度   默认为后置摄像头90度 前置摄像头180度 270度是
    private int mPictureAngle;      // 拍照后给予照片的角度，通过当前手机角度、摄像头角度计算

    private byte[] mPreviewFrameData; // 照相机返回的数据源

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        mPreviewFrameData = data;
    }

    /**
     * 摄像头启动浏览
     *
     * @param surfaceHolder 显示一个surface的抽象接口，使你能够控制surface的大小和格式， 以及在surface上编辑像素，和监视surace的改变。
     * @param screenProp    当前手机屏幕高宽比例
     */
    private void doStartPreview(SurfaceHolder surfaceHolder, float screenProp) {
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
            mCamera.startPreview();//启动浏览
            mIsPreviewing = true; // 目前浏览状态中
            Log.i(TAG, "=== Start Preview ===");
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
     * 销毁Camera
     */
    private void doDestroyCamera() {
        mErrorLisenter = null;
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);

        }
    }

    // region 对外开放的API

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
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
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

    public void stopRecord(boolean isShort, CameraCallback.StopRecordCallback callback){
        // 不是正在录像就返回
        if (!mIsRecorder)
            return;

        if (mMediaRecorder != null){
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
                if (mMediaRecorder != null) {
                    mMediaRecorder.release();
                }
                mMediaRecorder = null;
                mIsRecorder = false;
            }


        }
    }

    /**
     * 设置摄像切换 和 闪关灯 控件
     *
     * @param imgSwitch 摄像切换控件
     * @param imgFlash  闪光灯控件
     */
    public void setSwitchAndFlash(ImageView imgSwitch, ImageView imgFlash) {
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
    void setErrorLinsenter(ErrorListener errorLisenter) {
        this.mErrorLisenter = errorLisenter;
    }

    // endregion

}
