package com.zhongjh.imageedit.core.clip

/**
 * 图像裁剪接口，定义了裁剪功能的常量、枚举和方法
 * 提供了裁剪框尺寸、边角样式、网格线等相关常量，以及裁剪框锚点的枚举定义和操作方法
 * 是图像编辑器中裁剪功能的基础接口
 *
 * @author zhongjh
 * @date 2025-09-01
 */
open class ImageClip {
    companion object {
        /**
         * 裁剪区域的边距，即裁剪框与窗口边缘之间的最小距离
         */
        const val CLIP_MARGIN: Float = 60f

        /**
         * 裁剪框角点标记的尺寸，影响用户可点击区域的大小
         */
        const val CLIP_CORNER_SIZE: Float = 48f

        /**
         * 裁剪区域的最小尺寸，确保裁剪框不会过小而无法操作
         */
        const val CLIP_FRAME_MIN: Float = CLIP_CORNER_SIZE * 3.14f

        /**
         * 裁剪框内部网格线的厚度
         */
        const val CLIP_THICKNESS_CELL: Float = 3f

        /**
         * 裁剪框外边框的厚度
         */
        const val CLIP_THICKNESS_FRAME: Float = 8f

        /**
         * 裁剪框角点标记线条的厚度
         */
        const val CLIP_THICKNESS_SEWING: Float = 14f

        /**
         * 比例尺数组，用于计算裁剪框内部网格线的位置
         * 包含值：{0, 1, 0.33f, 0.66f}，对应于：{0%, 100%, 33%, 66%}的位置
         */
        val CLIP_SIZE_RATIO: FloatArray = floatArrayOf(0f, 0.25f, 0.5f, 0.75f, 1f)

        /**
         * 用于计算网格线坐标的位掩码常量
         * 这个16进制值在二进制中表示了网格线坐标计算的模式
         * 具体来说，这个值的二进制形式为: 01110011011000101101110010011000
         * 通过位运算和移位操作，可以高效地从基础尺寸数组中获取正确的参考值
         * 每次取出两位来确定使用CLIP_SIZE_RATIO数组中的哪个索引值
         */
        const val CLIP_CELL_STRIDES: Int = 0x7362DC98

        /**
         * 用于计算角点标记坐标的位掩码常量
         * 这个16进制值在二进制中表示了角点标记坐标计算的模式
         * 具体来说，这个值的二进制形式为: 00001010101011111111010101010000
         * 配合CLIP_CORNER_SIZES和CLIP_CORNER_STEPS数组，生成裁剪框8个角点的标记坐标
         * 每次取出一位来确定使用CLIP_SIZE_RATIO数组中的哪个索引值
         */
        const val CLIP_CORNER_STRIDES: Int = 0x0AAFF550

        /**
         * 角点标记线条的步长值数组，用于调整角点标记的形状
         */
        val CLIP_CORNER_STEPS: FloatArray = floatArrayOf(0f, 3f, -3f)

        /**
         * 角点标记的大小值数组，包含正负值用于绘制内外角点
         */
        val CLIP_CORNER_SIZES: FloatArray = floatArrayOf(0f, CLIP_CORNER_SIZE, -CLIP_CORNER_SIZE)

        /**
         * 角点标记的坐标计算数据数组
         * 存储了用于生成裁剪框8个角点标记的二进制数据
         * 这些数据通过位运算与CLIP_CORNER_SIZES和CLIP_CORNER_STEPS数组配合使用
         * 生成每个角点上的两条交叉线的坐标信息
         */
        val CLIP_CORNERS: ByteArray = byteArrayOf(
            0x8, 0x8, 0x9, 0x8,
            0x6, 0x8, 0x4, 0x8,
            0x4, 0x8, 0x4, 0x1,
            0x4, 0xA, 0x4, 0x8,
            0x4, 0x4, 0x6, 0x4,
            0x9, 0x4, 0x8, 0x4,
            0x8, 0x4, 0x8, 0x6,
            0x8, 0x9, 0x8, 0x8
        )
    }
}