package com.zhongjh.imageedit.core.clip;

import android.graphics.RectF;

/**
 *
 * @author felix
 * @date 2017/11/28 下午6:15
 */
public interface ImageClip {

    /**
     * 裁剪区域的边距
     */
    float CLIP_MARGIN = 60f;

    /**
     * 角尺寸
     */
    float CLIP_CORNER_SIZE = 48f;

    /**
     * 裁剪区域最小尺寸
     */
    float CLIP_FRAME_MIN = CLIP_CORNER_SIZE * 3.14f;

    /**
     * 内边厚度
     */
    float CLIP_THICKNESS_CELL = 3f;

    /**
     * 外边框厚度
     */
    float CLIP_THICKNESS_FRAME = 8f;

    /**
     * 角边厚度
     */
    float CLIP_THICKNESS_SEWING = 14f;

    /**
     * 比例尺，用于计算出 {0, width, 1/3 width, 2/3 width} & {0, height, 1/3 height, 2/3 height}
     */
    float[] CLIP_SIZE_RATIO = {0, 1, 0.33f, 0.66f};

    int CLIP_CELL_STRIDES = 0x7362DC98;

    int CLIP_CORNER_STRIDES = 0x0AAFF550;

    float[] CLIP_CORNER_STEPS = {0, 3, -3};

    float[] CLIP_CORNER_SIZES = {0, CLIP_CORNER_SIZE, -CLIP_CORNER_SIZE};

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

    enum Anchor {
        /**
         * LEFT
         */
        LEFT(1),
        /**
         * RIGHT
         */
        RIGHT(2),
        /**
         * TOP
         */
        TOP(4),
        /**
         * BOTTOM
         */
        BOTTOM(8),
        /**
         * LEFT_TOP
         */
        LEFT_TOP(5),
        /**
         * RIGHT_TOP
         */
        RIGHT_TOP(6),
        /**
         * LEFT_BOTTOM
         */
        LEFT_BOTTOM(9),
        /**
         * RIGHT_BOTTOM
         */
        RIGHT_BOTTOM(10);

        int v;

        /**
         * LEFT: 0
         * TOP: 2
         * RIGHT: 1
         * BOTTOM: 3
         */


        final static int[] PN = {1, -1};

        Anchor(int v) {
            this.v = v;
        }

        final static int COUNT = 4;

        public void move(RectF win, RectF frame, float dx, float dy) {
            float[] maxFrame = cohesion(win, CLIP_MARGIN);
            float[] minFrame = cohesion(frame, CLIP_FRAME_MIN);
            float[] theFrame = cohesion(frame, 0);

            float[] dxy = {dx, 0, dy};
            for (int i = 0; i < COUNT; i++) {
                if (((1 << i) & v) != 0) {

                    int pn = PN[i & 1];

                    theFrame[i] = pn * revise(pn * (theFrame[i] + dxy[i & 2]),
                            pn * maxFrame[i], pn * minFrame[i + PN[i & 1]]);
                }
            }

            frame.set(theFrame[0], theFrame[2], theFrame[1], theFrame[3]);
        }

        public static float revise(float v, float min, float max) {
            return Math.min(Math.max(v, min), max);
        }

        public static float[] cohesion(RectF win, float v) {
            return new float[]{
                    win.left + v, win.right - v,
                    win.top + v, win.bottom - v
            };
        }

        public static boolean isCohesionContains(RectF frame, float v, float x, float y) {
            return frame.left + v < x && frame.right - v > x
                    && frame.top + v < y && frame.bottom - v > y;
        }

        public static Anchor valueOf(int v) {
            Anchor[] values = values();
            for (Anchor anchor : values) {
                if (anchor.v == v) {
                    return anchor;
                }
            }
            return null;
        }
    }
}