package com.zhongjh.common.coordinator

import com.zhongjh.common.listener.VideoEditListener
import java.util.*

/**
 * 视频合并协调者
 *
 * @author zhongjh
 */
interface VideoMergeCoordinator {

    /**
     * 赋值事件
     *
     * @param videoMergeListener 事件
     */
    fun setVideoMergeListener(cls: Class<*>, videoMergeListener: VideoEditListener)

    /**
     * 合并视频
     *
     * @param newPath 合并后的新视频地址
     * @param paths   多个视频的集合
     * @param txtPath 多个视频的集合地址文本，用 ffmpeg 才能合并
     */
    fun merge(cls: Class<*>, newPath: String, paths: ArrayList<String?>, txtPath: String)

    /**
     * 销毁合并事件
     */
    fun onMergeDestroy(cls: Class<*>)

    /**
     * 关闭合并事件，不销毁
     */
    fun onMergeDispose(cls: Class<*>)

}