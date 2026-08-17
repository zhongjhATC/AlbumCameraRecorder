package com.zhongjh.multimedia.camera.ui.camera.motion.utils

import kotlin.math.roundToInt

/**
 * EXIF 元数据格式化工具类
 * 统一封装图片/视频EXIF标签有理数格式化、曝光时间标准化等通用逻辑
 */
object ExifUtils {

    /**
     * 将Double数值格式化为EXIF规范要求的有理数分数字符串（分子/分母）
     * EXIF规范中部分参数必须以有理数形式存储，固定分母为10000保证格式统一合法
     * @param value 原始浮点数值
     * @return EXIF有理数格式字符串 示例：传入12.5 → 返回 "125000/10000"
     */
    fun formatRational(value: Double): String {
        // 取绝对值计算，符号交由EXIF其他字段控制
        val absValue = kotlin.math.abs(value)
        // 乘以固定分母10000后四舍五入得到分子
        val numerator = (absValue * 10000).roundToInt()
        return "$numerator/10000"
    }

    /**
     * 规范化曝光时间字符串，转为EXIF可直接写入的标准分式格式
     * 处理规则：
     * 1. null或空字符串 → 直接返回null
     * 2. 已经是分式格式（如1/60）：校验分子分母为正整数，合法则原样返回，否则返回null
     * 3. 小数秒格式（如0.016）：自动换算为最接近的1/N分式，例0.016 → 1/63
     * 4. 数值≥1秒：直接返回小数字符串（如2.0）
     * 5. 非正数、无法解析、无穷大/NAN → 返回null
     * @param exposure 原始曝光时间字符串
     * @return 合规EXIF曝光时间字符串，非法输入返回null
     */
    fun normalizeExposureTime(exposure: String?): String? {
        // 空值、空白字符串直接拦截返回null
        val raw = exposure?.trim()?.takeIf { it.isNotEmpty() } ?: return null

        // 场景1：本身已经是 a/b 分式结构，做合法性校验
        if (raw.contains('/')) {
            val parts = raw.split('/')
            if (parts.size == 2) {
                val num = parts[0].toLongOrNull()
                val den = parts[1].toLongOrNull()
                // 分子分母必须为大于0的正整数才算有效
                if (num != null && den != null && num > 0 && den > 0) {
                    return raw
                }
            }
            // 分式格式不合法直接返回null
            return null
        }

        // 场景2：尝试解析为小数数值
        val asDouble = raw.toDoubleOrNull() ?: return null
        // 过滤非法数值：小于等于0、NaN、无穷大均无效
        if (asDouble <= 0.0 || asDouble.isNaN() || asDouble.isInfinite()) return null

        return if (asDouble < 1.0) {
            // 小于1秒，换算为 1/分母 形式，分母最小为1避免除零
            val denominator = (1.0 / asDouble).roundToInt().coerceAtLeast(1)
            "1/$denominator"
        } else {
            // 大于等于1秒，直接输出小数字符串
            asDouble.toString()
        }
    }

}