package com.zhongjh.videoedit

import com.zhongjh.common.coordinator.VideoMergeCoordinator
import com.zhongjh.common.listener.VideoEditListener
import com.zhongjh.common.utils.FileUtils
import io.microshow.rxffmpeg.RxFFmpegInvoke
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

/**
 * 视频合并管理
 *
 * @author zhongjh
 */
class VideoMergeManager : VideoMergeCoordinator {
    private var mMyRxFfmpegMergeSubscriber = HashMap<Class<*>, MyRxFfmpegSubscriber?>()
    private var mVideoMergeListener = HashMap<Class<*>, VideoEditListener>()

    override fun setVideoMergeListener(cls: Class<*>, videoMergeListener: VideoEditListener) {
        mVideoMergeListener[cls] = videoMergeListener
    }

    override fun merge(cls: Class<*>, newPath: String, paths: ArrayList<String?>, txtPath: String) {
        // 创建文本文件
        val file = File(txtPath)
        FileUtils.createOrExistsFile(file)
        if (!file.exists()) {
            return
        }
        if (mVideoMergeListener[cls] == null) {
            return
        }
        var myRxFfmpegSubscriber = mMyRxFfmpegMergeSubscriber[cls]
        if (myRxFfmpegSubscriber == null) {
            myRxFfmpegSubscriber = MyRxFfmpegSubscriber(mVideoMergeListener[cls]!!)
        }
        val stringBuilderFile = StringBuilder()
        for (path in paths) {
            stringBuilderFile.append("file ").append("'").append(path).append("'").append("\r\n")
        }
        val outStream: FileOutputStream
        try {
            outStream = FileOutputStream(file)
            outStream.write(stringBuilderFile.toString().toByteArray())
            outStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val commands = "ffmpeg -y -f concat -safe 0 -i " + file.path + " -c copy " + newPath

        // 开始执行FFmpeg命令
        RxFFmpegInvoke.getInstance()
                .runCommandRxJava(commands.split(" ".toRegex()).toTypedArray())
                .subscribe(myRxFfmpegSubscriber)
    }

    override fun onMergeDestroy(cls: Class<*>) {
        mMyRxFfmpegMergeSubscriber[cls]?.dispose()
        mMyRxFfmpegMergeSubscriber[cls] = null
    }

    override fun onMergeDispose(cls: Class<*>) {
        mMyRxFfmpegMergeSubscriber[cls]?.dispose()
    }
}