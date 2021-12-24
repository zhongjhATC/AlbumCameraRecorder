package com.zhongjh.albumcamerarecorder.listener;

import java.util.List;

/**
 * onResult 的事件
 * @author zhongjh
 */
public interface OnResultCallbackListener<T> {
    /**
     * return LocalMedia result
     *
     * @param result
     */
    void onResult(List<T> result);

    /**
     * Cancel
     */
    void onCancel();
}
