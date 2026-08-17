package com.zhongjh.multimedia.camera.ui.camera.motion.model

/**
 * 导出支持的图片格式枚举
 * 保存文件后缀、MIME类型、能力标记（是否支持透明通道、是否支持HDR）
 */
enum class ExportFormat(
    val extension: String,      // 文件后缀名，不带点，例如 jpg
    val mimeType: String,       // 文件MIME类型，系统SAF、MediaStore创建文件要用到
    val supportsAlpha: Boolean = false, // 是否支持透明通道
    val supportsHdr: Boolean = false    // 是否原生支持存储HDR图像
) {
    JPEG(
        extension = "jpg",
        mimeType = "image/jpeg",
        supportsAlpha = false,   // jpeg不支持透明
        supportsHdr = false      // 普通jpeg存不了HDR，HDR必须转SDR再导出
    )
}