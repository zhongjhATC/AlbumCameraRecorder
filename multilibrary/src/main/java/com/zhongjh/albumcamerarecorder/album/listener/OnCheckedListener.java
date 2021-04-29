package com.zhongjh.albumcamerarecorder.album.listener;


/**
 *  when original is enabled , callback immediately when user check or uncheck original.
 * @author zhihu
 */
public interface OnCheckedListener {

    /**
     * 选择了原图事件
     * @param isChecked 是否选择
     */
    void onCheck(boolean isChecked);

}
