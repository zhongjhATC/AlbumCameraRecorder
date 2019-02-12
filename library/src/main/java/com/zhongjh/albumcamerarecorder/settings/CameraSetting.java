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

}
