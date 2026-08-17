package com.zhongjh.multimedia.camera.ui.camera.motion

import android.content.Context
import androidx.exifinterface.media.ExifInterface
import com.zhongjh.common.utils.LogUtil
import com.zhongjh.multimedia.camera.ui.camera.motion.model.VideoMetadata
import com.zhongjh.multimedia.camera.ui.camera.motion.utils.DateTimeUtils
import com.zhongjh.multimedia.camera.ui.camera.motion.utils.ExifUtils
import java.io.File

/**
 * 视频元数据写入工具类
 * 核心作用：将原视频的拍摄信息写入导出后的静态图片EXIF与XMP元数据中
 * 兼容安卓Motion Photo动态照片规范，相册可识别为实况动态照片依赖此元数据体系
 * 可保留的原始视频信息包含：
 * 1. 视频拍摄时间、原始创建时间
 * 2. GPS经纬度、海拔地理位置信息
 * 3. 拍摄设备品牌、机型
 * 4. 相机参数：ISO感光度、曝光时长、光圈值、焦距
 * 5. 视频编码、帧率、分辨率等自定义备注信息
 */
object MetadataWriter {

    /**
     * 对外暴露入口方法：将完整视频元数据写入图片文件的EXIF标签
     * @param context 上下文对象，用于日志打印
     * @param imageFile 已经生成好的JPG静态底图文件（动态照片的载体图片）
     * @param metadata 解析出来的原视频所有元数据封装实体
     */
    fun writeMetadata(context: Context, imageFile: File, metadata: VideoMetadata) {
        try {
            // 打开图片EXIF编辑对象
            val exif = ExifInterface(imageFile)
            // 填充所有EXIF字段核心逻辑
            writeExifData(exif, metadata)
            // 持久化保存修改到图片文件中（必须调用，否则写入不生效）
            exif.saveAttributes()
        } catch (e: Exception) {
            // 元数据写入失败仅打印警告日志，不中断整个动态照片导出流程（容错设计）
            LogUtil.e("MetadataWriter", "写入图片EXIF元数据发生异常", e)
        }
    }

    /**
     * 内部核心填充逻辑：给已初始化的ExifInterface对象逐条赋值元数据
     * 抽离为独立函数复用：既支持本地File文件写入，也支持FrameExporter中URI/字节流内存写入场景
     * 重要约定：调用方执行完本方法后，必须手动调用 exif.saveAttributes() 保存改动
     * @param exif 已打开的EXIF操作实例
     * @param metadata 原视频元数据实体类
     */
    internal fun writeExifData(exif: ExifInterface, metadata: VideoMetadata) {
        // ====================== 1. 拍摄时间戳写入 ======================
        metadata.dateTime?.let { dateTime ->
            // 将原始时间字符串转换成EXIF标准格式（yyyy:MM:dd HH:mm:ss）
            val exifDate = DateTimeUtils.convertToExif(dateTime)
            exifDate?.let {
                // 三个时间标签统一赋值：修改时间、原始拍摄时间、数字化录入时间
                exif.setAttribute(ExifInterface.TAG_DATETIME, it)
                exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, it)
                exif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, it)
            }
        }

        // ====================== 2. GPS经纬度地理位置 ======================
        val latitude = metadata.latitude
        val longitude = metadata.longitude
        if (latitude != null && longitude != null) {
            // EXIF内置方法直接写入经纬度，自动换算度分秒格式存储
            exif.setLatLong(latitude, longitude)
        }

        // ====================== 3. GPS海拔高度 ======================
        metadata.altitude?.let { alt ->
            // 海拔数值转为EXIF有理数格式存储
            exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, ExifUtils.formatRational(alt))
            // 海拔基准标识：0=海平面以上，1=海平面以下
            exif.setAttribute(
                ExifInterface.TAG_GPS_ALTITUDE_REF,
                if (alt >= 0) "0" else "1"
            )
        }

        // ====================== 4. 拍摄设备品牌与型号 ======================
        metadata.make?.let {
            // 设备厂商（如Xiaomi、HUAWEI、SAMSUNG），先做字符串清洗再写入
            exif.setAttribute(ExifInterface.TAG_MAKE, sanitizeMetadataString(it))
        }
        metadata.model?.let {
            // 设备具体机型（如Mate 60 Pro、iPhone 15）
            exif.setAttribute(ExifInterface.TAG_MODEL, sanitizeMetadataString(it))
        }

        // ====================== 5. 相机拍摄硬件参数 ======================
        // ISO感光度（使用新版标准标签，废弃旧版Deprecated标签）
        metadata.iso?.let {
            exif.setAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY, it.toString())
        }
        // 曝光时间（如1/100s），标准化为EXIF分数格式
        metadata.exposureTime?.let { raw ->
            val normalized = ExifUtils.normalizeExposureTime(raw)
            if (normalized != null) {
                exif.setAttribute(ExifInterface.TAG_EXPOSURE_TIME, normalized)
            }
        }
        // 光圈F值（如F1.8、F2.4）
        metadata.fNumber?.let {
            exif.setAttribute(ExifInterface.TAG_F_NUMBER, it.toString())
        }
        // 镜头焦距，转为EXIF有理数格式
        metadata.focalLength?.let {
            exif.setAttribute(ExifInterface.TAG_FOCAL_LENGTH, ExifUtils.formatRational(it))
        }

        // ====================== 6. 图片实际宽高（根据旋转角度修正） ======================
        // 视频经过旋转90/270度后，宽高需要互换，保证相册显示尺寸正确
        val effectiveWidth: Int
        val effectiveHeight: Int
        if (metadata.rotation == 90 || metadata.rotation == 270) {
            effectiveWidth = metadata.videoHeight
            effectiveHeight = metadata.videoWidth
        } else {
            effectiveWidth = metadata.videoWidth
            effectiveHeight = metadata.videoHeight
        }
        // 写入EXIF图片宽高标签
        if (effectiveWidth > 0) {
            exif.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, effectiveWidth.toString())
        }
        if (effectiveHeight > 0) {
            exif.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, effectiveHeight.toString())
        }

        // ====================== 7. 软件生成标识 ======================
        // 标记这张图片由FrameEcho工具导出生成
        exif.setAttribute(ExifInterface.TAG_SOFTWARE, "FrameEcho")

        // ====================== 8. 自定义用户备注 ======================
        // 拼接视频来源、编码格式、帧率等附加信息存入备注字段
        val comment = buildUserComment(metadata)
        exif.setAttribute(ExifInterface.TAG_USER_COMMENT, comment)
    }

    /**
     * 拼接图片备注说明文本
     * 记录导出来源、原视频文件名、编码格式、帧率等附加信息
     * 内部方法，仅单元测试与本类调用
     */
    internal fun buildUserComment(metadata: VideoMetadata): String {
        return buildString {
            append("来源：视频帧截取生成")
            metadata.sourceFileName?.let { append("; 原文件名称：${sanitizeMetadataString(it)}") }
            metadata.codec?.let { append("; 视频编码：${sanitizeMetadataString(it)}") }
            metadata.frameRate.takeIf { it > 0 }?.let { append("; 原视频帧率：$it FPS") }
        }
    }

    /**
     * 元数据字符串安全清洗工具
     * 防止特殊控制字符、换行符、超长文本破坏EXIF结构导致图片损坏
     * 处理规则：
     * 1. 过滤不可见控制字符、删除DEL符号
     * 2. 去除首尾空格
     * 3. 限制最大长度100字符，避免EXIF字段溢出
     * @param value 原始待清洗字符串
     * @return 安全合规的字符串
     */
    private fun sanitizeMetadataString(value: String): String {
        return value
            // 只保留可打印字符，剔除ASCII控制符
            .filter { it >= ' ' && it != '\u007F' }
            .trim()
            .take(100) // 截断上限100个字符
    }
}