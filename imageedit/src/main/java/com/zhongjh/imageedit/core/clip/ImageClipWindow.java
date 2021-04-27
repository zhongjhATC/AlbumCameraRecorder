package com.zhongjh.imageedit.core.clip;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.zhongjh.imageedit.core.util.ImageUtils;


/**
 *
 * @author felix
 * @date 2017/11/29 下午5:41
 */
public class ImageClipWindow implements ImageClip {

    /**
     * 裁剪区域
     */
    private final RectF mFrame = new RectF();

    private final RectF mBaseFrame = new RectF();

    private final RectF mTargetFrame = new RectF();

    /**
     * 裁剪窗口
     */
    private final RectF mWinFrame = new RectF();

    private final RectF mWin = new RectF();

    private final float[] mCells = new float[16];

    private final float[] mCorners = new float[32];

    private final float[][] mBaseSizes = new float[2][4];

    /**
     * 是否在裁剪中
     */
    private boolean isClipping = false;

    private boolean isResetting = true;

    private boolean isShowShade = false;

    private boolean isHoming = false;

    private final Matrix mMatrix = new Matrix();

    private final Path mShadePath = new Path();

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * 垂直窗口比例
     */
    private static final float VERTICAL_RATIO = 0.8f;

    private static final int COLOR_CELL = 0x80FFFFFF;

    private static final int COLOR_FRAME = Color.WHITE;

    private static final int COLOR_CORNER = Color.WHITE;

    private static final int COLOR_SHADE = 0xCC000000;

    {
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.SQUARE);
    }

    public ImageClipWindow() {

    }

    /**
     * 计算裁剪窗口区域
     */
    public void setClipWinSize(float width, float height) {
        mWin.set(0, 0, width, height);
        mWinFrame.set(0, 0, width, height * VERTICAL_RATIO);

        if (!mFrame.isEmpty()) {
            ImageUtils.center(mWinFrame, mFrame);
            mTargetFrame.set(mFrame);
        }
    }

    public void reset(RectF clipImage, float rotate) {
        RectF imgRect = new RectF();
        mMatrix.setRotate(rotate, clipImage.centerX(), clipImage.centerY());
        mMatrix.mapRect(imgRect, clipImage);
        reset(imgRect.width(), imgRect.height());
    }

    /**
     * 重置裁剪
     */
    private void reset(float clipWidth, float clipHeight) {
        setResetting(true);
        mFrame.set(0, 0, clipWidth, clipHeight);
        ImageUtils.fitCenter(mWinFrame, mFrame, CLIP_MARGIN);
        mTargetFrame.set(mFrame);
    }

    public boolean homing() {
        mBaseFrame.set(mFrame);
        mTargetFrame.set(mFrame);
        ImageUtils.fitCenter(mWinFrame, mTargetFrame, CLIP_MARGIN);
        return isHoming = !mTargetFrame.equals(mBaseFrame);
    }

    public void homing(float fraction) {
        if (isHoming) {
            mFrame.set(
                    mBaseFrame.left + (mTargetFrame.left - mBaseFrame.left) * fraction,
                    mBaseFrame.top + (mTargetFrame.top - mBaseFrame.top) * fraction,
                    mBaseFrame.right + (mTargetFrame.right - mBaseFrame.right) * fraction,
                    mBaseFrame.bottom + (mTargetFrame.bottom - mBaseFrame.bottom) * fraction
            );
        }
    }

    public boolean isHoming() {
        return isHoming;
    }

    public void setHoming(boolean homing) {
        isHoming = homing;
    }

    public boolean isClipping() {
        return isClipping;
    }

    public void setClipping(boolean clipping) {
        isClipping = clipping;
    }

    public boolean isResetting() {
        return isResetting;
    }

    public void setResetting(boolean resetting) {
        isResetting = resetting;
    }

    public RectF getFrame() {
        return mFrame;
    }

    public RectF getWinFrame() {
        return mWinFrame;
    }

    public RectF getOffsetFrame(float offsetX, float offsetY) {
        RectF frame = new RectF(mFrame);
        frame.offset(offsetX, offsetY);
        return frame;
    }

    public RectF getTargetFrame() {
        return mTargetFrame;
    }

    public RectF getOffsetTargetFrame(float offsetX, float offsetY) {
        RectF targetFrame = new RectF(mFrame);
        targetFrame.offset(offsetX, offsetY);
        return targetFrame;
    }

    public boolean isShowShade() {
        return isShowShade;
    }

    public void setShowShade(boolean showShade) {
        isShowShade = showShade;
    }

    public void onDraw(Canvas canvas) {

        if (isResetting) {
            return;
        }

        float[] size = {mFrame.width(), mFrame.height()};
        for (int i = 0; i < mBaseSizes.length; i++) {
            for (int j = 0; j < mBaseSizes[i].length; j++) {
                mBaseSizes[i][j] = size[i] * CLIP_SIZE_RATIO[j];
            }
        }

        for (int i = 0; i < mCells.length; i++) {
            mCells[i] = mBaseSizes[i & 1][CLIP_CELL_STRIDES >>> (i << 1) & 3];
        }

        for (int i = 0; i < mCorners.length; i++) {
            mCorners[i] = mBaseSizes[i & 1][CLIP_CORNER_STRIDES >>> i & 1]
                    + CLIP_CORNER_SIZES[CLIP_CORNERS[i] & 3] + CLIP_CORNER_STEPS[CLIP_CORNERS[i] >> 2];
        }

        canvas.translate(mFrame.left, mFrame.top);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(COLOR_CELL);
        mPaint.setStrokeWidth(CLIP_THICKNESS_CELL);
        canvas.drawLines(mCells, mPaint);

        canvas.translate(-mFrame.left, -mFrame.top);
        mPaint.setColor(COLOR_FRAME);
        mPaint.setStrokeWidth(CLIP_THICKNESS_FRAME);
        canvas.drawRect(mFrame, mPaint);

        canvas.translate(mFrame.left, mFrame.top);
        mPaint.setColor(COLOR_CORNER);
        mPaint.setStrokeWidth(CLIP_THICKNESS_SEWING);
        canvas.drawLines(mCorners, mPaint);
    }

    public void onDrawShade(Canvas canvas) {
        if (!isShowShade) {
            return;
        }

        // 计算遮罩图形
        mShadePath.reset();

        mShadePath.setFillType(Path.FillType.WINDING);
        mShadePath.addRect(mFrame.left + 100, mFrame.top + 100, mFrame.right - 100, mFrame.bottom - 100, Path.Direction.CW);

        mPaint.setColor(COLOR_SHADE);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(mShadePath, mPaint);
    }

    public Anchor getAnchor(float x, float y) {
        if (Anchor.isCohesionContains(mFrame, -CLIP_CORNER_SIZE, x, y)
                && !Anchor.isCohesionContains(mFrame, CLIP_CORNER_SIZE, x, y)) {
            int v = 0;
            float[] cohesion = Anchor.cohesion(mFrame, 0);
            float[] pos = {x, y};
            for (int i = 0; i < cohesion.length; i++) {
                if (Math.abs(cohesion[i] - pos[i >> 1]) < CLIP_CORNER_SIZE) {
                    v |= 1 << i;
                }
            }

            Anchor anchor = Anchor.valueOf(v);
            if (anchor != null) {
                isHoming = false;
            }
            return anchor;
        }
        return null;
    }

    public void onScroll(Anchor anchor, float dx, float dy) {
        anchor.move(mWinFrame, mFrame, dx, dy);
    }
}
