package com.zhongjh.common.coordinator

import com.zhongjh.common.listener.VideoEditListener

/**
 * 视频压缩协调者
 *
 * @author zhongjh
 */
interface VideoCompressCoordinator {

    /**
     * 赋值事件
     *
     * @param videoCompressListener 事件
     */
    fun setVideoCompressListener(cls: Class<*>, videoCompressListener: VideoEditListener)

    /**
     * 压缩视频
     * 基于RxJava
     *
     * @param oldPath      压缩前的文件地址
     * @param compressPath 压缩后的文件地址
     */
    fun compressRxJava(cls: Class<*>, oldPath: String, compressPath: String)

    /**
     * 压缩视频
     * 同步执行
     *
     * @param oldPath      压缩前的文件地址
     * @param compressPath 压缩后的文件地址
     */
    fun compressAsync(cls: Class<*>, oldPath: String, compressPath: String)

    /**
     * 销毁压缩事件
     */
    fun onCompressDestroy(cls: Class<*>)

    /**
     * 关闭压缩事件，不销毁
     */
    fun onCompressDispose(cls: Class<*>)
}