package com.zhongjh.imageedit.core.constants

import android.graphics.RectF
import com.zhongjh.imageedit.core.clip.ImageClip
import kotlin.math.max
import kotlin.math.min

/**
 * 裁剪框锚点枚举，表示裁剪框可调整的边缘和角落位置
 * 用于处理用户拖拽裁剪框的不同位置时的交互逻辑
 * @param v 锚点的位掩码值，用于标识锚点的位置
 */
enum class Anchor(val v: Int) {
    /**
     * 左侧边缘锚点
     */
    LEFT(1),

    /**
     * 右侧边缘锚点
     */
    RIGHT(2),

    /**
     * 顶部边缘锚点
     */
    TOP(4),

    /**
     * 底部边缘锚点
     */
    BOTTOM(8),

    /**
     * 左上角锚点
     */
    LEFT_TOP(5),

    /**
     * 右上角锚点
     */
    RIGHT_TOP(6),

    /**
     * 左下角锚点
     */
    LEFT_BOTTOM(9),

    /**
     * 右下角锚点
     */
    RIGHT_BOTTOM(10);

    /**
     * 根据用户的拖拽操作移动裁剪框
     * 根据当前拖动的锚点位置和移动距离，计算并更新裁剪框的尺寸和位置
     *
     *
     * 移动逻辑：
     * 1. 计算窗口的凝聚力边界（考虑边距）作为最大边界限制
     * 2. 计算裁剪框的最小尺寸边界作为最小尺寸限制
     * 3. 根据锚点的位掩码值确定需要移动的边界
     * 4. 对每个需要移动的边界，根据移动距离和边界限制计算新的坐标
     * 5. 更新裁剪框的位置和尺寸
     *
     * @param win   用户操作的窗口矩形
     * @param frame 当前的裁剪框矩形，此矩形将被修改
     * @param dx    X轴方向的移动距离
     * @param dy    Y轴方向的移动距离
     */
    fun move(win: RectF, frame: RectF, dx: Float, dy: Float) {
        // 计算窗口的凝聚力边界（考虑边距）作为最大边界限制
        val maxFrame = cohesion(win, ImageClip.CLIP_MARGIN)
        // 计算裁剪框的最小尺寸边界作为最小尺寸限制
        val minFrame = cohesion(frame, ImageClip.CLIP_FRAME_MIN)
        // 获取当前裁剪框的边界坐标 [左, 右, 上, 下]
        val theFrame = cohesion(frame, 0f)

        // 存储X和Y轴的移动距离
        // 索引0: dx (X轴移动距离)
        // 索引1: 0 (占位)
        // 索引2: dy (Y轴移动距离)
        val dxy = floatArrayOf(dx, 0f, dy)
        // 遍历四个边界（左、右、上、下）
        for (i in 0 until COUNT) {
            // 检查当前锚点是否包含该边界（通过位运算判断）
            if (((1 shl i) and v) != 0) {
                // 确定坐标调整的方向符号（0或1，对应正方向或负方向）
                val pn = PN[i and 1]

                // 根据移动方向、边界限制和当前位置计算新的边界坐标
                // 使用revise方法确保新坐标在允许的范围内
                // i & 2操作将索引映射到dxy数组的正确位置（0->X, 1->X, 2->Y, 3->Y）
                theFrame[i] = pn * revise(
                    pn * (theFrame[i] + dxy[i and 2]),
                    pn * maxFrame[i], pn * minFrame[i + PN[i and 1]]
                )
            }
        }

        // 更新裁剪框的位置和尺寸 [左, 上, 右, 下]
        frame[theFrame[0], theFrame[2], theFrame[1]] = theFrame[3]
    }

    companion object {
        /**
         * 方向符号数组，用于在计算坐标时确定加减方向
         * 索引0: 1（正方向）
         * 索引1: -1（负方向）
         * 在位运算中，通过i & 1操作来确定使用哪个方向值
         */
        val PN: IntArray = intArrayOf(1, -1)

        /**
         * 坐标维度计数，表示矩形的四个边界（左、右、上、下）
         * 用于控制move方法中的循环次数
         */
        const val COUNT: Int = 4

        /**
         * 将值限制在指定的最小值和最大值之间
         * 确保值不会超出允许的范围
         *
         * @param v   要限制的值
         * @param min 最小值
         * @param max 最大值
         * @return 限制后的值，如果v小于min则返回min，如果v大于max则返回max，否则返回v
         */
        fun revise(v: Float, min: Float, max: Float): Float {
            // 先取v和min中的较大值，再取结果与max中的较小值
            return min(max(v.toDouble(), min.toDouble()), max.toDouble()).toFloat()
        }

        /**
         * 计算矩形的凝聚力边界，即考虑了边距后的内边界
         * 用于确定裁剪框可移动的有效范围
         *
         * @param win 原始矩形
         * @param v   边距值，正值表示向内缩小，负值表示向外扩大
         * @return 包含凝聚力边界坐标的数组 [左, 右, 上, 下]
         */
        fun cohesion(win: RectF, v: Float): FloatArray {
            return floatArrayOf(
                win.left + v,  // 左边界
                win.right - v,  // 右边界
                win.top + v,  // 上边界
                win.bottom - v // 下边界
            )
        }

        /**
         * 判断指定点是否在矩形的凝聚力边界内部
         * 用于确定用户的触摸点是否在裁剪框的可交互区域内
         *
         * @param frame 矩形区域
         * @param v     边距值，正值表示检查点是否在矩形内部偏移v的位置，负值表示外部偏移
         * @param x     点的X坐标
         * @param y     点的Y坐标
         * @return 如果点在凝聚力边界内部则返回true，否则返回false
         */
        fun isCohesionContains(frame: RectF, v: Float, x: Float, y: Float): Boolean {
            // 判断点是否在矩形的凝聚力边界内（考虑边距v）
            return frame.left + v < x && frame.right - v > x && frame.top + v < y && frame.bottom - v > y
        }

        /**
         * 根据位掩码值获取对应的锚点枚举
         * 用于将用户触摸位置计算出的位掩码值转换为具体的锚点枚举值
         *
         * @param v 位掩码值，表示用户可能触摸的边界组合
         * @return 对应的锚点枚举，如果没有找到匹配的锚点则返回null
         */
        fun valueOf(v: Int): Anchor? {
            // 获取所有锚点枚举值
            val values = values()
            // 遍历查找匹配的锚点
            for (anchor in values) {
                if (anchor.v == v) {
                    return anchor
                }
            }
            // 没有找到匹配的锚点
            return null
        }
    }
}
