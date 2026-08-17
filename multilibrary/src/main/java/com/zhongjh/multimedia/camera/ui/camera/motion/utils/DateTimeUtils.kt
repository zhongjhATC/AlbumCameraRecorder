package com.zhongjh.multimedia.camera.ui.camera.motion.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale


/**
 * 线程安全的日期时间解析与格式化工具类
 * 统一封装Java8 java.time新版时间API，替代频繁创建SimpleDateFormat带来的线程安全问题
 * 已开启Android核心库脱糖（coreLibraryDesugaring），兼容API低于26的系统
 * 功能：ISO时间标准化、EXIF图片时间格式转换、时间戳毫秒解析、多格式容错解析
 */
object DateTimeUtils {

    /**
     * 获取设备系统默认时区ZoneId，全局统一时区基准
     */
    private val systemZone: ZoneId
        get() = ZoneId.systemDefault()


    // ====================== 输出格式化模板（对外格式化使用） ======================
    /**
     * ISO标准 无时区本地时间输出格式：yyyy-MM-dd'T'HH:mm:ss
     * Locale.US 避免不同语言环境月份/星期字符错乱
     */
    private val ISO_LOCAL_OUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.US)

    /**
     * ISO标准 带时区偏移完整输出格式：yyyy-MM-dd'T'HH:mm:ssXXX
     * XXX 会生成 ±HH:mm 格式时区偏移
     */
    private val ISO_OFFSET_OUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)

    /**
     * EXIF图片元数据强制规范时间格式
     * EXIF协议要求年月日使用英文冒号分隔，必须绑定Locale.US防止中文环境元数据写入异常
     * 格式：yyyy:MM:dd HH:mm:ss
     */
    private val EXIF_OUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss", Locale.US)

    // ====================== 带时区Offset类型 输入解析模板组 ======================
    /**
     * 紧凑数字+毫秒+X时区格式 示例：20260811T153020.123X
     */
    private val FMT_COMPACT_MILLIS_OFFSET = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSSX", Locale.US)

    /**
     * ISO标准秒级+单字母X时区 示例：2026-08-11T15:30:20X
     */
    private val FMT_ISO_SECONDS_X = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX", Locale.US)

    /**
     * ISO带Z标识无时区冒号格式 示例：2026-08-11T15:30:20Z
     */
    private val FMT_ISO_OFFSET_NO_COLON = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)

    /**
     * ISO带毫秒+Z时区格式 示例：2026-08-11T15:30:20.123Z
     */
    private val FMT_ISO_MILLIS_OFFSET_NO_COLON = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)

    // ====================== 无本地时区Local类型 输入解析模板组 ======================
    /**
     * 紧凑数字带T本地时间 示例：20260811T153020
     */
    private val FMT_COMPACT_LOCAL = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss", Locale.US)

    /**
     * 标准ISO本地无时区时间 示例：2026-08-11T15:30:20
     */
    private val FMT_ISO_LOCAL = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.US)

    /**
     * ISO带毫秒本地无时区时间 示例：2026-08-11T15:30:20.123
     */
    private val FMT_ISO_MILLIS_LOCAL = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US)

    /**
     * 空格分隔日期时间 示例：2026-08-11 15:30:20
     */
    private val FMT_SPACE_SEP_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US)

    /**
     * 纯数字无T紧凑时间 示例：20260811153020
     */
    private val FMT_COMPACT_NO_T = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.US)

    /**
     * 空格分隔纯日期格式 示例：2026 08 11
     */
    private val FMT_SPACE_SEP_LOCAL = DateTimeFormatter.ofPattern("yyyy MM dd", Locale.US)

    /**
     * 所有带时区偏移的解析器集合
     * 优先级：系统标准ISO_OFFSET_DATE_TIME > 自定义各类偏移格式
     */
    private val OFFSET_INPUT_FORMATTERS = listOf(
        DateTimeFormatter.ISO_OFFSET_DATE_TIME,
        FMT_ISO_SECONDS_X,
        FMT_ISO_OFFSET_NO_COLON,
        FMT_ISO_MILLIS_OFFSET_NO_COLON,
        FMT_COMPACT_MILLIS_OFFSET
    )

    /**
     * 所有不带时区的LocalDateTime解析器集合
     */
    private val LOCAL_DATE_TIME_INPUT_FORMATTERS = listOf(
        FMT_ISO_LOCAL,
        FMT_ISO_MILLIS_LOCAL,
        FMT_COMPACT_LOCAL,
        FMT_SPACE_SEP_DATE_TIME,
        FMT_COMPACT_NO_T
    )


    /**
     * 通用入口：将任意ISO格式时间字符串转换为EXIF图片元数据标准时间格式 yyyy:MM:dd HH:mm:ss
     * 转换规则：
     * 1. OffsetDateTime：先换算到系统默认时区同一瞬间，再转为本地时间格式化（等价atZoneSameInstant）
     * 2. LocalDateTime：直接使用系统时区格式化
     * 3. LocalDate：补当天0点0分0秒后再格式化
     * @param isoDate 原始ISO时间字符串
     * @return EXIF格式时间字符串，解析失败返回null
     */
    fun convertToExif(isoDate: String): String? {
        if (isoDate.isBlank()) return null
        return when (val parsed = parseDateTime(isoDate)) {
            is ParsedDateTime.Offset -> EXIF_OUT_FORMATTER.format(
                // atZoneSameInstant：将带偏移时间换算为系统时区同一物理瞬间，再提取LocalDateTime
                parsed.value.atZoneSameInstant(systemZone).toLocalDateTime()
            )

            is ParsedDateTime.Local -> EXIF_OUT_FORMATTER.format(parsed.value)
            is ParsedDateTime.DateOnly -> EXIF_OUT_FORMATTER.format(parsed.value.atStartOfDay())
            null -> null
        }
    }

    /**
     * 对外核心方法：时间字符串 → UTC纪元毫秒(1970-01-0 00:00)
     * 业务用途：MediaStore DATE_TAKEN字段、文件名时间戳、列表排序时间基准
     * 转换示例对照（假设手机时区东八区UTC+8）：
     * 1. 带时区字符串 "2026-08-12T10:30:00+08:00"
     *    等价UTC时间：2026-08-12 02:30:00 → 输出毫秒：1786545000000
     * 2. 无时区本地 "2026-08-12T10:30:00"
     *    按本机东八区换算UTC：2026-08-12 02:30:00 → 输出毫秒：1786545000000
     * 3. 仅日期 "2026 08 12"
     *    取本机当天零点2026-08-12 00:00+8 → UTC 2026-08-11 16:00 → 输出毫秒：1786473600000
     * @param dateStr 原始ISO标准/相机EXIF各类时间字符串
     * @return 毫秒时间戳；空白/格式解析失败统一返回null，上层可做空兼容处理
     */
    fun parseToMillis(dateStr: String): Long? {
        if (dateStr.isBlank()) return null
        return when (val parsed = parseDateTime(dateStr)) {
            // 带时区：直接转UTC绝对时间戳，不受本地时区影响
            is ParsedDateTime.Offset -> parsed.value.toInstant().toEpochMilli()
            // 无时区本地时间：绑定系统时区再转UTC时间戳
            is ParsedDateTime.Local -> parsed.value.atZone(systemZone).toInstant().toEpochMilli()
            // 纯日期：当天零点（本机时区）换算为UTC毫秒
            is ParsedDateTime.DateOnly -> parsed.value.atStartOfDay(systemZone).toInstant().toEpochMilli()
            null -> null
        }
    }

    /**
     * 密封类：统一封装三种解析结果类型，做类型分支判断
     * Offset：带时区偏移 OffsetDateTime
     * Local：无时区本地 LocalDateTime
     * DateOnly：仅年月日 LocalDate
     */
    private sealed class ParsedDateTime {
        data class Offset(val value: OffsetDateTime) : ParsedDateTime()
        data class Local(val value: LocalDateTime) : ParsedDateTime()
        data class DateOnly(val value: LocalDate) : ParsedDateTime()
    }

    /**
     * 总分发解析入口，统一调度三级解析逻辑
     * 解析优先级从高到低：
     * 1. OffsetDateTime（带时区，全球绝对时间，精度最高，优先解析）
     * 2. LocalDateTime（仅本地年月日时分，无时区信息）
     * 3. LocalDate（仅年月日，无时分秒）
     * 只要上一级解析成功直接返回，全部格式匹配失败返回null
     * @param dateStr 原始未清洗时间字符串，方法内部自动去除首尾空格
     * @return 封装后的时间对象，三种类型其一；所有格式均不匹配返回null
     */
    private fun parseDateTime(dateStr: String): ParsedDateTime? {
        // 清除首尾空格、换行等空白字符
        val trimmed = dateStr.trim()
        // 空字符串直接返回，无需进入解析流程
        if (trimmed.isEmpty()) return null

        // 优先尝试解析带时区完整时间，解析成功直接返回，不再执行后续逻辑
        parseOffsetDateTime(trimmed)?.let { return ParsedDateTime.Offset(it) }
        // 无时区、但包含时分的本地时间解析
        parseLocalDateTime(trimmed)?.let { return ParsedDateTime.Local(it) }
        // 仅包含年月日纯日期，无时分秒最后兜底解析
        parseLocalDate(trimmed)?.let { return ParsedDateTime.DateOnly(it) }

        // 三种格式全部匹配失败，返回null交由上层做空判断兼容
        return null
    }

    /**
     * 尝试解析【带时区偏移】的时间字符串，输出OffsetDateTime
     * 性能优化前置快速过滤：字符串不包含 Z / + / - 时区标识，直接跳过循环，减少无效遍历与异常捕获
     * 匹配规则：必须完整匹配整个字符串，不允许半截匹配（如 2026-08-12T12:00 abc 不会解析成功）
     * @param dateStr 已去除首尾空格的干净时间字符串
     * @return 解析成功返回带时区OffsetDateTime；无匹配格式、解析异常返回null
     */
    private fun parseOffsetDateTime(dateStr: String): OffsetDateTime? {
        // 判断字符串是否存在时区标记：Z(UTC零时区)、+/-时分偏移
        val hasZ = dateStr.contains('Z') || dateStr.contains('+') || dateStr.contains('-')
        // 没有时区标识，不可能是带时区时间，直接跳过循环节省性能
        if (!hasZ) return null

        // 遍历所有支持的带时区格式化模板依次匹配
        for (formatter in OFFSET_INPUT_FORMATTERS) {
            // ParsePosition记录解析进度、错误位置，用于校验是否完整匹配整串
            val pos = java.text.ParsePosition(0)
            // 预解析，只做格式校验不抛出异常，提前判断是否匹配
            val parsed = formatter.parseUnresolved(dateStr, pos)
            // 校验条件：解析对象不为空 + 无解析错误 + 解析终点等于字符串总长度（完整匹配）
            if (parsed != null && pos.errorIndex < 0 && pos.index == dateStr.length) {
                try {
                    // 预校验通过后正式解析生成OffsetDateTime
                    return OffsetDateTime.parse(dateStr, formatter)
                } catch (_: DateTimeParseException) {
                    // 极端预匹配通过但实际解析失败，跳过当前模板，继续下一个
                    continue
                }
            }
        }
        return null
    }

    /**
     * 智能解析【无时区、带年月日时分】本地时间 LocalDateTime
     * 性能优化：根据字符串特征（是否含T分隔符、是否含空格）动态缩小格式化模板集合
     * 避免无意义全量循环，减少大量DateTimeParseException异常捕获，提升批量帧解析速度
     * 完整匹配校验：必须从头到尾完全匹配时间串，不能只匹配前半段
     * @param dateStr 已去除首尾空格的干净时间字符串
     * @return 解析成功返回LocalDateTime；无匹配格式、解析异常返回null
     */
    private fun parseLocalDateTime(dateStr: String): LocalDateTime? {
        // 提取字符串特征，用于筛选最小匹配模板子集
        val hasT = dateStr.contains('T')
        val hasSpace = dateStr.contains(' ')

        // 按特征分组，仅遍历最小必要模板，降低CPU开销
        val formatters = if (hasT) {
            listOf(FMT_ISO_LOCAL, FMT_ISO_MILLIS_LOCAL, FMT_COMPACT_LOCAL)
        } else if (hasSpace) {
            listOf(FMT_SPACE_SEP_DATE_TIME)
        } else {
            listOf(FMT_COMPACT_NO_T)
        }

        // 遍历筛选后的格式化模板逐个校验
        for (formatter in formatters) {
            // ParsePosition记录解析进度、错误位置，用于校验是否完整匹配整串
            val pos = java.text.ParsePosition(0)
            // 预解析，只做格式校验不抛出异常，提前判断是否匹配
            val parsed = formatter.parseUnresolved(dateStr, pos)
            // 校验条件：解析对象不为空 + 无解析错误 + 解析终点等于字符串总长度（完整匹配）
            if (parsed != null && pos.errorIndex < 0 && pos.index == dateStr.length) {
                try {
                    // 预校验通过后正式解析生成LocalDateTime
                    return LocalDateTime.parse(dateStr, formatter)
                } catch (_: DateTimeParseException) {
                    // 极端预匹配通过但实际解析失败，跳过当前模板，继续下一个
                    continue
                }
            }
        }
        return null
    }

    /**
     * 仅解析【纯年月日、无时分秒】的日期字符串，输出LocalDate
     * 支持格式示例：2026 08 12（空格分隔年月日）
     * 仅作为最低优先级兜底解析，仅当前两类时间均匹配失败才会执行
     * @param dateStr 已去除首尾空格的干净纯日期字符串
     * @return 解析成功返回LocalDate；格式不匹配、解析异常返回null
     */
    private fun parseLocalDate(dateStr: String): LocalDate? {
        // ParsePosition记录解析进度、错误位置，用于校验是否完整匹配整串
        val pos = java.text.ParsePosition(0)
        // 预解析，只做格式校验不抛出异常，提前判断是否匹配
        val parsed = FMT_SPACE_SEP_LOCAL.parseUnresolved(dateStr, pos)
        // 校验条件：解析对象不为空 + 无解析错误 + 解析终点等于字符串总长度（完整匹配）
        if (parsed != null && pos.errorIndex < 0 && pos.index == dateStr.length) {
            return try {
                // 预校验通过后正式解析生成LocalDate
                LocalDate.parse(dateStr, FMT_SPACE_SEP_LOCAL)
            } catch (_: DateTimeParseException) {
                // 格式合法但解析异常，直接返回null
                null
            }
        }
        return null
    }
}