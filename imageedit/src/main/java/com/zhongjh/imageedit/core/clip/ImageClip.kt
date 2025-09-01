package com.zhongjh.imageedit.core.clip;

import android.graphics.RectF;

/**
 * 图像裁剪接口，定义了裁剪功能的常量、枚举和方法
 * 提供了裁剪框尺寸、边角样式、网格线等相关常量，以及裁剪框锚点的枚举定义和操作方法
 * 是图像编辑器中裁剪功能的基础接口
 * 
 * @author felix
 * @date 2017/11/28 下午6:15
 */
public interface ImageClip {

    /**
     * 裁剪区域的边距，即裁剪框与窗口边缘之间的最小距离
     */
    float CLIP_MARGIN = 60f;

    /**
     * 裁剪框角点标记的尺寸，影响用户可点击区域的大小
     */
    float CLIP_CORNER_SIZE = 48f;

    /**
     * 裁剪区域的最小尺寸，确保裁剪框不会过小而无法操作
     */
    float CLIP_FRAME_MIN = CLIP_CORNER_SIZE * 3.14f;

    /**
     * 裁剪框内部网格线的厚度
     */
    float CLIP_THICKNESS_CELL = 3f;

    /**
     * 裁剪框外边框的厚度
     */
    float CLIP_THICKNESS_FRAME = 8f;

    /**
     * 裁剪框角点标记线条的厚度
     */
    float CLIP_THICKNESS_SEWING = 14f;

    /**
     * 比例尺数组，用于计算裁剪框内部网格线的位置
     * 包含值：{0, 1, 0.33f, 0.66f}，对应于：{0%, 100%, 33%, 66%}的位置
     */
    float[] CLIP_SIZE_RATIO = {0, 1, 0.33f, 0.66f};

    /**
     * 用于计算网格线坐标的位掩码常量
     * 这个16进制值在二进制中表示了网格线坐标计算的模式
     * 具体来说，这个值的二进制形式为: 01110011011000101101110010011000
     * 通过位运算和移位操作，可以高效地从基础尺寸数组中获取正确的参考值
     * 每次取出两位来确定使用CLIP_SIZE_RATIO数组中的哪个索引值
     */
    int CLIP_CELL_STRIDES = 0x7362DC98;

    /**
     * 用于计算角点标记坐标的位掩码常量
     * 这个16进制值在二进制中表示了角点标记坐标计算的模式
     * 具体来说，这个值的二进制形式为: 00001010101011111111010101010000
     * 配合CLIP_CORNER_SIZES和CLIP_CORNER_STEPS数组，生成裁剪框8个角点的标记坐标
     * 每次取出一位来确定使用CLIP_SIZE_RATIO数组中的哪个索引值
     */
    int CLIP_CORNER_STRIDES = 0x0AAFF550;

    /**
     * 角点标记线条的步长值数组，用于调整角点标记的形状
     */
    float[] CLIP_CORNER_STEPS = {0, 3, -3};

    /**
     * 角点标记的大小值数组，包含正负值用于绘制内外角点
     */
    float[] CLIP_CORNER_SIZES = {0, CLIP_CORNER_SIZE, -CLIP_CORNER_SIZE};

    /**
     * 角点标记的坐标计算数据数组
     * 存储了用于生成裁剪框8个角点标记的二进制数据
     * 这些数据通过位运算与CLIP_CORNER_SIZES和CLIP_CORNER_STEPS数组配合使用
     * 生成每个角点上的两条交叉线的坐标信息
     */
    byte[] CLIP_CORNERS = {
            0x8, 0x8, 0x9, 0x8,
            0x6, 0x8, 0x4, 0x8,
            0x4, 0x8, 0x4, 0x1,
            0x4, 0xA, 0x4, 0x8,
            0x4, 0x4, 0x6, 0x4,
            0x9, 0x4, 0x8, 0x4,
            0x8, 0x4, 0x8, 0x6,
            0x8, 0x9, 0x8, 0x8
    };

    /**
     * 裁剪框锚点枚举，表示裁剪框可调整的边缘和角落位置
     * 用于处理用户拖拽裁剪框的不同位置时的交互逻辑
     */
    enum Anchor {
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
         * 锚点的位掩码值，用于标识锚点的位置
         */
        final int v;

        /**
         * 方向符号数组，用于在计算坐标时确定加减方向
         * 索引0: 1（正方向）
         * 索引1: -1（负方向）
         * 在位运算中，通过i & 1操作来确定使用哪个方向值
         */
        final static int[] PN = {1, -1};

        /**
         * 锚点构造函数，初始化锚点的位掩码值
         * 
         * @param v 锚点的位掩码值
         */
        Anchor(int v) {
            this.v = v;
        }

        /**
         * 坐标维度计数，表示矩形的四个边界（左、右、上、下）
         * 用于控制move方法中的循环次数
         */
        final static int COUNT = 4;

        /**
         * 根据用户的拖拽操作移动裁剪框
         * 根据当前拖动的锚点位置和移动距离，计算并更新裁剪框的尺寸和位置
         * 
         * 移动逻辑：
         * 1. 计算窗口的凝聚力边界（考虑边距）作为最大边界限制
         * 2. 计算裁剪框的最小尺寸边界作为最小尺寸限制
         * 3. 根据锚点的位掩码值确定需要移动的边界
         * 4. 对每个需要移动的边界，根据移动距离和边界限制计算新的坐标
         * 5. 更新裁剪框的位置和尺寸
         * 
         * @param win 用户操作的窗口矩形
         * @param frame 当前的裁剪框矩形，此矩形将被修改
         * @param dx X轴方向的移动距离
         * @param dy Y轴方向的移动距离
         */
        public void move(RectF win, RectF frame, float dx, float dy) {
            // 计算窗口的凝聚力边界（考虑边距）作为最大边界限制
            float[] maxFrame = cohesion(win, CLIP_MARGIN);
            // 计算裁剪框的最小尺寸边界作为最小尺寸限制
            float[] minFrame = cohesion(frame, CLIP_FRAME_MIN);
            // 获取当前裁剪框的边界坐标 [左, 右, 上, 下]
            float[] theFrame = cohesion(frame, 0);

            // 存储X和Y轴的移动距离
            // 索引0: dx (X轴移动距离)
            // 索引1: 0 (占位)
            // 索引2: dy (Y轴移动距离)
            float[] dxy = {dx, 0, dy};
            // 遍历四个边界（左、右、上、下）
            for (int i = 0; i < COUNT; i++) {
                // 检查当前锚点是否包含该边界（通过位运算判断）
                if (((1 << i) & v) != 0) {
                    // 确定坐标调整的方向符号（0或1，对应正方向或负方向）
                    int pn = PN[i & 1];

                    // 根据移动方向、边界限制和当前位置计算新的边界坐标
                    // 使用revise方法确保新坐标在允许的范围内
                    // i & 2操作将索引映射到dxy数组的正确位置（0->X, 1->X, 2->Y, 3->Y）
                    theFrame[i] = pn * revise(pn * (theFrame[i] + dxy[i & 2]),
                            pn * maxFrame[i], pn * minFrame[i + PN[i & 1]]);
                }
            }

            // 更新裁剪框的位置和尺寸 [左, 上, 右, 下]
            frame.set(theFrame[0], theFrame[2], theFrame[1], theFrame[3]);
        }

        /**
         * 将值限制在指定的最小值和最大值之间
         * 确保值不会超出允许的范围
         * 
         * @param v 要限制的值
         * @param min 最小值
         * @param max 最大值
         * @return 限制后的值，如果v小于min则返回min，如果v大于max则返回max，否则返回v
         */
        public static float revise(float v, float min, float max) {
            // 先取v和min中的较大值，再取结果与max中的较小值
            return Math.min(Math.max(v, min), max);
        }

        /**
         * 计算矩形的凝聚力边界，即考虑了边距后的内边界
         * 用于确定裁剪框可移动的有效范围
         * 
         * @param win 原始矩形
         * @param v 边距值，正值表示向内缩小，负值表示向外扩大
         * @return 包含凝聚力边界坐标的数组 [左, 右, 上, 下]
         */
        public static float[] cohesion(RectF win, float v) {
            return new float[]{
                    win.left + v,      // 左边界
                    win.right - v,     // 右边界
                    win.top + v,       // 上边界
                    win.bottom - v     // 下边界
            };
        }

        /**
         * 判断指定点是否在矩形的凝聚力边界内部
         * 用于确定用户的触摸点是否在裁剪框的可交互区域内
         * 
         * @param frame 矩形区域
         * @param v 边距值，正值表示检查点是否在矩形内部偏移v的位置，负值表示外部偏移
         * @param x 点的X坐标
         * @param y 点的Y坐标
         * @return 如果点在凝聚力边界内部则返回true，否则返回false
         */
        public static boolean isCohesionContains(RectF frame, float v, float x, float y) {
            // 判断点是否在矩形的凝聚力边界内（考虑边距v）
            return frame.left + v < x && frame.right - v > x  // X坐标范围检查
                    && frame.top + v < y && frame.bottom - v > y;  // Y坐标范围检查
        }

        /**
         * 根据位掩码值获取对应的锚点枚举
         * 用于将用户触摸位置计算出的位掩码值转换为具体的锚点枚举值
         * 
         * @param v 位掩码值，表示用户可能触摸的边界组合
         * @return 对应的锚点枚举，如果没有找到匹配的锚点则返回null
         */
        public static Anchor valueOf(int v) {
            // 获取所有锚点枚举值
            Anchor[] values = values();
            // 遍历查找匹配的锚点
            for (Anchor anchor : values) {
                if (anchor.v == v) {
                    return anchor;
                }
            }
            // 没有找到匹配的锚点
            return null;
        }
    }
}