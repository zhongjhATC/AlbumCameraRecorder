package com.zhongjh.imageedit.core.clip

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import com.zhongjh.imageedit.core.clip.ImageClip.Companion.CLIP_CELL_STRIDES
import com.zhongjh.imageedit.core.clip.ImageClip.Companion.CLIP_CORNERS
import com.zhongjh.imageedit.core.clip.ImageClip.Companion.CLIP_CORNER_SIZE
import com.zhongjh.imageedit.core.clip.ImageClip.Companion.CLIP_CORNER_SIZES
import com.zhongjh.imageedit.core.clip.ImageClip.Companion.CLIP_CORNER_STEPS
import com.zhongjh.imageedit.core.clip.ImageClip.Companion.CLIP_CORNER_STRIDES
import com.zhongjh.imageedit.core.clip.ImageClip.Companion.CLIP_MARGIN
import com.zhongjh.imageedit.core.clip.ImageClip.Companion.CLIP_SIZE_RATIO
import com.zhongjh.imageedit.core.clip.ImageClip.Companion.CLIP_THICKNESS_CELL
import com.zhongjh.imageedit.core.clip.ImageClip.Companion.CLIP_THICKNESS_FRAME
import com.zhongjh.imageedit.core.clip.ImageClip.Companion.CLIP_THICKNESS_SEWING
import com.zhongjh.imageedit.core.constants.Anchor
import com.zhongjh.imageedit.core.util.ImageUtils
import kotlin.math.abs


/**
 * 图像裁剪窗口实现类，负责裁剪框的绘制、交互和状态管理
 * @author zhongjh
 * @date 2025/09/01
 */
class ImageClipWindow : ImageClip {

    /**
     * 当前裁剪区域的矩形范围（实时更新）
     */
    private val mFrame = RectF()

    /**
     * 归位动画开始前的裁剪区域（用于动画计算）
     */
    private val mBaseFrame = RectF()

    /**
     * 归位动画的目标裁剪区域（用于动画计算）
     */
    private val mTargetFrame = RectF()

    /**
     * 裁剪窗口的有效区域（用户可操作的裁剪范围）
     */
    private val mWinFrame = RectF()

    /**
     * 整个裁剪视图的窗口范围（包含上下边距）
     */
    private val mWin = RectF()

    /**
     * 裁剪框内部网格线的坐标数组（用于绘制网格）
     */
    private val mCells = FloatArray(16)

    /**
     * 裁剪框边角线条的坐标数组（用于绘制边角）
     */
    private val mCorners = FloatArray(32)

    /**
     * 基础尺寸数组，存储宽高方向的比例计算结果
     * 格式: mBaseSizes[0] = 宽度相关比例值, mBaseSizes[1] = 高度相关比例值
     */
    private val mBaseSizes = Array(2) { FloatArray(4) }

    /**
     * 是否正在裁剪操作中（用户拖拽裁剪框时为true）
     */
    var isClipping = false

    /**
     * 是否正在重置裁剪状态（初始化或重置时为true）
     */
    var isResetting = true

    /**
     * 是否显示裁剪区域外的遮罩层
     */
    var isShowShade = false

    /**
     * 是否正在执行归位动画（裁剪框自动回到中心位置时为true）
     */
    var isHoming = false

    /**
     * 矩阵对象，用于图像旋转等变换计算
     */
    private val mMatrix = Matrix()

    /**
     * 绘制裁剪框的画笔对象
     */
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * 垂直方向窗口比例（裁剪窗口高度占整个视图高度的比例）
     */
    private companion object {
        const val VERTICAL_RATIO = 0.8f
        const val COLOR_CELL = 0x80FFFFFF // 网格线颜色（半透明白色）
        const val COLOR_FRAME = Color.WHITE // 边框颜色（白色）
        const val COLOR_CORNER = Color.WHITE // 边角线条颜色（白色）
    }

    init {
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeCap = Paint.Cap.SQUARE
    }

    /**
     * 设置裁剪窗口的大小，并初始化裁剪框位置
     * @param width 窗口宽度
     * @param height 窗口高度
     */
    fun setClipWinSize(width: Float, height: Float) {
        mWin.set(0f, 0f, width, height)
        mWinFrame.set(0f, 0f, width, height * VERTICAL_RATIO)

        if (!mFrame.isEmpty) {
            ImageUtils.center(mWinFrame, mFrame)
            mTargetFrame.set(mFrame)
        }
    }

    /**
     * 根据图像尺寸和旋转角度重置裁剪框
     * @param clipImage 原始图像的矩形范围
     * @param rotate 图像旋转角度（度）
     */
    fun reset(clipImage: RectF, rotate: Float) {
        val imgRect = RectF()
        mMatrix.setRotate(rotate, clipImage.centerX(), clipImage.centerY())
        mMatrix.mapRect(imgRect, clipImage)
        reset(imgRect.width(), imgRect.height())
    }

    /**
     * 重置裁剪框到初始状态（根据图像尺寸居中显示）
     * @param clipWidth 图像宽度（考虑旋转后）
     * @param clipHeight 图像高度（考虑旋转后）
     */
    private fun reset(clipWidth: Float, clipHeight: Float) {
        isResetting = true
        mFrame.set(0f, 0f, clipWidth, clipHeight)
        ImageUtils.fitCenter(mWinFrame, mFrame, CLIP_MARGIN)
        mTargetFrame.set(mFrame)
    }

    /**
     * 启动裁剪框归位动画（将裁剪框移动到窗口中心）
     * @return 是否需要执行归位动画（true表示裁剪框不在中心位置）
     */
    fun homing(): Boolean {
        mBaseFrame.set(mFrame)
        mTargetFrame.set(mFrame)
        ImageUtils.fitCenter(mWinFrame, mTargetFrame, CLIP_MARGIN)
        return isHoming.also { isHoming = mTargetFrame != mBaseFrame }
    }

    /**
     * 执行归位动画的帧更新
     * @param fraction 动画进度（0~1）
     */
    fun homing(fraction: Float) {
        if (isHoming) {
            mFrame.set(
                mBaseFrame.left + (mTargetFrame.left - mBaseFrame.left) * fraction,
                mBaseFrame.top + (mTargetFrame.top - mBaseFrame.top) * fraction,
                mBaseFrame.right + (mTargetFrame.right - mBaseFrame.right) * fraction,
                mBaseFrame.bottom + (mTargetFrame.bottom - mBaseFrame.bottom) * fraction
            )
        }
    }

    /**
     * 获取偏移后的裁剪区域
     * @param offsetX X方向偏移量
     * @param offsetY Y方向偏移量
     * @return 偏移后的裁剪区域矩形
     */
    fun getOffsetFrame(offsetX: Float, offsetY: Float): RectF {
        val frame = RectF(mFrame)
        frame.offset(offsetX, offsetY)
        return frame
    }

    /**
     * 获取归位动画的目标裁剪区域
     * @return 目标区域矩形
     */
    fun getTargetFrame(): RectF {
        return mTargetFrame
    }

    /**
     * 绘制裁剪框（网格线、边框、边角）
     * @param canvas 绘制画布
     */
    fun onDraw(canvas: Canvas) {
        if (isResetting) {
            return
        }

        val size = floatArrayOf(mFrame.width(), mFrame.height())
        for (i in mBaseSizes.indices) {
            for (j in mBaseSizes[i].indices) {
                mBaseSizes[i][j] = size[i] * CLIP_SIZE_RATIO[j]
            }
        }

        // 计算网格线坐标
        for (i in mCells.indices) {
            mCells[i] = mBaseSizes[i and 1][CLIP_CELL_STRIDES ushr (i shl 1) and 3]
        }

        // 计算边角线条坐标
        for (i in mCorners.indices) {
            mCorners[i] = mBaseSizes[i and 1][CLIP_CORNER_STRIDES ushr i and 1] +
                    CLIP_CORNER_SIZES[CLIP_CORNERS[i].toInt() and 3] +
                    CLIP_CORNER_STEPS[CLIP_CORNERS[i].toInt() shr 2]
        }

        // 绘制内部网格线
        canvas.translate(mFrame.left, mFrame.top)
        mPaint.style = Paint.Style.STROKE
        mPaint.color = COLOR_CELL.toInt()
        mPaint.strokeWidth = CLIP_THICKNESS_CELL
        canvas.drawLines(mCells, mPaint)

        // 绘制外边框
        canvas.translate(-mFrame.left, -mFrame.top)
        mPaint.color = COLOR_FRAME
        mPaint.strokeWidth = CLIP_THICKNESS_FRAME
        canvas.drawRect(mFrame, mPaint)

        // 绘制边角线条
        canvas.translate(mFrame.left, mFrame.top)
        mPaint.color = COLOR_CORNER
        mPaint.strokeWidth = CLIP_THICKNESS_SEWING
        canvas.drawLines(mCorners, mPaint)
    }

    /**
     * 判断触摸点是否在裁剪框的边角控制点上
     * @param x 触摸点X坐标
     * @param y 触摸点Y坐标
     * @return 对应的边角锚点（null表示不在控制点上）
     */
    fun getAnchor(x: Float, y: Float): Anchor? {
        if (Anchor.isCohesionContains(mFrame, -CLIP_CORNER_SIZE, x, y)
            && !Anchor.isCohesionContains(mFrame, CLIP_CORNER_SIZE, x, y)
        ) {
            var v = 0
            val cohesion = Anchor.cohesion(mFrame, 0f)
            val pos = floatArrayOf(x, y)
            for (i in cohesion.indices) {
                if (abs(cohesion[i] - pos[i shr 1]) < CLIP_CORNER_SIZE) {
                    v = v or (1 shl i)
                }
            }

            val anchor = Anchor.valueOf(v)
            if (anchor != null) {
                isHoming = false
            }
            return anchor
        }
        return null
    }

    /**
     * 处理裁剪框的拖拽事件
     * @param anchor 拖拽的边角锚点
     * @param dx X方向拖拽距离
     * @param dy Y方向拖拽距离
     */
    fun onScroll(anchor: Anchor, dx: Float, dy: Float) {
        anchor.move(mWinFrame, mFrame, dx, dy)
    }
}