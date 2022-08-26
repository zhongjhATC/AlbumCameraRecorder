package com.zhongjh.albumcamerarecorder.camera.ui.camera.impl;

import com.otaliastudios.cameraview.VideoResult;

/**
 * 拍摄界面的有关视频View的接口
 *
 * @author zhongjh
 * @date 2022/8/23
 */
public interface ICameraVideo {

    /**
     * 初始化有关视频的配置数据
     */
    void initData();

    /**
     * 视频编辑后的事件，目前 有分段录制后合并、压缩视频
     */
    void initVideoEditListener();

    /**
     * 生命周期onDestroy
     *
     * @param isCommit 是否提交了数据,如果不是提交则要删除冗余文件
     */
    void onDestroy(boolean isCommit);

    /**
     * 录制视频
     */
    void recordVideo();

    /**
     * 视频录制结束后
     *
     * @param result 视频录制结束后提供的数据源
     */
    void onVideoTaken(VideoResult result);

    /**
     * 删除视频 - 多个模式
     */
    void removeVideoMultiple();

    /**
     * 打开预览视频界面
     */
    void openPreviewVideoActivity();
}
