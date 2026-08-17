package com.zhongjh.multimedia.camera.ui.camera.motion.model

/**
 * 代表从视频截取出来的一帧画面，把画面相关全部信息封装在一起
 * 包含：帧时间戳、画面尺寸、色彩空间、EXIF元数据、导出格式、质量参数
 */
data class CapturedFrame(
    /**
     * 该帧在原视频里面的时间戳，单位：微秒
     */
    val timestampUs: Long,
    /**
     * 这一帧画面的像素宽度
     */
    val width: Int,
    /**
     * 这一帧画面的像素高度
     */
    val height: Int,
    /**
     * 色彩空间信息，标记是不是HDR高动态画面
     */
    val colorSpace: ColorSpaceInfo,
    /**
     * 图片/视频元数据：拍摄时间、设备型号、地理位置等EXIF信息
     */
    val metadata: VideoMetadata,
    /**
     * 默认导出文件格式，默认JPEG
     */
    val format: ExportFormat = ExportFormat.JPEG,
    /**
     * 导出压缩质量，0~100，100质量最好，默认100
     */
    val quality: Int = 100
)
