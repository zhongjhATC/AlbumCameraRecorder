package com.zhongjh.albumcamerarecorder.settings;

import androidx.annotation.NonNull;

import com.zhongjh.albumcamerarecorder.settings.api.CameraSettingApi;

import gaode.zhongjh.com.common.enums.MimeType;

import java.util.Set;

/**
 * 有关拍摄界面的动态设置
 * Created by zhongjh on 2018/12/26.
 */
public final class CameraSetting implements CameraSettingApi {

    private final CameraSpec mCameraSpec;

    public CameraSetting() {
        mCameraSpec = CameraSpec.getCleanInstance();
    }

    @Override
    public CameraSetting mimeTypeSet(@NonNull Set<MimeType> mimeTypes) {
        mCameraSpec.mimeTypeSet = mimeTypes;
        return this;
    }

    @Override
    public CameraSetting duration(int duration) {
        mCameraSpec.duration = duration;
        return this;
    }

    @Override
    public CameraSetting minDuration(int minDuration) {
        mCameraSpec.minDuration = minDuration;
        return this;
    }

    @Override
    public CameraSetting imageSwitch(int imageSwitch) {
        mCameraSpec.imageSwitch = imageSwitch;
        return this;
    }

    @Override
    public CameraSetting imageFlashOn(int imageFlashOn) {
        mCameraSpec.imageFlashOn = imageFlashOn;
        return this;
    }

    @Override
    public CameraSetting imageFlashOff(int imageFlashOff) {
        mCameraSpec.imageFlashOff = imageFlashOff;
        return this;
    }

    @Override
    public CameraSetting imageFlashAuto(int imageFlashAuto) {
        mCameraSpec.imageFlashAuto = imageFlashAuto;
        return this;
    }

}
