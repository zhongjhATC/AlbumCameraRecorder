package com.zhongjh.videoedit;

import com.zhongjh.common.listener.VideoEditListener;

import java.lang.ref.WeakReference;

import io.microshow.rxffmpeg.RxFFmpegSubscriber;

/**
 * 回调事件
 *
 * @author zhongjh
 * @date 2022/1/27
 */
public class MyRxFfmpegSubscriber extends RxFFmpegSubscriber {

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