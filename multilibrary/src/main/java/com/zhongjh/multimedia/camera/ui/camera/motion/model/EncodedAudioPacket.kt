package com.zhongjh.multimedia.camera.ui.camera.motion.model

/**
 * 单帧编码后的音频数据包
 * 存放一帧AAC压缩音频二进制、时间戳、标记位，供给MediaMuxer写入MP4文件
 * @param data 音频压缩后的二进制字节，一帧AAC音频数据
 * @param presentationTimeUs 这一帧音频的展示时间戳，单位微秒，MP4封装必须依靠这个做时间排序
 * @param flags 数据包标志位，对应MediaCodec.BufferInfo的flags，用来标记关键帧等属性
 */
class EncodedAudioPacket(
    val data: ByteArray,
    val presentationTimeUs: Long,
    val flags: Int
)