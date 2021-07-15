package com.zhongjh.albumcamerarecorder.data;

import com.zhongjh.albumcamerarecorder.settings.RecordeSpec;

/**
 * 当前选择的图片、视频、音频数量
 *
 * @author zhongjh
 * @date 2021/7/15
 */
public class SelectedCount {

    private static final class InstanceHolder {
        private static final SelectedCount INSTANCE = new SelectedCount();
    }

    public static SelectedCount getInstance() {
        return SelectedCount.InstanceHolder.INSTANCE;
    }

    /**
     * 当前选择的视频数量
     */
    public int selectedVideoCount;
    /**
     * 当前选择的图片数量
     */
    public int selectedImageCount;
    /**
     * 当前选择的音频数量
     */
    public int selectedAudioCount;

}
