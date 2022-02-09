package com.zhongjh.videoedit;

import com.zhongjh.common.coordinator.VideoCompressCoordinator;
import com.zhongjh.common.listener.VideoEditListener;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import io.microshow.rxffmpeg.RxFFmpegInvoke;

/**
 * 视频压缩管理
 *
 * @author zhongjh
 * @date 2022/1/27
 */
public class VideoCompressManager implements VideoCompressCoordinator {

    HashMap<Class<?>, MyRxFfmpegSubscriber> mMyRxFfmpegCompressSubscriber = new HashMap<>();
    HashMap<Class<?>, VideoEditListener> mVideoCompressListener = new HashMap<>();

    @Override
    public void setVideoCompressListener(@NotNull Class<?> clsKey, @NotNull VideoEditListener videoCompressListener) {
        mVideoCompressListener.put(clsKey, videoCompressListener);
    }

    @Override
    public void compressRxJava(@NotNull Class<?> cls, @NotNull String oldPath, @NotNull String compressPath) {
        String commands = "ffmpeg -y -i " + oldPath + " -b 2097k -r 30 -vcodec libx264 -preset superfast " + compressPath;

        MyRxFfmpegSubscriber myRxFfmpegSubscriber = mMyRxFfmpegCompressSubscriber.get(cls);
        if (myRxFfmpegSubscriber == null) {
            myRxFfmpegSubscriber = new MyRxFfmpegSubscriber(mVideoCompressListener.get(cls));
        }

        // 开始执行FFmpeg命令
        RxFFmpegInvoke.getInstance()
                .runCommandRxJava(commands.split(" "))
                .subscribe(myRxFfmpegSubscriber);
    }

    @Override
    public void compressAsync(@NotNull Class<?> cls, @NotNull String oldPath, @NotNull String compressPath) {
        String commands = "ffmpeg -y -i " + oldPath + " -b 2097k -r 30 -vcodec libx264 -preset superfast " + compressPath;

        MyRxFfmpegSubscriber myRxFfmpegSubscriber = mMyRxFfmpegCompressSubscriber.get(cls);
        if (myRxFfmpegSubscriber == null) {
            myRxFfmpegSubscriber = new MyRxFfmpegSubscriber(mVideoCompressListener.get(cls));
        }

        // 开始执行FFmpeg命令
        RxFFmpegInvoke.getInstance().
                runCommand(commands.split(" "),
                        myRxFfmpegSubscriber);
    }

    @Override
    public void onCompressDestroy(@NotNull Class<?> cls) {
        if (mMyRxFfmpegCompressSubscriber.get(cls) != null) {
            mMyRxFfmpegCompressSubscriber.get(cls).dispose();
            mVideoCompressListener.put(cls, null);
        }
    }

    @Override
    public void onCompressDispose(@NotNull Class<?> cls) {
        if (mMyRxFfmpegCompressSubscriber.get(cls) != null) {
            mMyRxFfmpegCompressSubscriber.get(cls).dispose();
        }
    }


}
