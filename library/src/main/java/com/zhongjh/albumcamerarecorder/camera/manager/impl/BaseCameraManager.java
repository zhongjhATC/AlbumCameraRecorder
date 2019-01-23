package com.zhongjh.albumcamerarecorder.camera.manager.impl;

import android.content.Context;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

import com.zhongjh.albumcamerarecorder.camera.config.CameraConfig;
import com.zhongjh.albumcamerarecorder.camera.config.CameraConfigProvider;
import com.zhongjh.albumcamerarecorder.camera.entity.Size;
import com.zhongjh.albumcamerarecorder.camera.manager.CameraManager;

/**
 * Created by zhongjh on 2019/1/3.
 */

public abstract class BaseCameraManager<CameraId, SurfaceListener> implements CameraManager<CameraId, SurfaceListener>
        , MediaRecorder.OnInfoListener {

    private static final String TAG = "BaseCameraManager";

    Context mContext;
    CameraConfigProvider cameraConfigProvider;

    MediaRecorder mMediaRecorder;
    boolean mIsVideoRecording = false;  // 是否正在录像中

    CameraId mCameraId = null;
    CameraId mFaceFrontCameraId = null;
    CameraId mFaceBackCameraId = null;
    int mNumberOfCameras = 0;
    int mFaceFrontCameraOrientation;
    int mFaceBackCameraOrientation;

    Size mPhotoSize;
    Size mVideoSize;
    Size mPreviewSize;
    Size mWindowSize;
    CamcorderProfile mCamcorderProfile;     // 这个类是保存摄像机配置信息的一个实体类，准确说是保存音视频配置信息

    HandlerThread mBackgroundThread;
    Handler mBackgroundHandler;
    Handler mUiiHandler = new Handler(Looper.getMainLooper());

    @Override
    public void initializeCameraManager(CameraConfigProvider cameraConfigProvider, Context context) {
        this.mContext = context;
        this.cameraConfigProvider = cameraConfigProvider;
        startBackgroundThread();
    }

    @Override
    public void releaseCameraManager() {
        this.mContext = null;
        stopBackgroundThread();
    }

    /**
     * 准备相机输出
     */
    protected abstract void prepareCameraOutputs();

    /**
     * 准备录像配置
     * @return 是否准备好
     */
    protected abstract boolean prepareVideoRecorder();

    /**
     * 已到最大录像时间
     */
    protected abstract void onMaxDurationReached();

    /**
     * 已到最大文件大小
     */
    protected abstract void onMaxFileSizeReached();

    /**
     * @param sensorPosition 传感位置
     * @return 获取照片方向
     */
    protected abstract int getPhotoOrientation(@CameraConfig.SensorPosition int sensorPosition);

    /**
     * @param sensorPosition 传感位置
     * @return 获取录像方向
     */
    protected abstract int getVideoOrientation(@CameraConfig.SensorPosition int sensorPosition);

    /**
     * 释放录像机
     */
    protected void releaseVideoRecorder() {
        try {
            if (mMediaRecorder != null) {
                mMediaRecorder.reset();
                mMediaRecorder.release();
            }
        } catch (Exception ignore) {

        } finally {
            mMediaRecorder = null;
        }
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        if (MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED == what) {
            onMaxDurationReached();
        } else if (MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED == what) {
            onMaxFileSizeReached();
        }
    }

    @Override
    public boolean isVideoRecording() {
        return mIsVideoRecording;
    }


    @Override
    public void setCameraId(CameraId cameraId) {
        this.mCameraId = cameraId;
    }

    @Override
    public CameraId getCameraId() {
        return mCameraId;
    }

    @Override
    public CameraId getFaceFrontCameraId() {
        return mFaceFrontCameraId;
    }

    @Override
    public CameraId getFaceBackCameraId() {
        return mFaceBackCameraId;
    }

    @Override
    public int getNumberOfCameras() {
        return mNumberOfCameras;
    }

    public int getFaceFrontCameraOrientation() {
        return mFaceFrontCameraOrientation;
    }

    public int getFaceBackCameraOrientation() {
        return mFaceBackCameraOrientation;
    }

    /**
     * 启动后台线程
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * 停止后台线程
     */
    private void stopBackgroundThread() {
        if (Build.VERSION.SDK_INT > 17)
            mBackgroundThread.quitSafely();
        else
            mBackgroundThread.quit();

        try {
            mBackgroundThread.join(); // 是主线程等待子线程的终止。也就是在子线程调用了join()方法后面的代码，只有等到子线程结束了才能执行
        } catch (InterruptedException e) {
            Log.e(TAG, "stopBackgroundThread: ", e);
        } finally {
            mBackgroundThread = null;
            mBackgroundHandler = null;
        }
    }

}
