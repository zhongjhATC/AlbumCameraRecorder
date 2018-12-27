package com.zhongjh.cameraviewsoundrecorder.settings;

import android.support.annotation.NonNull;

import com.zhongjh.cameraviewsoundrecorder.album.enums.MimeType;

import java.util.Set;

/**
 * Created by zhongjh on 2018/12/26.
 */
public final class CameraSetting {

    private final CameraSpec mCameraSpec;

    public CameraSetting(@NonNull Set<MimeType> mimeTypes) {
        mCameraSpec = CameraSpec.getInstance();
        mCameraSpec.mimeTypeSet = mimeTypes;
    }

    /**
     * 仅仅支持一个多媒体类型
     * @param supportSingleMediaType 是否
     * @return this
     */
    public CameraSetting supportSingleMediaType(boolean supportSingleMediaType) {
        mCameraSpec.supportSingleMediaType = supportSingleMediaType;
        return this;
    }

    /**
     * 提供保存公有或者私有的文件路径
     * 文件路径存储于 {@link android.support.v4.content.FileProvider}.
     *
     * @param captureStrategy {@link CaptureStrategy},仅仅启用时才需要
     * @return {@link GlobalSetting} for fluent API.
     */
    public CameraSetting captureStrategy(CaptureStrategy captureStrategy) {
        mCameraSpec.captureStrategy = captureStrategy;
        return this;
    }

}
