package com.zhongjh.albumcamerarecorder.camera.manager;

import android.content.Context;

import com.zhongjh.albumcamerarecorder.camera.config.CameraConfig;
import com.zhongjh.albumcamerarecorder.camera.config.CameraConfigProvider;
import com.zhongjh.albumcamerarecorder.camera.entity.Size;
import com.zhongjh.albumcamerarecorder.camera.listener.OnCameraResultListener;
import com.zhongjh.albumcamerarecorder.camera.manager.listener.CameraCloseListener;
import com.zhongjh.albumcamerarecorder.camera.manager.listener.CameraOpenListener;
import com.zhongjh.albumcamerarecorder.camera.manager.listener.CameraPictureListener;
import com.zhongjh.albumcamerarecorder.camera.manager.listener.CameraVideoListener;

import java.io.File;

/**
 * 兼容Camera和Camera2的接口处理
 *
 * @param <CameraId>        相册id
 * @param <SurfaceListener> Camera和Camera2的各自事件
 */
public interface CameraManager<CameraId, SurfaceListener> {

    /**
     * 初始化Camera管理
     *
     * @param cameraConfigProvider 配置
     * @param context              上下文
     */
    void initializeCameraManager(CameraConfigProvider cameraConfigProvider, Context context);

    /**
     * 重置Camera管理
     */
    void releaseCameraManager();

    /**
     * 打开相机
     *
     * @param cameraId           相机id
     * @param cameraOpenListener 打开相机的相关回调事件
     */
    void openCamera(CameraId cameraId, CameraOpenListener<CameraId, SurfaceListener> cameraOpenListener);

    /**
     * 关闭相机
     *
     * @param cameraCloseListener 关闭相机的相关回调事件
     */
    void closeCamera(CameraCloseListener<CameraId> cameraCloseListener);

    /**
     * 拍摄照片，仔细说说cameraPictureListener和callback之间关系
     * 当拍摄完返回byte数据后，v-p-b先调用cameraPictureListener，里面参数包含callback，然后再回调给b-p-v
     *
     * @param photoFile             存储的图片路径
     * @param cameraPictureListener 自身view执行的事件
     * @param callback              回调给最外层view使用
     */
    void takePicture(File photoFile, CameraPictureListener cameraPictureListener, OnCameraResultListener callback);

    /**
     * 开始录像
     *
     * @param videoFile           存储的视频路径
     * @param cameraVideoListener 自身view执行的事件
     */
    void startVideoRecord(File videoFile, CameraVideoListener cameraVideoListener);

    /**
     * 停止录像
     *
     * @param callback 回调事件
     */
    void stopVideoRecord(OnCameraResultListener callback);

    /**
     * @return 是否正在录像
     */
    boolean isVideoRecording();

    /**
     * 设置照相机id
     *
     * @param cameraId 照相机id
     */
    void setCameraId(CameraId cameraId);

    /**
     * 设置闪光灯模式
     *
     * @param flashMode 闪光灯模式
     */
    void setFlashMode(@CameraConfig.FlashMode int flashMode);

    /**
     * @return 返回摄像头id
     */
    CameraId getCameraId();

    /**
     * @return 返回前置摄像头id
     */
    CameraId getFaceFrontCameraId();

    /**
     * @return 返回后置摄像头id
     */
    CameraId getFaceBackCameraId();

    /**
     * @return 返回摄像头数量
     */
    int getNumberOfCameras();

    /**
     * @return 返回摄像头的前置角度
     */
    int getFaceFrontCameraOrientation();

    /**
     * @return 返回摄像头的后置角度
     */
    int getFaceBackCameraOrientation();

    /**
     * @param mediaQuality 质量
     * @return 返回图片大小
     */
    Size getPictureSizeForQuality(@CameraConfig.MediaQuality int mediaQuality);

    /**
     * @return 图片质量设置
     */
    CharSequence[] getPictureQualityOptions();

    /**
     * @return 视频质量设置
     */
    CharSequence[] getVideoQualityOptions();
}
