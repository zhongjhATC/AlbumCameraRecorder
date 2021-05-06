package gaode.zhongjh.com.common.coordinator;

import java.util.ArrayList;

import gaode.zhongjh.com.common.listener.VideoEditListener;

/**
 * 视频编辑协调者
 * @author zhongjh
 */
public class VideoEditCoordinator {

    /**
     * 用于子类继承
     */
    protected VideoEditCoordinator mVideoEditManager;
    /**
     * 事件回调
     */
    protected VideoEditListener mVideoEditListener;

    public void setVideoEditListener(VideoEditListener videoEditListener) {
        mVideoEditListener = videoEditListener;
    }

    /**
     * 合并视频
     *
     * @param newPath 合并后的新视频地址
     * @param paths   多个视频的集合
     * @param txtPath 多个视频的集合地址文本，用 ffmpeg 才能合并
     */
    public void merge(String newPath, ArrayList<String> paths, String txtPath) {
        mVideoEditManager.merge(newPath, paths, txtPath);
    }

    /**
     * 销毁
     */
    public void onDestroy() {
        mVideoEditManager.onDestroy();
    }

}
