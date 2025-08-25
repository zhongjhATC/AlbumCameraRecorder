package com.zhongjh.multimedia.settings

import com.zhongjh.multimedia.camera.ui.camera.BaseCameraFragment
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraPictureManager
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraVideoManager
import com.zhongjh.multimedia.camera.ui.camera.state.CameraStateManager
import com.zhongjh.multimedia.recorder.BaseSoundRecordingFragment
import com.zhongjh.multimedia.settings.api.RecorderSettingApi

/**
 * 录音机
 * @author zhongjh
 */
class RecorderSetting : RecorderSettingApi {

    private val mRecordeSpec: RecordeSpec = RecordeSpec.cleanInstance

    /**
     * 赋予自定义的SoundRecordingFragment
     * 如果设置则使用自定义的SoundRecordingFragment,否则使用默认的SoundRecordingFragment
     * 每次使用要重新赋值，因为会在每次关闭界面后删除该Fragment
     */
    var baseSoundRecordingFragment: BaseSoundRecordingFragment? = null

    override fun soundRecordingFragment(baseSoundRecordingFragment: BaseSoundRecordingFragment): RecorderSetting {
        this.baseSoundRecordingFragment = baseSoundRecordingFragment
        return this
    }

    override fun maxDuration(maxDuration: Int): RecorderSetting {
        mRecordeSpec.maxDuration = maxDuration
        return this
    }

    override fun minDuration(minDuration: Int): RecorderSetting {
        mRecordeSpec.minDuration = minDuration
        return this
    }

}