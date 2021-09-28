package gaode.zhongjh.com.common.coordinator;

import java.util.ArrayList;

import gaode.zhongjh.com.common.listener.VideoEditListener;

/**
 * 视频编辑协调者
 *
 * @author zhongjh
 */
public class VideoEditCoordinator {

    /**
     * 用于子类继承
     */
    protected VideoEditCoordinator mVideoEditManager;
    /**
     * 合并事件回调
     */
    protected VideoEditListener mVideoMergeListener;
    /**
     * 压缩事件回调
     */
    protected VideoEditListener mVideoCompressListener;

    public void setVideoMergeListener(VideoEditListener videoMergeListener) {
        mVideoMergeListener = videoMergeListener;
    }

    public void setVideoCompressListener(VideoEditListener videoCompressListener) {
        mVideoCompressListener = videoCompressListener;
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
     * 压缩视频
     *
     * @param oldPath      压缩前的文件地址
     * @param compressPath 压缩后的文件地址
     */
    public void compress(String oldPath, String compressPath) {
        mVideoEditManager.compress(oldPath, compressPath);
    }

    /**
     * 销毁合并事件
     */
    public void onMergeDestroy() {
        mVideoEditManager.onMergeDestroy();
    }

    /**
     * 销毁压缩事件
     */
    public void onCompressDestroy() {
        mVideoEditManager.onCompressDestroy();
    }

}
