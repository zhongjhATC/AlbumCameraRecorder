package com.zhongjh.albumcamerarecorder.listener;

import com.zhongjh.common.entity.LocalFile;
import com.zhongjh.common.entity.MultiMedia;

import java.util.List;

/**
 * onResult 的事件
 *
 * @author zhongjh
 */
public interface OnResultCallbackListener {

    /**
     * return LocalMedia result
     *
     * @param result 控件返回的相关数据
     */
    void onResult(List<LocalFile> result);

    /**
     * return LocalMedia result
     *
     * @param result 控件返回的相关数据,跟九宫格挂钩
     * @param apply  是否预览界面点击了同意
     */
    void onResultFromPreview(List<MultiMedia> result, boolean apply);

    /**
     * Cancel
     */
    void onCancel();
}
