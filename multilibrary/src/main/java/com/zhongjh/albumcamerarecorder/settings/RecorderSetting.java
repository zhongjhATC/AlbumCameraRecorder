package com.zhongjh.albumcamerarecorder.settings;

/**
 * 录音机
 */
public final class RecorderSetting {

    private final RecordeSpec mRecordeSpec;

    public RecorderSetting() {
        mRecordeSpec = RecordeSpec.getInstance();
    }

    /**
     * 提供保存公有或者私有的文件路径
     * 文件路径存储于 {@link android.support.v4.content.FileProvider}.
     *
     * @param captureStrategy {@link CaptureStrategy},仅仅启用时才需要
     * @return {@link GlobalSetting} for fluent API.
     */
    public RecorderSetting captureStrategy(CaptureStrategy captureStrategy) {
        mRecordeSpec.captureStrategy = captureStrategy;
        return this;
    }

}
