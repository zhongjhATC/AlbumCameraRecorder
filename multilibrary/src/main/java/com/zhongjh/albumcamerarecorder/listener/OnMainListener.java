package com.zhongjh.albumcamerarecorder.listener;


/**
 * 首页的事件
 * @author zhongjh
 */
public interface OnMainListener {

    /**
     * 指打开该界面产生的逻辑异常信息，目前只有语音上限
     * @param errorMessage 异常信息
     */
    void onOpenFail(String errorMessage);

}
