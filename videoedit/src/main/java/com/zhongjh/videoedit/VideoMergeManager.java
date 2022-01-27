package com.zhongjh.videoedit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.zhongjh.common.coordinator.VideoMergeCoordinator;
import com.zhongjh.common.listener.VideoEditListener;

import org.jetbrains.annotations.NotNull;

import io.microshow.rxffmpeg.RxFFmpegInvoke;

/**
 * 视频编辑管理
 *
 * @author zhongjh
 */
public class VideoMergeManager implements VideoMergeCoordinator {

    HashMap<Class<?>, MyRxFfmpegSubscriber> mMyRxFfmpegMergeSubscriber = new HashMap<>();
    HashMap<Class<?>, VideoEditListener> mVideoMergeListener = new HashMap<>();

    @Override
    public void setVideoMergeListener(@NotNull Class<?> cls, @NotNull VideoEditListener videoMergeListener) {
        mVideoMergeListener.put(cls, videoMergeListener);
    }

    @Override
    public void merge(@NotNull Class<?> cls, String newPath,@NotNull  ArrayList<String> paths,@NotNull String txtPath) {
        boolean isMerge = false;
        // 创建文本文件
        File file = new File(txtPath);
        if (!file.exists()) {
            if (file.getParent() != null) {
                File dir = new File(file.getParent());
                // 判断父目录是否存在
                if (!dir.exists()) {
                    isMerge = dir.mkdirs();
                    if (isMerge) {
                        try {
                            isMerge = file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    // 如果存在直接创建文件
                    try {
                        isMerge = file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        MyRxFfmpegSubscriber myRxFfmpegSubscriber = mMyRxFfmpegMergeSubscriber.get(cls);
        if (myRxFfmpegSubscriber == null) {
            myRxFfmpegSubscriber = new MyRxFfmpegSubscriber(mVideoMergeListener.get(cls));
        }

        if (!isMerge && !file.exists()) {
            return;
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


        // 开始执行FFmpeg命令
        RxFFmpegInvoke.getInstance()
                .runCommandRxJava(commands.split(" "))
                .subscribe(myRxFfmpegSubscriber);
    }

    @Override
    public void onMergeDestroy(@NotNull Class<?> cls) {
        if (mMyRxFfmpegMergeSubscriber.get(cls) != null) {
            mMyRxFfmpegMergeSubscriber.get(cls).dispose();
            mMyRxFfmpegMergeSubscriber.put(cls, null);
        }
    }

    @Override
    public void onMergeDispose(@NotNull Class<?> cls) {
        if (mMyRxFfmpegMergeSubscriber.get(cls) != null) {
            mMyRxFfmpegMergeSubscriber.get(cls).dispose();
        }
    }


}
