package com.zhongjh.imageedit.core.clip;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.zhongjh.imageedit.core.util.ImageUtils;


/**
 * 图像裁剪窗口类，实现了ImageClip接口
 * 负责处理图像编辑器中的裁剪区域显示、交互和绘制逻辑
 * 提供裁剪框调整、遮罩显示、锚点检测等功能
 * 
 * @author felix
 * @date 2017/11/29 下午5:41
 */
public class ImageClipWindow implements ImageClip {

    /**
     * 当前裁剪区域的矩形对象
     * 表示用户可调整的裁剪框位置和大小
     */
    private final RectF mFrame = new RectF();

    /**
     * 归位动画开始时的裁剪区域矩形
     * 用于计算归位动画过程中的中间状态
     */
    private final RectF mBaseFrame = new RectF();

    /**
     * 归位动画结束时的目标裁剪区域矩形
     * 用于计算归位动画过程中的中间状态
     */
    private final RectF mTargetFrame = new RectF();

    /**
     * 裁剪窗口区域的矩形对象
     * 定义了用户可操作的裁剪区域范围
     */
    private final RectF mWinFrame = new RectF();

    /**
     * 整个视图窗口的矩形对象
     * 用于确定裁剪窗口的初始位置和大小
     */
    private final RectF mWin = new RectF();

    /**
     * 裁剪框内部网格线的坐标数组
     * 用于绘制裁剪框内的辅助网格
     */
    private final float[] mCells = new float[16];

    /**
     * 裁剪框角点标记的坐标数组
     * 用于绘制裁剪框四个角上的调整标记
     */
    private final float[] mCorners = new float[32];

    /**
     * 基础尺寸数组
     * 用于计算裁剪框网格和角点标记的尺寸
     */
    private final float[][] mBaseSizes = new float[2][4];

    /**
     * 标记当前是否处于裁剪模式
     * 用于控制裁剪功能的启用状态
     */
    private boolean isClipping = false;

    /**
     * 标记当前是否正在重置裁剪框
     * 重置过程中不会绘制裁剪框
     */
    private boolean isResetting = true;

    /**
     * 标记是否显示裁剪框外的遮罩
     * 用于突出显示裁剪区域
     */
    private boolean isShowShade = false;

    /**
     * 标记当前是否正在执行归位动画
     * 归位动画会将裁剪框调整到合适的位置和大小
     */
    private boolean isHoming = false;

    /**
     * 矩阵对象，用于执行坐标变换操作
     * 主要用于处理旋转等复杂变换
     */
    private final Matrix mMatrix = new Matrix();

    /**
     * 遮罩路径对象
     * 用于绘制裁剪框外的半透明遮罩
     */
    private final Path mShadePath = new Path();

    /**
     * 画笔对象，用于绘制裁剪框、网格和角点
     * 设置了抗锯齿标志以提高绘制质量
     */
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * 垂直窗口比例
     * 定义了裁剪窗口高度占整个视图高度的比例
     */
    private static final float VERTICAL_RATIO = 0.8f;

    /**
     * 裁剪框内部网格线的颜色
     * 半透明白色，便于用户对齐图像
     */
    private static final int COLOR_CELL = 0x80FFFFFF; // 半透明白色

    /**
     * 裁剪框边框的颜色
     * 纯白色，使裁剪框清晰可见
     */
    private static final int COLOR_FRAME = Color.WHITE; // 纯白色

    /**
     * 裁剪框角点标记的颜色
     * 纯白色，使角点标记清晰可见
     */
    private static final int COLOR_CORNER = Color.WHITE; // 纯白色

    /**
     * 裁剪框外遮罩的颜色
     * 半透明黑色，用于突出显示裁剪区域
     */
    private static final int COLOR_SHADE = 0xCC000000; // 半透明黑色

    /**
     * 初始化代码块
     * 设置画笔的基本属性
     */
    {
        // 设置画笔为描边模式
        mPaint.setStyle(Paint.Style.STROKE);
        // 设置画笔笔触为方形，使线条连接更清晰
        mPaint.setStrokeCap(Paint.Cap.SQUARE);
    }

    /**
     * 默认构造函数
     * 创建一个新的裁剪窗口实例
     */
    public ImageClipWindow() {

    }

    /**
     * 设置裁剪窗口的尺寸
     * 初始化窗口区域并计算裁剪框的默认位置
     * 
     * @param width 视图宽度
     * @param height 视图高度
     */
    public void setClipWinSize(float width, float height) {
        // 设置整个视图窗口的尺寸
        mWin.set(0, 0, width, height);
        // 设置裁剪窗口的尺寸，高度为视图高度的80%
        mWinFrame.set(0, 0, width, height * VERTICAL_RATIO);

        // 如果裁剪框已存在，将其居中放置在裁剪窗口中
        if (!mFrame.isEmpty()) {
            ImageUtils.center(mWinFrame, mFrame);
            mTargetFrame.set(mFrame);
        }
    }

    /**
     * 重置裁剪框
     * 根据旋转后的图像尺寸重新计算裁剪框的大小和位置
     * 
     * @param clipImage 原始图像的矩形区域
     * @param rotate 图像的旋转角度
     */
    public void reset(RectF clipImage, float rotate) {
        // 创建一个新的矩形用于存储旋转后的图像尺寸
        RectF imgRect = new RectF();
        // 设置旋转矩阵，以图像中心点为旋转原点
        mMatrix.setRotate(rotate, clipImage.centerX(), clipImage.centerY());
        // 应用旋转变换，计算旋转后的图像边界
        mMatrix.mapRect(imgRect, clipImage);
        // 根据旋转后的图像尺寸重置裁剪框
        reset(imgRect.width(), imgRect.height());
    }

    /**
     * 重置裁剪框的私有方法
     * 设置裁剪框的初始大小并使其适应裁剪窗口
     * 
     * @param clipWidth 裁剪内容的宽度
     * @param clipHeight 裁剪内容的高度
     */
    private void reset(float clipWidth, float clipHeight) {
        // 标记为正在重置状态
        setResetting(true);
        // 设置裁剪框的初始大小为图像尺寸
        mFrame.set(0, 0, clipWidth, clipHeight);
        // 调整裁剪框使其适应裁剪窗口，留出指定的边距
        ImageUtils.fitCenter(mWinFrame, mFrame, CLIP_MARGIN);
        // 设置目标裁剪框为当前裁剪框
        mTargetFrame.set(mFrame);
    }

    /**
     * 开始裁剪框归位动画
     * 计算裁剪框应该归位到的目标位置
     * 
     * @return 是否需要执行归位动画
     */
    public boolean homing() {
        // 保存当前裁剪框位置作为归位起点
        mBaseFrame.set(mFrame);
        // 设置目标裁剪框为当前裁剪框
        mTargetFrame.set(mFrame);
        // 计算裁剪框应该归位到的目标位置
        ImageUtils.fitCenter(mWinFrame, mTargetFrame, CLIP_MARGIN);
        // 如果目标位置与当前位置不同，则需要执行归位动画
        return isHoming = !mTargetFrame.equals(mBaseFrame);
    }

    /**
     * 执行归位动画的一帧
     * 根据动画进度更新裁剪框位置
     * 
     * @param fraction 动画进度（0.0-1.0）
     */
    public void homing(float fraction) {
        // 只有在归位状态时才执行动画
        if (isHoming) {
            // 根据动画进度线性插值计算当前裁剪框的位置
            mFrame.set(
                    mBaseFrame.left + (mTargetFrame.left - mBaseFrame.left) * fraction,
                    mBaseFrame.top + (mTargetFrame.top - mBaseFrame.top) * fraction,
                    mBaseFrame.right + (mTargetFrame.right - mBaseFrame.right) * fraction,
                    mBaseFrame.bottom + (mTargetFrame.bottom - mBaseFrame.bottom) * fraction
            );
        }
    }

    /**
     * 获取当前是否正在执行归位动画
     * 
     * @return 是否正在归位
     */
    public boolean isHoming() {
        return isHoming;
    }

    /**
     * 设置是否正在执行归位动画
     * 
     * @param homing 是否正在归位
     */
    public void setHoming(boolean homing) {
        isHoming = homing;
    }

    /**
     * 获取当前是否处于裁剪模式
     * 
     * @return 是否在裁剪中
     */
    public boolean isClipping() {
        return isClipping;
    }

    /**
     * 设置当前是否处于裁剪模式
     * 
     * @param clipping 是否在裁剪中
     */
    public void setClipping(boolean clipping) {
        isClipping = clipping;
    }

    /**
     * 获取当前是否正在重置裁剪框
     * 
     * @return 是否正在重置
     */
    public boolean isResetting() {
        return isResetting;
    }

    /**
     * 设置当前是否正在重置裁剪框
     * 
     * @param resetting 是否正在重置
     */
    public void setResetting(boolean resetting) {
        isResetting = resetting;
    }

    /**
     * 获取当前裁剪区域的矩形对象
     * 
     * @return 裁剪区域矩形
     */
    public RectF getFrame() {
        return mFrame;
    }

    /**
     * 获取裁剪窗口区域的矩形对象
     * 
     * @return 裁剪窗口矩形
     */
    public RectF getWinFrame() {
        return mWinFrame;
    }

    /**
     * 获取偏移后的裁剪区域矩形
     * 
     * @param offsetX X轴偏移量
     * @param offsetY Y轴偏移量
     * @return 偏移后的裁剪区域矩形
     */
    public RectF getOffsetFrame(float offsetX, float offsetY) {
        // 创建一个新的矩形对象并设置为当前裁剪框
        RectF frame = new RectF(mFrame);
        // 应用偏移量
        frame.offset(offsetX, offsetY);
        // 返回偏移后的矩形
        return frame;
    }

    /**
     * 获取目标裁剪区域的矩形对象
     * 
     * @return 目标裁剪区域矩形
     */
    public RectF getTargetFrame() {
        return mTargetFrame;
    }

    /**
     * 获取偏移后的目标裁剪区域矩形
     * 
     * @param offsetX X轴偏移量
     * @param offsetY Y轴偏移量
     * @return 偏移后的目标裁剪区域矩形
     */
    public RectF getOffsetTargetFrame(float offsetX, float offsetY) {
        // 创建一个新的矩形对象并设置为目标裁剪框
        RectF targetFrame = new RectF(mTargetFrame);
        // 应用偏移量
        targetFrame.offset(offsetX, offsetY);
        // 返回偏移后的矩形
        return targetFrame;
    }

    /**
     * 获取是否显示裁剪框外的遮罩
     * 
     * @return 是否显示遮罩
     */
    public boolean isShowShade() {
        return isShowShade;
    }

    /**
     * 设置是否显示裁剪框外的遮罩
     * 
     * @param showShade 是否显示遮罩
     */
    public void setShowShade(boolean showShade) {
        isShowShade = showShade;
    }

    /**
     * 绘制裁剪框
     * 包括裁剪框边框、内部网格和角点标记
     * 
     * 绘制流程：
     * 1. 计算基础尺寸和网格线坐标
     * 2. 绘制裁剪框内部的辅助网格线（2条水平线，2条垂直线，形成九宫格）
     * 3. 绘制裁剪框的外边框
     * 4. 绘制裁剪框四个角上的调整标记（每个角有两条交叉线）
     * 
     * @param canvas 画布对象
     */
    public void onDraw(Canvas canvas) {

        // 如果正在重置，则不绘制
        if (isResetting) {
            return;
        }

        // 获取裁剪框的宽度和高度
        float[] size = {mFrame.width(), mFrame.height()};
        // 计算基础尺寸数组 - 根据预设比例计算裁剪框不同位置的参考尺寸
        for (int i = 0; i < mBaseSizes.length; i++) {
            for (int j = 0; j < mBaseSizes[i].length; j++) {
                // 根据预设比例计算基础尺寸（CLIP_SIZE_RATIO包含0,1,0.33,0.66四个值）
                mBaseSizes[i][j] = size[i] * CLIP_SIZE_RATIO[j];
            }
        }

        // 计算网格线坐标 - 生成裁剪框内的九宫格网格线
        for (int i = 0; i < mCells.length; i++) {
            // 通过位运算和移位操作获取对应维度的基础尺寸索引
            // 位运算可以高效地根据i的二进制位确定使用哪个基础尺寸
            mCells[i] = mBaseSizes[i & 1][CLIP_CELL_STRIDES >>> (i << 1) & 3];
        }

        // 计算角点标记坐标 - 生成裁剪框四个角上的调整标记坐标
        for (int i = 0; i < mCorners.length; i++) {
            // 复杂的位运算和查表操作，用于生成精确的角点标记位置
            mCorners[i] = mBaseSizes[i & 1][CLIP_CORNER_STRIDES >>> i & 1]
                    + CLIP_CORNER_SIZES[CLIP_CORNERS[i] & 3] + CLIP_CORNER_STEPS[CLIP_CORNERS[i] >> 2];
        }

        // 平移画布原点到裁剪框左上角 - 简化网格线坐标计算
        canvas.translate(mFrame.left, mFrame.top);
        // 设置画笔为描边模式
        mPaint.setStyle(Paint.Style.STROKE);
        // 设置画笔颜色为网格线颜色（半透明白色）
        mPaint.setColor(COLOR_CELL);
        // 设置画笔宽度为网格线宽度
        mPaint.setStrokeWidth(CLIP_THICKNESS_CELL);
        // 绘制网格线 - 使用之前计算好的坐标数组
        canvas.drawLines(mCells, mPaint);

        // 恢复画布原点 - 准备绘制边框
        canvas.translate(-mFrame.left, -mFrame.top);
        // 设置画笔颜色为裁剪框边框颜色（纯白色）
        mPaint.setColor(COLOR_FRAME);
        // 设置画笔宽度为裁剪框边框宽度
        mPaint.setStrokeWidth(CLIP_THICKNESS_FRAME);
        // 绘制裁剪框边框
        canvas.drawRect(mFrame, mPaint);

        // 再次平移画布原点到裁剪框左上角 - 简化角点标记坐标计算
        canvas.translate(mFrame.left, mFrame.top);
        // 设置画笔颜色为角点标记颜色（纯白色）
        mPaint.setColor(COLOR_CORNER);
        // 设置画笔宽度为角点标记线条宽度
        mPaint.setStrokeWidth(CLIP_THICKNESS_SEWING);
        // 绘制角点标记 - 使用之前计算好的坐标数组
        canvas.drawLines(mCorners, mPaint);
    }

    /**
     * 绘制裁剪框外的遮罩
     * 
     * 遮罩效果通过绘制裁剪框外的半透明黑色区域实现，突出显示裁剪区域
     * 注意：这里的实现实际上是在裁剪框内部绘制了一个稍小的矩形，通过Path.FillType.WINDING的特性
     * 配合后续绘制的其他内容，最终在视觉上形成裁剪框外的遮罩效果
     * 
     * @param canvas 画布对象
     */
    public void onDrawShade(Canvas canvas) {
        // 如果不显示遮罩，则直接返回
        if (!isShowShade) {
            return;
        }

        // 重置遮罩路径
        mShadePath.reset();

        // 设置路径填充类型为绕转填充 - 控制路径重叠区域的填充规则
        mShadePath.setFillType(Path.FillType.WINDING);
        // 添加一个矩形到路径中，作为遮罩区域
        // 在裁剪框内部添加了一个小100像素的矩形，配合外部绘制形成视觉上的遮罩效果
        mShadePath.addRect(mFrame.left + 100, mFrame.top + 100, mFrame.right - 100, mFrame.bottom - 100, Path.Direction.CW);

        // 设置画笔颜色为遮罩颜色（半透明黑色）
        mPaint.setColor(COLOR_SHADE);
        // 设置画笔为填充模式
        mPaint.setStyle(Paint.Style.FILL);
        // 绘制遮罩路径
        canvas.drawPath(mShadePath, mPaint);
    }

    /**
     * 根据触摸坐标获取对应的锚点
     * 用于确定用户正在调整裁剪框的哪个部分（如左上角、右侧边缘等）
     * 
     * 锚点检测算法：
     * 1. 首先检查触摸点是否在裁剪框的角点检测区域内（一个环形区域）
     * 2. 然后计算触摸点距离裁剪框各边界的距离
     * 3. 根据距离确定触摸点对应的锚点枚举值（通过位运算组合边界标记）
     * 
     * @param x 触摸点X坐标
     * @param y 触摸点Y坐标
     * @return 对应的锚点枚举值，如果不在锚点区域则返回null
     */
    public Anchor getAnchor(float x, float y) {
        // 检查触摸点是否在裁剪框的角点区域内
        // 使用两个同心区域的差集，形成一个环形检测区域
        if (Anchor.isCohesionContains(mFrame, -CLIP_CORNER_SIZE, x, y)
                && !Anchor.isCohesionContains(mFrame, CLIP_CORNER_SIZE, x, y)) {
            // 初始化锚点值（使用位掩码表示选中的边界）
            int v = 0;
            // 获取裁剪框的凝聚力点（四个边界坐标）
            float[] cohesion = Anchor.cohesion(mFrame, 0);
            // 存储触摸点坐标
            float[] pos = {x, y};
            // 检查触摸点与每个凝聚力点的距离
            for (int i = 0; i < cohesion.length; i++) {
                // 如果触摸点距离凝聚力点在指定范围内，则标记对应的位
                // i >> 1操作将索引映射到x/y坐标（0-1->x, 2-3->y）
                if (Math.abs(cohesion[i] - pos[i >> 1]) < CLIP_CORNER_SIZE) {
                    v |= 1 << i; // 通过位或操作标记选中的边界
                }
            }

            // 根据标记的位值获取对应的锚点枚举值
            Anchor anchor = Anchor.valueOf(v);
            // 如果找到了锚点，则取消归位状态（用户正在手动调整裁剪框）
            if (anchor != null) {
                isHoming = false;
            }
            // 返回找到的锚点
            return anchor;
        }
        return null;
    }

    /**
     * 处理裁剪框的滚动事件
     * 根据用户触摸的锚点和移动距离调整裁剪框的位置和大小
     * 
     * @param anchor 用户触摸的锚点
     * @param dx X轴移动距离
     * @param dy Y轴移动距离
     */
    public void onScroll(Anchor anchor, float dx, float dy) {
        // 调用锚点的移动方法，根据触摸事件调整裁剪框
        anchor.move(mWinFrame, mFrame, dx, dy);
    }
}
