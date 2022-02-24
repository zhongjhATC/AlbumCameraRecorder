package com.zhongjh.videoedit

import com.zhongjh.common.listener.VideoEditListener
import io.microshow.rxffmpeg.RxFFmpegSubscriber
import java.lang.ref.WeakReference

/**
 * 回调事件
 *
 * @author zhongjh
 * @date 2022/1/27
 */
class MyRxFfmpegSubscriber(videoEditListener: VideoEditListener) : RxFFmpegSubscriber() {

    private val mWeakReference: WeakReference<VideoEditListener> = WeakReference(videoEditListener)

    override fun onFinish() {
        val mVideoEditListener = mWeakReference.get()
        mVideoEditListener?.onFinish()
    }

    override fun onProgress(progress: Int, progressTime: Long) {
        val mVideoEditListener = mWeakReference.get()
        mVideoEditListener?.onProgress(progress, progressTime)
    }

    override fun onCancel() {
        val mVideoEditListener = mWeakReference.get()
        mVideoEditListener?.onCancel()
    }

    override fun onError(message: String) {
        val mVideoEditListener = mWeakReference.get()
        mVideoEditListener?.onError(message)
    }

}