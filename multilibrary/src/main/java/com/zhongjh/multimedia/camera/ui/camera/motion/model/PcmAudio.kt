package com.zhongjh.multimedia.camera.ui.camera.motion.model

/**
 * 解码后16位PCM无损音频缓存实体
 * @param bytes PCM原始二进制
 * @param sampleRate 采样率
 * @param channelCount 声道1/2
 * @param basePtsUs 第一条PCM帧相对剪辑起始偏移时间戳
 */
class PcmAudio(val bytes: ByteArray, val sampleRate: Int, val channelCount: Int, val basePtsUs: Long)