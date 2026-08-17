package com.zhongjh.multimedia.camera.ui.camera.motion.model

/**
 * 导出操作的结果密封类，只有两种情况：导出成功 Success / 导出失败 Error
 * 上层调用 exportStaticFrame / exportMotionPhoto 拿到这个对象，用来判断结果、更新UI提示
 */
sealed class ExportResult {

    /**
     * 导出成功的结果实体
     * @param outputPath 输出文件的Uri字符串
     * @param width 导出图片的像素宽
     * @param height 导出图片的像素高
     * @param fileSizeBytes 导出文件字节大小
     * @param format 文件实际最终格式（动态照片强制JPEG，不一定等于用户选择的格式）
     * @param isMotionPhoto true代表这是Google动态照片（JPEG尾部拼接MP4）
     * @param metadataPreserved true代表EXIF元数据写入成功
     * @param requestedFormat 用户原本选择的导出格式；只有实际格式和用户选的不一样才赋值，例如用户选PNG，但动态照片只能输出JPEG
     * @param audioDropped true：用户没有开启静音，但音频最终丢失了；比如原音频编码不支持，无法嵌入MP4，UI可以用来给用户弹窗提示警告
     */
    data class Success(
        val outputPath: String,
        val width: Int,
        val height: Int,
        val fileSizeBytes: Long,
        /** 文件实际导出格式 */
        val format: ExportFormat,
        val isMotionPhoto: Boolean = false,
        val metadataPreserved: Boolean = false,
        /**
         * 用户原本想要的导出格式
         * 只有实际format和用户选择不一致才不为null；
         * 举例：用户选PNG，但是动态照片强制输出JPEG，这里就记录用户原本选的PNG
         */
        val requestedFormat: ExportFormat? = null,
        /**
         * 音频被丢弃标记：用户没有要求静音，但音频没能放进动态照片MP4片段
         * 比如原音频格式不支持转码封装，UI可以根据这个字段提醒用户“音频未能保存”
         */
        val audioDropped: Boolean = false
    ) : ExportResult() {
        /**
         * 是否发生格式降级/回退
         * true = 用户想要的格式和最终产出格式不一样，例如想要PNG，实际输出JPEG
         */
        val formatFallbackOccurred: Boolean
            get() = requestedFormat != null && requestedFormat != format
    }

    /**
     * 导出失败结果
     * @param message 给UI展示的错误提示文字
     * @param cause 原始异常，用于日志打印排查问题
     */
    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : ExportResult()
}