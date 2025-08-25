package com.zhongjh.multimedia.settings.api

import com.zhongjh.multimedia.recorder.BaseSoundRecordingFragment
import com.zhongjh.multimedia.settings.RecorderSetting

/**
 * 有关录音功能的动态设置
 *
 * @author zhongjh
 * @date 2019/3/21
 */
interface RecorderSettingApi {
    /**
     * 赋予自定义的SoundRecordingFragment
     * 如果设置则使用自定义的SoundRecordingFragment,否则使用默认的CameraFragment
     *
     * @param baseSoundRecordingFragment SoundRecordingFragment的基类，必须继承它实现才可设置
     * @return [RecorderSetting] for fluent API.
     */
    fun soundRecordingFragment(baseSoundRecordingFragment: BaseSoundRecordingFragment): RecorderSetting

    /**
     * 最长录制时间,默认1000毫秒
     *
     * @param maxDuration 最长录制时间,单位为毫秒
     * @return [RecorderSetting] for fluent API.
     */
    fun maxDuration(maxDuration: Int): RecorderSetting

    /**
     * 最短录制时间限制，单位为毫秒，即是如果长按在1500毫秒内，都暂时不开启录制
     *
     * @param minDuration 最短录制时间限制，单位为毫秒
     * @return [RecorderSetting] for fluent API.
     */
    fun minDuration(minDuration: Int): RecorderSetting
}