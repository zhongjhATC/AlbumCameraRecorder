package com.zhongjh.albumcamerarecorder.constants;

/**
 * @author zhongjh
 */
public class Constant {

    /**
     * 录制的预览
     */
    public static final int REQUEST_CODE_PREVIEW_CAMRRA = 25;
    /**
     * 合成视频录制的预览
     */
    public static final int REQUEST_CODE_PREVIEW_VIDEO = 26;

    /**
     * 返回的多媒体类型
     */
    public static final String EXTRA_MULTIMEDIA_TYPES = "extra_multimedia_types";
    /**
     * 标记是否通过相册选择获取的
     */
    public static final String EXTRA_MULTIMEDIA_CHOICE = "extra_multimedia_choice";
    /**
     * Uri的数据
     */
    public static final String EXTRA_RESULT_SELECTION = "extra_result_selection";
    /**
     * path的数据
     */
    public static final String EXTRA_RESULT_SELECTION_PATH = "extra_result_selection_path";
    /**
     * 录像的第一帧图片
     */
    public static final String EXTRA_RESULT_FIRST_FRAME = "extra_result_first_frame";
    /**
     * 录音的对象数据
     */
    public static final String EXTRA_RESULT_RECORDING_ITEM = "extra_result_recording_item";
}
