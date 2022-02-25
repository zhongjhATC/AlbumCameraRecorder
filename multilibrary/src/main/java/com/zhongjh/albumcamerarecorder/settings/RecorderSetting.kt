package com.zhongjh.albumcamerarecorder.settings

import com.zhongjh.albumcamerarecorder.settings.api.RecorderSettingApi

/**
 * 录音机
 * @author zhongjh
 */
class RecorderSetting : RecorderSettingApi {

    private val mRecordeSpec: RecordeSpec = RecordeSpec.cleanInstance

    override fun duration(duration: Int): RecorderSetting {
        mRecordeSpec.duration = duration
        return this
    }

    override fun minDuration(minDuration: Int): RecorderSetting {
        mRecordeSpec.minDuration = minDuration
        return this
    }

}