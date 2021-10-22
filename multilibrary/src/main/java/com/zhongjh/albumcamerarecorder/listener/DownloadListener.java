package com.zhongjh.albumcamerarecorder.listener;

/**
 * 下载接口
 * @author zhongjh
 * @date 2021/10/22
 */
public interface DownloadListener {

    /**
     * 下载开始
     */
    void onStart();

    /**
     * 下载进度
     * @param progress 进度
     */
    void onProgress(int progress);

    /**
     * 下载完成
     * @param path 路径
     */
    void onFinish(String path);

    /**
     * 下载失败
     * @param errorInfo 异常信息
     */
    void onFail(String errorInfo);

}
