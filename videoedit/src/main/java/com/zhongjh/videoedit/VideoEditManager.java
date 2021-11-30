package com.zhongjh.videoedit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import gaode.zhongjh.com.common.coordinator.VideoEditCoordinator;
import gaode.zhongjh.com.common.listener.VideoEditListener;
import io.microshow.rxffmpeg.RxFFmpegInvoke;
import io.microshow.rxffmpeg.RxFFmpegSubscriber;

/**
 * 视频编辑管理
 * @author zhongjh
 */
public class VideoEditManager extends VideoEditCoordinator {

    MyRxFfmpegSubscriber mMyRxFfmpegMergeSubscriber;
    MyRxFfmpegSubscriber mMyRxFfmpegCompressSubscriber;

    @Override
    public void merge(String newPath, ArrayList<String> paths,String txtPath) {
        // 创建文本文件
        File file = new File(txtPath);
        if (!file.exists()) {
            File dir = new File(file.getParent());
            dir.mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        StringBuilder stringBuilderFile = new StringBuilder();
        for (String path : paths) {
            stringBuilderFile.append("file ").append("'").append(path).append("'").append("\r\n");
        }

        FileOutputStream outStream;
        try {
            outStream = new FileOutputStream(file);
            outStream.write(stringBuilderFile.toString().getBytes());
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String commands = "ffmpeg -y -f concat -safe 0 -i " + file.getPath() + " -c copy " + newPath;

        mMyRxFfmpegMergeSubscriber = new MyRxFfmpegSubscriber(mVideoMergeListener);

        // 开始执行FFmpeg命令
        RxFFmpegInvoke.getInstance()
                .runCommandRxJava(commands.split(" "))
                .subscribe(mMyRxFfmpegMergeSubscriber);
    }

    @Override
    public void compress(String oldPath,String compressPath) {
        String commands = "ffmpeg -y -i " + oldPath + " -b 2097k -r 30 -vcodec libx264 -preset superfast " + compressPath;

        mMyRxFfmpegCompressSubscriber = new MyRxFfmpegSubscriber(mVideoCompressListener);

        // 开始执行FFmpeg命令
        RxFFmpegInvoke.getInstance()
                .runCommandRxJava(commands.split(" "))
                .subscribe(mMyRxFfmpegCompressSubscriber);
    }

    @Override
    public void onCompressDestroy() {
        if (mMyRxFfmpegCompressSubscriber != null) {
            mMyRxFfmpegCompressSubscriber.dispose();
            mVideoCompressListener = null;
        }
    }

    @Override
    public void onMergeDestroy() {
        if (mMyRxFfmpegMergeSubscriber != null) {
            mMyRxFfmpegMergeSubscriber.dispose();
            mVideoMergeListener = null;
        }
    }

    @Override
    public void onMergeDispose() {
        if (mMyRxFfmpegMergeSubscriber != null) {
            mMyRxFfmpegMergeSubscriber.dispose();
        }
    }

    @Override
    public void onCompressDispose() {
        if (mMyRxFfmpegCompressSubscriber != null) {
            mMyRxFfmpegCompressSubscriber.dispose();
        }
    }



    public static class MyRxFfmpegSubscriber extends RxFFmpegSubscriber {

        private final WeakReference<VideoEditListener> mWeakReference;

        public MyRxFfmpegSubscriber(VideoEditListener videoEditListener) {
            mWeakReference = new WeakReference<>(videoEditListener);
        }

        @Override
        public void onFinish() {
            final VideoEditListener mVideoEditListener = mWeakReference.get();
            if (mVideoEditListener != null) {
                mVideoEditListener.onFinish();
            }
        }

        @Override
        public void onProgress(int progress, long progressTime) {
            final VideoEditListener mVideoEditListener = mWeakReference.get();
            if (mVideoEditListener != null) {
                mVideoEditListener.onProgress(progress, progressTime);
            }
        }

        @Override
        public void onCancel() {
            final VideoEditListener mVideoEditListener = mWeakReference.get();
            if (mVideoEditListener != null) {
                mVideoEditListener.onCancel();
            }
        }

        @Override
        public void onError(String message) {
            final VideoEditListener mVideoEditListener = mWeakReference.get();
            if (mVideoEditListener != null) {
                mVideoEditListener.onError(message);
            }
        }
    }

}
