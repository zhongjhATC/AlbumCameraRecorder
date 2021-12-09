package com.zhongjh.common.listener

/**
 * 视频编辑的回调
 * @author zhongjh
 * @date 2021/12/9
 */
interface VideoEditListener {

    /**
     * 完成
     */
    fun onFinish()

    /**
     * 进度
     * @param progress 进度百分比
     * @param progressTime 进度时间
     */
    fun onProgress(progress: Int, progressTime: Long);

    /**
     * 取消
     */
    fun onCancel();

    /**
     * 异常
     * @param message 信息
     */
    fun onError(message: String);

}