package com.zhongjh.multimedia.camera.ui.camera.motion.model

/**
 * 截取视频片段后的返回结果
 * 截取动态照片短视频片段使用，返回生成的临时MP4文件和各项实际参数
 *
 * @param file 裁剪输出的临时MP4文件，存放在app缓存目录，用完之后需要手动删除
 * @param actualStartUs 片段真实的起始时间，单位微秒；因为MP4必须从关键帧开始播放，实际起始时间可能比请求的时间点要靠前
 * @param videoSamplesWritten 成功写入MP4的视频帧数量，可以用来判断有没有截到画面
 * @param audioIncluded 生成出来的这个MP4里面是否带上了音频数据
 * @param hasAudioTrack 原始源视频本身是否存在音频轨道；就算有音轨，也可能因为静音/转码失败导致audioIncluded为false
 */
data class VideoClipResult(
    /** 裁剪好的临时MP4文件，位于应用缓存，使用完毕记得删除释放空间 */
    val file: java.io.File,
    /**
     * 片段真实起始时间，微秒
     * 注意：MP4要求片段开头必须是关键帧，所以实际起点往往会比你传入的startUs更早
     */
    val actualStartUs: Long,
    /** 成功写入MP4的视频帧数量，0代表没有截取到有效画面 */
    val videoSamplesWritten: Int,
    /** true=输出MP4里面包含音频；false=没有声音 */
    val audioIncluded: Boolean,
    /** 原始视频是否存在音频轨道，只是标记源文件状态，不代表最终片段一定有声音 */
    val hasAudioTrack: Boolean = false
)