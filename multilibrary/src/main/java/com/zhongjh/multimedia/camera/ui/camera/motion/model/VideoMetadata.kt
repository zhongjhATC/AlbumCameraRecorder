package com.zhongjh.multimedia.camera.ui.camera.motion.model

/**
 * 视频元数据实体，导出图片时需要保留的各类拍摄信息
 * 这些字段后续会写入EXIF和XMP标签
 */
data class VideoMetadata(
    /** 原始拍摄时间，ISO‑8601标准时间字符串，例如：2026‑08‑17T14:30:00+08:00 */
    val dateTime: String? = null,
    /** GPS纬度，为空代表没有定位信息 */
    val latitude: Double? = null,
    /** GPS经度 */
    val longitude: Double? = null,
    /** GPS海拔，单位米 */
    val altitude: Double? = null,
    /** 设备厂商，例如Xiaomi、HUAWEI */
    val make: String? = null,
    /** 设备型号，例如 Mi‑14 */
    val model: String? = null,
    /** ISO感光度 */
    val iso: Int? = null,
    /** 曝光时间，字符串格式，例如 "1/60"、"1/1000" */
    val exposureTime: String? = null,
    /** 光圈F值，例如1.8、2.4 */
    val fNumber: Double? = null,
    /** 焦距，单位毫米 */
    val focalLength: Double? = null,
    /** 视频画面旋转角度：0 / 90 / 180 / 270 */
    val rotation: Int = 0,
    /** 原始视频像素宽度 */
    val videoWidth: Int = 0,
    /** 原始视频像素高度 */
    val videoHeight: Int = 0,
    /** 视频帧率，例如30.0f、60.0f */
    val frameRate: Float = 0f,
    /** 视频码率，单位bit/s */
    val bitrate: Long = 0,
    /** 视频编码格式名称，例如 "h264"、"h265" */
    val codec: String? = null,
    /** 视频总时长，单位毫秒 */
    val durationMs: Long = 0,
    /** 源视频原始文件名 */
    val sourceFileName: String? = null
)