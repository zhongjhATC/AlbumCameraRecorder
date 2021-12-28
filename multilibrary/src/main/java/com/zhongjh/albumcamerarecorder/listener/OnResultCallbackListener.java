package com.zhongjh.albumcamerarecorder.listener;

import java.util.List;

/**
 * onResult 的事件
 *
 * @author zhongjh
 */
public interface OnResultCallbackListener<T> {

    /**
     * return LocalMedia result
     *
     * @param result      控件返回的相关数据
     * @param fromPreview 是否来自预览界面
     */
    void onResult(List<T> result, boolean fromPreview);

    /**
     * Cancel
     */
    void onCancel();
}
