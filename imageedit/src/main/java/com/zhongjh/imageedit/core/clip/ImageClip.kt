package com.zhongjh.imageedit.core.clip


/**
 * 图像裁剪功能的基础接口，定义裁剪相关的常量和规范
 * @author zhongjh
 * @date 2025/09/01
 */
interface ImageClip {

    /**
     * 裁剪相关常量定义
     */
    companion object {
        /**
         * 裁剪区域与窗口边缘的边距，用于限制裁剪框最小内边距
         */
        const val CLIP_MARGIN = 60f

        /**
         * 裁剪框边角控制点的尺寸大小（边长）
         */
        const val CLIP_CORNER_SIZE = 48f

        /**
         * 裁剪区域的最小尺寸限制（宽/高的最小值）
         */
        const val CLIP_FRAME_MIN = CLIP_CORNER_SIZE * 3.14f

        /**
         * 裁剪框内部网格线的厚度
         */
        const val CLIP_THICKNESS_CELL = 3f

        /**
         * 裁剪框外边框的厚度
         */
        const val CLIP_THICKNESS_FRAME = 8f

        /**
         * 裁剪框边角线条的厚度
         */
        const val CLIP_THICKNESS_SEWING = 14f

        /**
         * 尺寸比例数组，用于计算网格线位置
         * 格式: {0f (起点), 1f (终点), 0.33f (1/3位置), 0.66f (2/3位置)}
         */
        val CLIP_SIZE_RATIO = floatArrayOf(0f, 1f, 0.33f, 0.66f)

        /**
         * 网格线坐标计算的位运算参数（内部使用）
         */
        const val CLIP_CELL_STRIDES = 0x7362DC98

        /**
         * 边角线条坐标计算的位运算参数（内部使用）
         */
        const val CLIP_CORNER_STRIDES = 0x0AAFF550

        /**
         * 边角线条的偏移步长数组，用于控制边角线条的延伸方向
         */
        val CLIP_CORNER_STEPS = floatArrayOf(0f, 3f, -3f)

        /**
         * 边角尺寸数组，用于计算边角线条的长度
         * 格式: {0f (无偏移), CLIP_CORNER_SIZE (正向偏移), -CLIP_CORNER_SIZE (负向偏移)}
         */
        val CLIP_CORNER_SIZES = floatArrayOf(0f, CLIP_CORNER_SIZE, -CLIP_CORNER_SIZE)

        /**
         * 边角线条的配置数组，存储每个边角线条的计算参数（内部使用）
         */
        val CLIP_CORNERS = byteArrayOf(
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