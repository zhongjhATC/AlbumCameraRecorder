package com.zhongjh.albumcamerarecorder.settings.api;

import com.zhongjh.albumcamerarecorder.settings.RecorderSetting;

/**
 * 有关录音功能的动态设置
 * Created by zhongjh on 2019/3/21.
 */
public interface RecorderSettingApi {

    /**
     * 最长录制时间,默认10秒
     *
     * @param duration 最长录制时间,单位为秒
     * @return {@link RecorderSetting} for fluent API.
     */
    RecorderSetting duration(int duration);

    /**
     * 最短录制时间限制，单位为毫秒，即是如果长按在1500毫秒内，都暂时不开启录制
     *
     * @param minDuration 最短录制时间限制，单位为毫秒
     * @return {@link RecorderSetting} for fluent API.
     */
    RecorderSetting minDuration(int minDuration);

}
