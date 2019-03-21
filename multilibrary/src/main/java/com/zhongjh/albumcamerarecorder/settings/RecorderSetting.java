package com.zhongjh.albumcamerarecorder.settings;

import com.zhongjh.albumcamerarecorder.settings.api.RecorderSettingApi;

/**
 * 录音机
 */
public final class RecorderSetting implements RecorderSettingApi {

    private final RecordeSpec mRecordeSpec;

    public RecorderSetting() {
        mRecordeSpec = RecordeSpec.getInstance();
    }


}
