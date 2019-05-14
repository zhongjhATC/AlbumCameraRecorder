package com.zhongjh.albumcamerarecorder.settings;

import com.zhongjh.albumcamerarecorder.settings.api.RecorderSettingApi;

/**
 * 录音机
 */
public final class RecorderSetting implements RecorderSettingApi {

    private final RecordeSpec mRecordeSpec;

    public RecorderSetting() {
        mRecordeSpec = RecordeSpec.getCleanInstance();
    }

    @Override
    public RecorderSetting duration(int duration) {
        mRecordeSpec.duration = duration;
        return this;
    }

    @Override
    public RecorderSetting minDuration(int minDuration) {
        mRecordeSpec.minDuration = minDuration;
        return this;
    }

}
