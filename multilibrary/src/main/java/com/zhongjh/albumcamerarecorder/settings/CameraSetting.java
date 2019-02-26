package com.zhongjh.albumcamerarecorder.settings;

import android.support.annotation.NonNull;

import com.zhongjh.albumcamerarecorder.album.enums.MimeType;

import java.util.Set;

/**
 * Created by zhongjh on 2018/12/26.
 */
public final class CameraSetting {

    private final CameraSpec mCameraSpec;

    public CameraSetting() {
        mCameraSpec = CameraSpec.getInstance();
    }

    /**
     * 支持的类型：图片，视频
     * 这个优先于 {@link MultiMediaSetting#choose}
     * @param mimeTypes 类型
     * @return this
     */
    public CameraSetting mimeTypeSet(@NonNull Set<MimeType> mimeTypes) {
        mCameraSpec.mimeTypeSet = mimeTypes;
        return this;
    }

    /**
     * 提供保存公有或者私有的文件路径
     * 文件路径存储于 {@link android.support.v4.content.FileProvider}.
     * 这个优先于 {@link GlobalSetting#captureStrategy}
     *
     * @param captureStrategy {@link CaptureStrategy},仅仅启用时才需要
     * @return {@link GlobalSetting} for fluent API.
     */
    public CameraSetting captureStrategy(CaptureStrategy captureStrategy) {
        mCameraSpec.captureStrategy = captureStrategy;
        return this;
    }

    /**
     * 最长录制时间,默认10秒
     *
     * @param duration 最长录制时间,单位为秒
     * @return {@link GlobalSetting} for fluent API.
     */
    public CameraSetting duration(int duration) {
        mCameraSpec.duration = duration;
        return this;
    }

    /**
     * 最短录制时间限制，单位为毫秒，即是如果长按在1500毫秒内，都暂时不开启录制
     *
     * @param minDuration 最短录制时间限制，单位为毫秒
     * @return {@link GlobalSetting} for fluent API.
     */
    public CameraSetting minDuration(int minDuration) {
        mCameraSpec.minDuration = minDuration;
        return this;
    }

    /**
     * 更换 切换前置/后置摄像头图标资源
     *
     * @param imageSwitch 切换前置/后置摄像头图标资源
     * @return {@link GlobalSetting} for fluent API.
     */
    public CameraSetting imageSwitch(int imageSwitch) {
        mCameraSpec.imageSwitch = imageSwitch;
        return this;
    }

    /**
     * 更换 闪光灯开启状态图标
     *
     * @param imageFlashOn 闪光灯开启状态图标
     * @return {@link GlobalSetting} for fluent API.
     */
    public CameraSetting imageFlashOn(int imageFlashOn) {
        mCameraSpec.imageFlashOn = imageFlashOn;
        return this;
    }

    /**
     * 更换 闪光灯关闭状态图标
     *
     * @param imageFlashOff 闪光灯关闭状态图标
     * @return {@link GlobalSetting} for fluent API.
     */
    public CameraSetting imageFlashOff(int imageFlashOff) {
        mCameraSpec.imageFlashOff = imageFlashOff;
        return this;
    }

    /**
     * 更换 闪光灯自动状态图标
     *
     * @param imageFlashAuto 闪光灯自动状态图标
     * @return {@link GlobalSetting} for fluent API.
     */
    public CameraSetting imageFlashAuto(int imageFlashAuto) {
        mCameraSpec.imageFlashAuto = imageFlashAuto;
        return this;
    }

}
