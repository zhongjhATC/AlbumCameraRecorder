package com.zhongjh.albumcamerarecorder.listener

import com.zhongjh.common.entity.LocalFile
import com.zhongjh.common.entity.MultiMedia

/**
 * onResult 的事件
 *
 * @author zhongjh
 */
interface OnResultCallbackListener {

    /**
     * return LocalMedia result
     *
     * @param result 控件返回的相关数据
     */
    fun onResult(result: List<LocalFile>)

    /**
     * return LocalMedia result
     *
     * @param result 控件返回的相关数据,跟九宫格挂钩
     * @param apply  是否预览界面点击了同意
     */
    fun onResultFromPreview(result: List<MultiMedia>, apply: Boolean)
}