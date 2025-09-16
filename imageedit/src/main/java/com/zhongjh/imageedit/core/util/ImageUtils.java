package com.zhongjh.imageedit.core.util;

import android.graphics.Matrix;
import android.graphics.RectF;

import com.zhongjh.imageedit.core.homing.ImageHoming;


/**
 * 图像处理工具类，提供图像坐标变换、缩放适配、居中对齐等常用功能
 * 用于支持图像编辑器中的各种交互和显示操作
 * @author felix
 * @date 2017/12/5 下午2:20
 */
public class ImageUtils {

    /**
     * 用于矩阵变换的静态实例，避免频繁创建对象
     */
    private static final Matrix M = new Matrix();

    /**
     * 将一个矩形居中放置在另一个矩形内部
     * @param win 目标窗口矩形
     * @param frame 要居中的框架矩形
     */
    public static void center(RectF win, RectF frame) {
        // 计算并应用偏移量，使frame在win中居中
        frame.offset(win.centerX() - frame.centerX(), win.centerY() - frame.centerY());
    }

    /**
     * 将框架矩形等比例缩放到适应窗口，保持中心点不变（无内边距）
     * @param win 目标窗口矩形
     * @param frame 要缩放的框架矩形
     */
    public static void fitCenter(RectF win, RectF frame) {
        fitCenter(win, frame, 0);
    }

    /**
     * 将框架矩形等比例缩放到适应窗口，保持中心点不变（带统一内边距）
     * @param win 目标窗口矩形
     * @param frame 要缩放的框架矩形
     * @param padding 四周统一的内边距值
     */
    public static void fitCenter(RectF win, RectF frame, float padding) {
        fitCenter(win, frame, padding, padding, padding, padding);
    }

    /**
     * 将框架矩形等比例缩放到适应窗口，保持中心点不变（带自定义内边距）
     * 支持设置不同的左右上下内边距，实现更灵活的布局适配
     * 
     * @param win 目标窗口矩形
     * @param frame 要缩放的框架矩形
     * @param paddingLeft 左侧内边距
     * @param paddingTop 顶部内边距
     * @param paddingRight 右侧内边距
     * @param paddingBottom 底部内边距
     */
    public static void fitCenter(RectF win, RectF frame, float paddingLeft, float paddingTop, float paddingRight, float paddingBottom) {
        if (win.isEmpty() || frame.isEmpty()) {
            return;
        }

        if (win.width() < paddingLeft + paddingRight) {
            paddingLeft = paddingRight = 0;
            // 忽略Padding 值
        }

        if (win.height() < paddingTop + paddingBottom) {
            paddingTop = paddingBottom = 0;
            // 忽略Padding 值
        }

        float w = win.width() - paddingLeft - paddingRight;
        float h = win.height() - paddingTop - paddingBottom;

        float scale = Math.min(w / frame.width(), h / frame.height());

        // 缩放FIT
        frame.set(0, 0, frame.width() * scale, frame.height() * scale);

        // 中心对齐
        frame.offset(
                win.centerX() + (paddingLeft - paddingRight) / 2 - frame.centerX(),
                win.centerY() + (paddingTop - paddingBottom) / 2 - frame.centerY()
        );
    }

    /**
     * 计算框架矩形适应窗口的归位信息（居中缩放模式）
     * @param win 目标窗口矩形
     * @param frame 要缩放的框架矩形
     * @return 归位信息对象，包含平移和缩放参数
     */
    public static ImageHoming fitHoming(RectF win, RectF frame) {
        // 创建默认归位信息对象（无平移、无缩放、无旋转）
        ImageHoming dHoming = new ImageHoming(0, 0, 1, 0);

        // 框架完全包含窗口时，不需要缩放适配
        if (frame.contains(win)) {
            return dHoming;
        }

        // 只有当框架的宽高都小于窗口时，才进行放大操作
        if (frame.width() < win.width() && frame.height() < win.height()) {
            // 计算最小缩放比例，确保框架完全适应窗口
            dHoming.setScale(Math.min(win.width() / frame.width(), win.height() / frame.height()));
        }

        RectF rect = new RectF();
        M.setScale(dHoming.getScale(), dHoming.getScale(), frame.centerX(), frame.centerY());
        M.mapRect(rect, frame);

        if (rect.width() < win.width()) {
            dHoming.setX(dHoming.getX() + win.centerX() - rect.centerX());
        } else {
            if (rect.left > win.left) {
                dHoming.setX(dHoming.getX() + win.left - rect.left);
            } else if (rect.right < win.right) {
                dHoming.setX(dHoming.getX() + win.right - rect.right);
            }
        }

        if (rect.height() < win.height()) {
            dHoming.setY(dHoming.getY() + win.centerY() - rect.centerY());
        } else {
            if (rect.top > win.top) {
                dHoming.setY(dHoming.getY() + win.top - rect.top);
            } else if (rect.bottom < win.bottom) {
                dHoming.setY(dHoming.getY() + win.bottom - rect.bottom);
            }
        }

        return dHoming;
    }

    /**
     * 计算框架矩形适应窗口的归位信息（居中缩放模式），使用自定义中心点
     * 
     * @param win 目标窗口矩形
     * @param frame 要缩放的框架矩形
     * @param centerX 缩放中心点的X坐标
     * @param centerY 缩放中心点的Y坐标
     * @return 归位信息对象，包含平移和缩放参数
     */
    public static ImageHoming fitHoming(RectF win, RectF frame, float centerX, float centerY) {
        ImageHoming dHoming = new ImageHoming(0, 0, 1, 0);

        if (frame.contains(win)) {
            // 不需要Fit
            return dHoming;
        }

        // 宽高都小于Win，才有必要放大
        if (frame.width() < win.width() && frame.height() < win.height()) {
            dHoming.setScale(Math.min(win.width() / frame.width(), win.height() / frame.height()));
        }

        RectF rect = new RectF();
        M.setScale(dHoming.getScale(), dHoming.getScale(), centerX, centerY);
        M.mapRect(rect, frame);

        if (rect.width() < win.width()) {
            dHoming.setX(dHoming.getX() + win.centerX() - rect.centerX());
        } else {
            if (rect.left > win.left) {
                dHoming.setX(dHoming.getX() + win.left - rect.left);
            } else if (rect.right < win.right) {
                dHoming.setX(dHoming.getX() + win.right - rect.right);
            }
        }

        if (rect.height() < win.height()) {
            dHoming.setY(dHoming.getY() + win.centerY() - rect.centerY());
        } else {
            if (rect.top > win.top) {
                dHoming.setY(dHoming.getY() + win.top - rect.top);
            } else if (rect.bottom < win.bottom) {
                dHoming.setY(dHoming.getY() + win.bottom - rect.bottom);
            }
        }

        return dHoming;
    }


    /**
     * 计算框架矩形适应窗口的归位信息（居中缩放模式），支持强制内部适配
     * 
     * @param win 目标窗口矩形
     * @param frame 要缩放的框架矩形
     * @param isJustInner 是否强制框架完全包含在窗口内部，即使框架尺寸大于窗口
     * @return 归位信息对象，包含平移和缩放参数
     */
    public static ImageHoming fitHoming(RectF win, RectF frame, boolean isJustInner) {
        ImageHoming dHoming = new ImageHoming(0, 0, 1, 0);

        if (frame.contains(win) && !isJustInner) {
            // 不需要Fit
            return dHoming;
        }

        // 宽高都小于Win，才有必要放大
        boolean isScale = isJustInner || frame.width() < win.width() && frame.height() < win.height();
        if (isScale) {
            dHoming.setScale(Math.min(win.width() / frame.width(), win.height() / frame.height()));
        }

        RectF rect = new RectF();
        M.setScale(dHoming.getScale(), dHoming.getScale(), frame.centerX(), frame.centerY());
        M.mapRect(rect, frame);

        if (rect.width() < win.width()) {
            dHoming.setX(dHoming.getX() + win.centerX() - rect.centerX());
        } else {
            if (rect.left > win.left) {
                dHoming.setX(dHoming.getX() + win.left - rect.left);
            } else if (rect.right < win.right) {
                dHoming.setX(dHoming.getX() + win.right - rect.right);
            }
        }

        if (rect.height() < win.height()) {
            dHoming.setY(dHoming.getY() + win.centerY() - rect.centerY());
        } else {
            if (rect.top > win.top) {
                dHoming.setY(dHoming.getY() + win.top - rect.top);
            } else if (rect.bottom < win.bottom) {
                dHoming.setY(dHoming.getY() + win.bottom - rect.bottom);
            }
        }

        return dHoming;
    }

    /**
     * 计算框架矩形填充窗口的归位信息（可能会裁剪部分内容）
     * 确保框架完全覆盖窗口，适用于需要充满整个显示区域的场景
     * 
     * @param win 目标窗口矩形
     * @param frame 要填充的框架矩形
     * @return 归位信息对象，包含平移和缩放参数
     */
    public static ImageHoming fillHoming(RectF win, RectF frame) {
        ImageHoming dHoming = new ImageHoming(0, 0, 1, 0);
        if (frame.contains(win)) {
            // 不需要Fill
            return dHoming;
        }

        if (frame.width() < win.width() || frame.height() < win.height()) {
            dHoming.setScale(Math.max(win.width() / frame.width(), win.height() / frame.height()));
        }

        RectF rect = new RectF();
        M.setScale(dHoming.getScale(), dHoming.getScale(), frame.centerX(), frame.centerY());
        M.mapRect(rect, frame);

        if (rect.left > win.left) {
            dHoming.setX(dHoming.getX() + win.left - rect.left);
        } else if (rect.right < win.right) {
            dHoming.setX(dHoming.getX() + win.right - rect.right);
        }

        if (rect.top > win.top) {
            dHoming.setY(dHoming.getY() + win.top - rect.top);
        } else if (rect.bottom < win.bottom) {
            dHoming.setY(dHoming.getY() + win.bottom - rect.bottom);
        }

        return dHoming;
    }

    /**
     * 计算框架矩形填充窗口的归位信息（可能会裁剪部分内容），使用自定义中心点
     * 确保框架完全覆盖窗口，适用于需要充满整个显示区域的场景
     * 
     * @param win 目标窗口矩形
     * @param frame 要填充的框架矩形
     * @param pivotX 缩放中心点的X坐标
     * @param pivotY 缩放中心点的Y坐标
     * @return 归位信息对象，包含平移和缩放参数
     */
    public static ImageHoming fillHoming(RectF win, RectF frame, float pivotX, float pivotY) {
        ImageHoming dHoming = new ImageHoming(0, 0, 1, 0);
        if (frame.contains(win)) {
            // 不需要Fill
            return dHoming;
        }

        if (frame.width() < win.width() || frame.height() < win.height()) {
            dHoming.setScale(Math.max(win.width() / frame.width(), win.height() / frame.height()));
        }

        RectF rect = new RectF();
        M.setScale(dHoming.getScale(), dHoming.getScale(), pivotX, pivotY);
        M.mapRect(rect, frame);

        if (rect.left > win.left) {
            dHoming.setX(dHoming.getX() + win.left - rect.left);
        } else if (rect.right < win.right) {
            dHoming.setX(dHoming.getX() + win.right - rect.right);
        }

        if (rect.top > win.top) {
            dHoming.setY(dHoming.getY() + win.top - rect.top);
        } else if (rect.bottom < win.bottom) {
            dHoming.setY(dHoming.getY() + win.bottom - rect.bottom);
        }

        return dHoming;
    }

    /**
     * 将框架矩形填充到目标窗口（可能会裁剪部分内容），返回归位信息
     * 确保框架完全覆盖窗口，适用于需要充满整个显示区域的场景
     * 
     * @param win 目标窗口矩形
     * @param frame 要填充的框架矩形
     * @return 归位信息对象，包含平移和缩放参数
     */
    public static ImageHoming fill(RectF win, RectF frame) {
        ImageHoming dHoming = new ImageHoming(0, 0, 1, 0);

        if (win.equals(frame)) {
            return dHoming;
        }

        // 第一次时缩放到裁剪区域内
        dHoming.setScale(Math.max(win.width() / frame.width(), win.height() / frame.height()));

        RectF rect = new RectF();
        M.setScale(dHoming.getScale(), dHoming.getScale(), frame.centerX(), frame.centerY());
        M.mapRect(rect, frame);

        dHoming.setX(dHoming.getX() + win.centerX() - rect.centerX());
        dHoming.setY(dHoming.getY() + win.centerY() - rect.centerY());

        return dHoming;
    }

    /**
     * 计算图像采样率，确保返回的是2的幂次方
     * Android BitmapFactory解码时使用2的幂次方采样率能获得更好的性能
     * @param rawSampleSize 原始采样率
     * @return 调整后的采样率（2的幂次方）
     */
    public static int inSampleSize(int rawSampleSize) {
        int raw = rawSampleSize, ans = 1;
        // 通过位运算计算不大于原始采样率的最大2的幂次方
        while (raw > 1) {
            ans <<= 1;  // 相当于ans = ans * 2
            raw >>= 1;  // 相当于raw = raw / 2
        }

        // 如果结果与原始采样率不相等，则将结果乘以2
        if (ans != rawSampleSize) {
            ans <<= 1;
        }

        return ans;
    }

    /**
     * 将框架矩形填充到目标窗口（可能会裁剪部分内容）
     * @param win 目标窗口矩形
     * @param frame 要填充的框架矩形
     */
    public static void rectFill(RectF win, RectF frame) {
        if (win.equals(frame)) {
            return;
        }

        // 计算最大缩放比例，确保框架完全覆盖窗口
        float scale = Math.max(win.width() / frame.width(), win.height() / frame.height());

        // 以框架中心点为原点进行缩放
        M.setScale(scale, scale, frame.centerX(), frame.centerY());
        // 应用缩放变换到框架矩形
        M.mapRect(frame);

        if (frame.left > win.left) {
            frame.left = win.left;
        } else if (frame.right < win.right) {
            frame.right = win.right;
        }

        if (frame.top > win.top) {
            frame.top = win.top;
        } else if (frame.bottom < win.bottom) {
            frame.bottom = win.bottom;
        }
    }
}
