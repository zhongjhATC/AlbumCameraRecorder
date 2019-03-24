package com.zhongjh.albumcamerarecorder.utils.constants;

public class Constant {

    public static final int REQUEST_CODE_PREVIEW = 23;     // 相册的预览
    private static final int REQUEST_CODE_CAPTURE = 24;     // 相册的拍照（作废）
    public static final int REQUEST_CODE_PREVIEW_CAMRRA = 25;     // 录制的预览

    public static final String EXTRA_MULTIMEDIA_TYPES = "extra_multimedia_types";               // 返回的多媒体类型

    public static final String EXTRA_RESULT_SELECTION = "extra_result_selection";               // Uri的数据
    public static final String EXTRA_RESULT_SELECTION_PATH = "extra_result_selection_path";     // path的数据
    public static final String EXTRA_RESULT_FIRST_FRAME = "extra_result_first_frame";           // 录像的第一帧图片
    public static final String EXTRA_RESULT_RECORDING_ITEM = "extra_result_recording_item";     // 录音的对象数据
}
