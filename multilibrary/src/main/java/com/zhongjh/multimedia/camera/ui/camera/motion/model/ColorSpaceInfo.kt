package com.zhongjh.multimedia.camera.ui.camera.motion.model

/**
 * 色彩空间信息实体，用来标记画面是普通SDR还是HDR高动态，保存色域、位深、HDR元数据标记
 * 导出的时候依靠这个对象判断要不要做HDR转SDR色调映射
 */
data class ColorSpaceInfo(
    /**
     * 画面类型：SDR普通 / HDR10 / HLG 等等
     */
    val type: ColorSpaceType = ColorSpaceType.SDR,
    /**
     * 光电转换函数，HDR识别用
     */
    val transferFunction: TransferFunction = TransferFunction.SRGB,
    /**
     * 色彩色域范围，BT709就是普通sRGB；BT2020是HDR广色域
     */
    val colorGamut: ColorGamut = ColorGamut.BT709,
    /**
     * 像素位深，普通图片8bit，HDR一般10bit
     */
    val bitDepth: Int = 8,
    /**
     * 是否携带HDR静态元数据 SMPTE ST 2086（HDR10使用)
     * */
    val hasStaticHdrMetadata: Boolean = false,
    /**
     * 是否携带动态HDR元数据，例如杜比视界RPU数据
     *  */
    val hasDynamicHdrMetadata: Boolean = false
) {
    /**
     * 计算属性：true代表这是HDR画面；只要type不等于SDR就判定为HDR
     */
    val isHdr: Boolean
        get() = type != ColorSpaceType.SDR
}

/**
 * 支持的画面类型枚举
 */
enum class ColorSpaceType {
    SDR,            // 普通标准动态范围，我们日常大部分照片视频
    HDR10,          // HDR10，最通用的HDR格式，带静态元数据
    HDR10_PLUS,     // HDR10+，带动态元数据
    HLG,            // HLG，广播类HDR
    DOLBY_VISION    // 杜比视界
}

/**
 * 光电转换函数（描述像素亮度如何映射到屏幕显示）
 */
enum class TransferFunction {
    SRGB,       // 普通图片、屏幕默认
    LINEAR,     // 线性光，一般是内部处理中间数据
    PQ,         // PQ曲线，HDR10、杜比视界在用 SMPTE ST 2084
    HLG,        // HLG曲线，广播HDR ARIB STD‑B67
    GAMMA_2_2   // Gamma2.2，老式视频曲线
}

/**
 * 色彩色域，决定能显示多少种颜色
 */
enum class ColorGamut {
    BT709,     // Rec.709，等同于sRGB，手机普通图片视频标准色域
    BT2020,    // BT2020，HDR视频的广色域，颜色范围更大
    DCI_P3,    // DCI‑P3，电影级色域
    DISPLAY_P3 // Display‑P3，苹果设备常用广色域
}
