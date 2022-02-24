package com.zhongjh.videoedit

import com.zhongjh.common.coordinator.VideoCompressCoordinator
import com.zhongjh.common.listener.VideoEditListener
import io.microshow.rxffmpeg.RxFFmpegInvoke
import java.util.*

/**
 * 视频压缩管理
 *
 * @author zhongjh
 * @date 2022/1/27
 */
class VideoCompressManager : VideoCompressCoordinator {

    private var mMyRxFfmpegCompressSubscriber = HashMap<Class<*>, MyRxFfmpegSubscriber>()
    private var mVideoCompressListener = HashMap<Class<*>, VideoEditListener?>()

    override fun setVideoCompressListener(cls: Class<*>, videoCompressListener: VideoEditListener) {
        mVideoCompressListener[cls] = videoCompressListener
    }

    override fun compressRxJava(cls: Class<*>, oldPath: String, compressPath: String) {
        val commands = "ffmpeg -y -i $oldPath -b 2097k -r 30 -vcodec libx264 -preset superfast $compressPath"
        if (mVideoCompressListener[cls] == null) {
            return
        }
        var myRxFfmpegSubscriber = mMyRxFfmpegCompressSubscriber[cls]
        if (myRxFfmpegSubscriber == null) {
            myRxFfmpegSubscriber = MyRxFfmpegSubscriber(mVideoCompressListener[cls]!!)
        }

        // 开始执行FFmpeg命令
        RxFFmpegInvoke.getInstance()
                .runCommandRxJava(commands.split(" ".toRegex()).toTypedArray())
                .subscribe(myRxFfmpegSubscriber)
    }

    override fun compressAsync(cls: Class<*>, oldPath: String, compressPath: String) {
        val commands = "ffmpeg -y -i $oldPath -b 2097k -r 30 -vcodec libx264 -preset superfast $compressPath"
        if (mVideoCompressListener[cls] == null) {
            return
        }
        var myRxFfmpegSubscriber = mMyRxFfmpegCompressSubscriber[cls]
        if (myRxFfmpegSubscriber == null) {
            myRxFfmpegSubscriber = MyRxFfmpegSubscriber(mVideoCompressListener[cls]!!)
        }

        // 开始执行FFmpeg命令
        RxFFmpegInvoke.getInstance().runCommand(commands.split(" ".toRegex()).toTypedArray(),
                myRxFfmpegSubscriber)
    }

    override fun onCompressDestroy(cls: Class<*>) {
        mMyRxFfmpegCompressSubscriber[cls]?.dispose()
        mVideoCompressListener[cls] = null
    }

    override fun onCompressDispose(cls: Class<*>) {
        mMyRxFfmpegCompressSubscriber[cls]?.dispose()
    }
}