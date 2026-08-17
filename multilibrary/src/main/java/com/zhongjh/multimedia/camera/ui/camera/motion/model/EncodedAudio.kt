package com.zhongjh.multimedia.camera.ui.camera.motion.model

import android.media.MediaFormat

/**
 * 编码完成后的音频数据封装实体
 * 保存音频编码参数 + 一整批音频数据包，用于给Muxer混流合成MP4文件
 * @param format 音频编码格式信息：采样率、声道、编码类型(AAC)等，来自MediaFormat
 * @param packets 编码后的音频数据包集合，每一个EncodedAudioPacket代表一帧音频压缩数据
 */
class EncodedAudio(
    val format: MediaFormat,
    val packets: List<EncodedAudioPacket>
)