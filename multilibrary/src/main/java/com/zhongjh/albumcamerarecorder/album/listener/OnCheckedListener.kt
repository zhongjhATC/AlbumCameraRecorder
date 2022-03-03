package com.zhongjh.albumcamerarecorder.album.listener

/**
 * when original is enabled , callback immediately when user check or uncheck original.
 * @author zhihu
 */
interface OnCheckedListener {
    /**
     * 选择了原图事件
     * @param isChecked 是否选择
     */
    fun onCheck(isChecked: Boolean)
}