package com.zhongjh.albumcamerarecorder.settings.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zhongjh.albumcamerarecorder.album.listener.OnSelectedListener;
import com.zhongjh.albumcamerarecorder.camera.listener.OnCameraViewListener;
import com.zhongjh.albumcamerarecorder.settings.AlbumSetting;
import com.zhongjh.albumcamerarecorder.settings.CameraSetting;
import com.zhongjh.albumcamerarecorder.settings.GlobalSetting;
import com.zhongjh.albumcamerarecorder.settings.MultiMediaSetting;

import java.util.Set;

import com.zhongjh.common.coordinator.VideoEditCoordinator;
import com.zhongjh.common.enums.MimeType;

/**
 * 有关拍摄界面的动态设置
 *
 * @author zhongjh
 * @date 2019/3/20
 */
public interface CameraSettingApi {

    /**
     * 销毁事件
     */
    void onDestroy();

    /**
     * 支持的类型：图片，视频
     * 这个优先于 {@link MultiMediaSetting#choose}
     *
     * @param mimeTypes 类型
     * @return {@link CameraSetting} for fluent API.
     */
    CameraSetting mimeTypeSet(@NonNull Set<MimeType> mimeTypes);

    /**
     * 最长录制时间,默认10秒
     *
     * @param duration 最长录制时间,单位为秒
     * @return {@link CameraSetting} for fluent API.
     */
    CameraSetting duration(int duration);

    /**
     * 最短录制时间限制，单位为毫秒，即是如果长按在1500毫秒内，都暂时不开启录制
     *
     * @param minDuration 最短录制时间限制，单位为毫秒
     * @return {@link CameraSetting} for fluent API.
     */
    CameraSetting minDuration(int minDuration);

    /**
     * 点击即录制（点击拍摄图片功能则失效）
     * @param isClickReocrd 是：开启该功能，否：关闭该功能
     * @return {@link CameraSetting} for fluent API.
     */
    CameraSetting isClickRecord(boolean isClickReocrd);

    /**
     * 启动视频编辑功能，目前只有视频分段录制，后续会增加
     * @param videoEditManager 视频编辑协调者
     * @return {@link CameraSetting} for fluent API.
     */
    CameraSetting videoEdit(VideoEditCoordinator videoEditManager);

    /**
     * 水印资源,可通过layout赋值水印，所处于的位置等等都可通过layout本身来处理
     * @param watermarkResource 水印资源的layout id
     * @return {@link CameraSetting} for fluent API.
     */
    CameraSetting watermarkResource(int watermarkResource);

    /**
     * 更换 切换前置/后置摄像头图标资源
     *
     * @param imageSwitch 切换前置/后置摄像头图标资源
     * @return {@link CameraSetting} for fluent API.
     */
    CameraSetting imageSwitch(int imageSwitch);

    /**
     * 更换 闪光灯开启状态图标
     *
     * @param imageFlashOn 闪光灯开启状态图标
     * @return {@link CameraSetting} for fluent API.
     */
    CameraSetting imageFlashOn(int imageFlashOn);

    /**
     * 更换 闪光灯关闭状态图标
     *
     * @param imageFlashOff 闪光灯关闭状态图标
     * @return {@link CameraSetting} for fluent API.
     */
    CameraSetting imageFlashOff(int imageFlashOff);

    /**
     * 更换 闪光灯自动状态图标
     *
     * @param imageFlashAuto 闪光灯自动状态图标
     * @return {@link CameraSetting} for fluent API.
     */
    CameraSetting imageFlashAuto(int imageFlashAuto);

    /**
     * 有关CameraView事件
     * <p>
     *
     * @param listener {@link OnCameraViewListener}
     * @return {@link CameraSetting} this
     */
    CameraSetting setOnCameraViewListener(@Nullable OnCameraViewListener listener);

}
