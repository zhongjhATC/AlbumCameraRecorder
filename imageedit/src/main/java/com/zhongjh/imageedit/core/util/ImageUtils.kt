package com.zhongjh.imageedit.core.util

import android.graphics.Matrix
import android.graphics.RectF
import com.zhongjh.imageedit.core.homing.ImageHoming
import kotlin.math.max
import kotlin.math.min

/**
 * 图像处理工具类，提供图像坐标变换、缩放适配、居中对齐等常用功能
 * 用于支持图像编辑器中的各种交互和显示操作
 * @author zhongjh
 * @date 2025/10/13
 */
object ImageUtils {
    /**
     * 用于矩阵变换的静态实例，避免频繁创建对象
     */
    private val M = Matrix()

    /**
     * 将一个矩形居中放置在另一个矩形内部
     * @param win 目标窗口矩形
     * @param frame 要居中的框架矩形
     */
    fun center(win: RectF, frame: RectF) {
        // 计算并应用偏移量，使frame在win中居中
        frame.offset(win.centerX() - frame.centerX(), win.centerY() - frame.centerY())
    }

    /**
     * 将框架矩形等比例缩放到适应窗口，保持中心点不变（带统一内边距）
     * @param win 目标窗口矩形
     * @param frame 要缩放的框架矩形
     * @param padding 四周统一的内边距值
     */
    /**
     * 将框架矩形等比例缩放到适应窗口，保持中心点不变（无内边距）
     * @param win 目标窗口矩形
     * @param frame 要缩放的框架矩形
     */
    @JvmOverloads
    fun fitCenter(win: RectF, frame: RectF, padding: Float = 0f) {
        fitCenter(win, frame, padding, padding, padding, padding)
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
    fun fitCenter(win: RectF, frame: RectF, paddingLeft: Float, paddingTop: Float, paddingRight: Float, paddingBottom: Float) {
        var paddingLeftVar = paddingLeft
        var paddingTopVar = paddingTop
        var paddingRightVar = paddingRight
        var paddingBottomVar = paddingBottom
        if (win.isEmpty || frame.isEmpty) {
            return
        }

        if (win.width() < paddingLeftVar + paddingRightVar) {
            paddingRightVar = 0f
            paddingLeftVar = paddingRightVar
            // 忽略Padding 值
        }

        if (win.height() < paddingTopVar + paddingBottomVar) {
            paddingBottomVar = 0f
            paddingTopVar = paddingBottomVar
            // 忽略Padding 值
        }

        val w = win.width() - paddingLeftVar - paddingRightVar
        val h = win.height() - paddingTopVar - paddingBottomVar

        val scale = min((w / frame.width()).toDouble(), (h / frame.height()).toDouble()).toFloat()

        // 缩放FIT
        frame[0f, 0f, frame.width() * scale] = frame.height() * scale

        // 中心对齐
        frame.offset(
            win.centerX() + (paddingLeftVar - paddingRightVar) / 2 - frame.centerX(),
            win.centerY() + (paddingTopVar - paddingBottomVar) / 2 - frame.centerY()
        )
    }

    /**
     * 计算框架矩形适应窗口的归位信息（居中缩放模式）
     * @param win 目标窗口矩形
     * @param frame 要缩放的框架矩形
     * @return 归位信息对象，包含平移和缩放参数
     */
    fun fitHoming(win: RectF, frame: RectF): ImageHoming {
        // 创建默认归位信息对象（无平移、无缩放、无旋转）
        val dHoming = ImageHoming(0f, 0f, 1f, 0f)

        // 框架完全包含窗口时，不需要缩放适配
        if (frame.contains(win)) {
            return dHoming
        }

        // 只有当框架的宽高都小于窗口时，才进行放大操作
        if (frame.width() < win.width() && frame.height() < win.height()) {
            // 计算最小缩放比例，确保框架完全适应窗口
            dHoming.scale = min((win.width() / frame.width()).toDouble(), (win.height() / frame.height()).toDouble()).toFloat()
        }

        val rect = RectF()
        M.setScale(dHoming.scale, dHoming.scale, frame.centerX(), frame.centerY())
        M.mapRect(rect, frame)

        if (rect.width() < win.width()) {
            dHoming.x = dHoming.x + win.centerX() - rect.centerX()
        } else {
            if (rect.left > win.left) {
                dHoming.x = dHoming.x + win.left - rect.left
            } else if (rect.right < win.right) {
                dHoming.x = dHoming.x + win.right - rect.right
            }
        }

        if (rect.height() < win.height()) {
            dHoming.y = dHoming.y + win.centerY() - rect.centerY()
        } else {
            if (rect.top > win.top) {
                dHoming.y = dHoming.y + win.top - rect.top
            } else if (rect.bottom < win.bottom) {
                dHoming.y = dHoming.y + win.bottom - rect.bottom
            }
        }

        return dHoming
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
    fun fitHoming(win: RectF, frame: RectF, centerX: Float, centerY: Float): ImageHoming {
        val dHoming = ImageHoming(0f, 0f, 1f, 0f)

        if (frame.contains(win)) {
            // 不需要Fit
            return dHoming
        }

        // 宽高都小于Win，才有必要放大
        if (frame.width() < win.width() && frame.height() < win.height()) {
            dHoming.scale = min((win.width() / frame.width()).toDouble(), (win.height() / frame.height()).toDouble()).toFloat()
        }

        val rect = RectF()
        M.setScale(dHoming.scale, dHoming.scale, centerX, centerY)
        M.mapRect(rect, frame)

        if (rect.width() < win.width()) {
            dHoming.x = dHoming.x + win.centerX() - rect.centerX()
        } else {
            if (rect.left > win.left) {
                dHoming.x = dHoming.x + win.left - rect.left
            } else if (rect.right < win.right) {
                dHoming.x = dHoming.x + win.right - rect.right
            }
        }

        if (rect.height() < win.height()) {
            dHoming.y = dHoming.y + win.centerY() - rect.centerY()
        } else {
            if (rect.top > win.top) {
                dHoming.y = dHoming.y + win.top - rect.top
            } else if (rect.bottom < win.bottom) {
                dHoming.y = dHoming.y + win.bottom - rect.bottom
            }
        }

        return dHoming
    }


    /**
     * 计算框架矩形适应窗口的归位信息（居中缩放模式），支持强制内部适配
     *
     * @param win 目标窗口矩形
     * @param frame 要缩放的框架矩形
     * @param isJustInner 是否强制框架完全包含在窗口内部，即使框架尺寸大于窗口
     * @return 归位信息对象，包含平移和缩放参数
     */
    fun fitHoming(win: RectF, frame: RectF, isJustInner: Boolean): ImageHoming {
        val dHoming = ImageHoming(0f, 0f, 1f, 0f)

        if (frame.contains(win) && !isJustInner) {
            // 不需要Fit
            return dHoming
        }

        // 宽高都小于Win，才有必要放大
        val isScale = isJustInner || frame.width() < win.width() && frame.height() < win.height()
        if (isScale) {
            dHoming.scale = min((win.width() / frame.width()).toDouble(), (win.height() / frame.height()).toDouble()).toFloat()
        }

        val rect = RectF()
        M.setScale(dHoming.scale, dHoming.scale, frame.centerX(), frame.centerY())
        M.mapRect(rect, frame)

        if (rect.width() < win.width()) {
            dHoming.x = dHoming.x + win.centerX() - rect.centerX()
        } else {
            if (rect.left > win.left) {
                dHoming.x = dHoming.x + win.left - rect.left
            } else if (rect.right < win.right) {
                dHoming.x = dHoming.x + win.right - rect.right
            }
        }

        if (rect.height() < win.height()) {
            dHoming.y = dHoming.y + win.centerY() - rect.centerY()
        } else {
            if (rect.top > win.top) {
                dHoming.y = dHoming.y + win.top - rect.top
            } else if (rect.bottom < win.bottom) {
                dHoming.y = dHoming.y + win.bottom - rect.bottom
            }
        }

        return dHoming
    }

    /**
     * 计算框架矩形填充窗口的归位信息（可能会裁剪部分内容）
     * 确保框架完全覆盖窗口，适用于需要充满整个显示区域的场景
     *
     * @param win 目标窗口矩形
     * @param frame 要填充的框架矩形
     * @return 归位信息对象，包含平移和缩放参数
     */
    fun fillHoming(win: RectF, frame: RectF): ImageHoming {
        val dHoming = ImageHoming(0f, 0f, 1f, 0f)
        if (frame.contains(win)) {
            // 不需要Fill
            return dHoming
        }

        if (frame.width() < win.width() || frame.height() < win.height()) {
            dHoming.scale = max((win.width() / frame.width()).toDouble(), (win.height() / frame.height()).toDouble()).toFloat()
        }

        val rect = RectF()
        M.setScale(dHoming.scale, dHoming.scale, frame.centerX(), frame.centerY())
        M.mapRect(rect, frame)

        if (rect.left > win.left) {
            dHoming.x = dHoming.x + win.left - rect.left
        } else if (rect.right < win.right) {
            dHoming.x = dHoming.x + win.right - rect.right
        }

        if (rect.top > win.top) {
            dHoming.y = dHoming.y + win.top - rect.top
        } else if (rect.bottom < win.bottom) {
            dHoming.y = dHoming.y + win.bottom - rect.bottom
        }

        return dHoming
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
    fun fillHoming(win: RectF, frame: RectF, pivotX: Float, pivotY: Float): ImageHoming {
        val dHoming = ImageHoming(0f, 0f, 1f, 0f)
        if (frame.contains(win)) {
            // 不需要Fill
            return dHoming
        }

        if (frame.width() < win.width() || frame.height() < win.height()) {
            dHoming.scale = max((win.width() / frame.width()).toDouble(), (win.height() / frame.height()).toDouble()).toFloat()
        }

        val rect = RectF()
        M.setScale(dHoming.scale, dHoming.scale, pivotX, pivotY)
        M.mapRect(rect, frame)

        if (rect.left > win.left) {
            dHoming.x = dHoming.x + win.left - rect.left
        } else if (rect.right < win.right) {
            dHoming.x = dHoming.x + win.right - rect.right
        }

        if (rect.top > win.top) {
            dHoming.y = dHoming.y + win.top - rect.top
        } else if (rect.bottom < win.bottom) {
            dHoming.y = dHoming.y + win.bottom - rect.bottom
        }

        return dHoming
    }

    /**
     * 将框架矩形填充到目标窗口（可能会裁剪部分内容），返回归位信息
     * 确保框架完全覆盖窗口，适用于需要充满整个显示区域的场景
     *
     * @param win 目标窗口矩形
     * @param frame 要填充的框架矩形
     * @return 归位信息对象，包含平移和缩放参数
     */
    fun fill(win: RectF, frame: RectF): ImageHoming {
        val dHoming = ImageHoming(0f, 0f, 1f, 0f)

        if (win == frame) {
            return dHoming
        }

        // 第一次时缩放到裁剪区域内
        dHoming.scale = max((win.width() / frame.width()).toDouble(), (win.height() / frame.height()).toDouble()).toFloat()

        val rect = RectF()
        M.setScale(dHoming.scale, dHoming.scale, frame.centerX(), frame.centerY())
        M.mapRect(rect, frame)

        dHoming.x = dHoming.x + win.centerX() - rect.centerX()
        dHoming.y = dHoming.y + win.centerY() - rect.centerY()

        return dHoming
    }

    /**
     * 计算图像采样率，确保返回的是2的幂次方
     * Android BitmapFactory解码时使用2的幂次方采样率能获得更好的性能
     * @param rawSampleSize 原始采样率
     * @return 调整后的采样率（2的幂次方）
     */
    fun inSampleSize(rawSampleSize: Int): Int {
        var raw = rawSampleSize
        var ans = 1
        // 通过位运算计算不大于原始采样率的最大2的幂次方
        while (raw > 1) {
            ans = ans shl 1 // 相当于ans = ans * 2
            raw = raw shr 1 // 相当于raw = raw / 2
        }

        // 如果结果与原始采样率不相等，则将结果乘以2
        if (ans != rawSampleSize) {
            ans = ans shl 1
        }

        return ans
    }

    /**
     * 将框架矩形填充到目标窗口（可能会裁剪部分内容）
     * @param win 目标窗口矩形
     * @param frame 要填充的框架矩形
     */
    fun rectFill(win: RectF, frame: RectF) {
        if (win == frame) {
            return
        }

        // 计算最大缩放比例，确保框架完全覆盖窗口
        val scale = max((win.width() / frame.width()).toDouble(), (win.height() / frame.height()).toDouble()).toFloat()

        // 以框架中心点为原点进行缩放
        M.setScale(scale, scale, frame.centerX(), frame.centerY())
        // 应用缩放变换到框架矩形
        M.mapRect(frame)

        if (frame.left > win.left) {
            frame.left = win.left
        } else if (frame.right < win.right) {
            frame.right = win.right
        }

        if (frame.top > win.top) {
            frame.top = win.top
        } else if (frame.bottom < win.bottom) {
            frame.bottom = win.bottom
        }
    }
}
