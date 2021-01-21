package com.zhongjh.imageedit.core;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.Log;

import com.zhongjh.imageedit.core.clip.IMGClip;
import com.zhongjh.imageedit.core.clip.IMGClipWindow;
import com.zhongjh.imageedit.core.homing.IMGHoming;
import com.zhongjh.imageedit.core.sticker.IMGSticker;
import com.zhongjh.imageedit.core.util.IMGUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by felix on 2017/11/21 下午10:03.
 */

public class IMGImage {

    private static final String TAG = "IMGImage";

    private Bitmap mImage, mMosaicImage;

    /**
     * 完整图片边框
     */
    private RectF mFrame = new RectF();

    /**
     * 裁剪图片边框（显示的图片区域）
     */
    private RectF mClipFrame = new RectF();

    private RectF mTempClipFrame = new RectF();

    /**
     * 裁剪模式前状态备份
     */
    private RectF mBackupClipFrame = new RectF();

    private float mBackupClipRotate = 0;

    private float mRotate = 0, mTargetRotate = 0;

    private boolean isRequestToBaseFitting = false;

    private boolean isAnimCanceled = false;

    /**
     * 裁剪模式时当前触摸锚点
     */
    private IMGClip.Anchor mAnchor;

    private boolean isSteady = true;

    private Path mShade = new Path();

    /**
     * 裁剪窗口
     */
    private IMGClipWindow mClipWin = new IMGClipWindow();

    private boolean isDrawClip = false;

    /**
     * 编辑模式
     */
    private IMGMode mMode = IMGMode.NONE;

    /**
     * 是否冻结的
     */
    private boolean isFreezing = false;

    /**
     * 可视区域，无Scroll 偏移区域
     */
    private RectF mWindow = new RectF();

    /**
     * 是否初始位置
     */
    private boolean isInitialHoming = false;

    /**
     * 当前选中贴片
     */
    private IMGSticker mForeSticker;

    /**
     * 为被选中贴片
     */
    private List<IMGSticker> mBackStickers = new ArrayList<>();

    /**
     * 涂鸦路径
     */
    private List<IMGPath> mDoodles = new ArrayList<>();

    /**
     * 马赛克路径
     */
    private List<IMGPath> mMosaics = new ArrayList<>();

    private static final int MIN_SIZE = 500;

    private static final int MAX_SIZE = 10000;

    private Paint mPaint, mMosaicPaint, mShadePaint;

    private Matrix M = new Matrix();

    private static final boolean DEBUG = false;

    private static final Bitmap DEFAULT_IMAGE;

    private static final int COLOR_SHADE = 0xCC000000;

    static {
        DEFAULT_IMAGE = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    }

    {
        mShade.setFillType(Path.FillType.WINDING);

        // Doodle&Mosaic 's paint
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(IMGPath.BASE_DOODLE_WIDTH);
        mPaint.setColor(Color.RED);
        mPaint.setPathEffect(new CornerPathEffect(IMGPath.BASE_DOODLE_WIDTH));
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
    }

    public IMGImage() {
        Log.d(TAG, "IMGImage");
        mImage = DEFAULT_IMAGE;

        if (mMode == IMGMode.CLIP) {
            initShadePaint();
        }
    }

    public void setBitmap(Bitmap bitmap) {
        Log.d(TAG, "setBitmap");
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }

        this.mImage = bitmap;

        // 清空马赛克图层
        if (mMosaicImage != null) {
            mMosaicImage.recycle();
        }
        this.mMosaicImage = null;

        makeMosaicBitmap();

        onImageChanged();
    }

    public IMGMode getMode() {
        Log.d(TAG, "getMode");
        return mMode;
    }

    public void setMode(IMGMode mode) {
        Log.d(TAG, "setMode");

        if (this.mMode == mode) return;

        moveToBackground(mForeSticker);

        if (mode == IMGMode.CLIP) {
            setFreezing(true);
        }

        this.mMode = mode;

        if (mMode == IMGMode.CLIP) {

            // 初始化Shade 画刷
            initShadePaint();

            // 备份裁剪前Clip 区域
            mBackupClipRotate = getRotate();
            mBackupClipFrame.set(mClipFrame);

            float scale = 1 / getScale();
            M.setTranslate(-mFrame.left, -mFrame.top);
            M.postScale(scale, scale);
            M.mapRect(mBackupClipFrame);

            // 重置裁剪区域
            mClipWin.reset(mClipFrame, getTargetRotate());
        } else {

            if (mMode == IMGMode.MOSAIC) {
                makeMosaicBitmap();
            }

            mClipWin.setClipping(false);
        }
    }

    // TODO
    private void rotateStickers(float rotate) {
        Log.d(TAG, "rotateStickers");
        M.setRotate(rotate, mClipFrame.centerX(), mClipFrame.centerY());
        for (IMGSticker sticker : mBackStickers) {
            M.mapRect(sticker.getFrame());
            sticker.setRotation(sticker.getRotation() + rotate);
            sticker.setX(sticker.getFrame().centerX() - sticker.getPivotX());
            sticker.setY(sticker.getFrame().centerY() - sticker.getPivotY());
        }
    }

    private void initShadePaint() {
        Log.d(TAG, "initShadePaint");
        if (mShadePaint == null) {
            mShadePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mShadePaint.setColor(COLOR_SHADE);
            mShadePaint.setStyle(Paint.Style.FILL);
        }
    }

    public boolean isMosaicEmpty() {
        Log.d(TAG, "isMosaicEmpty");
        return mMosaics.isEmpty();
    }

    public boolean isDoodleEmpty() {
        Log.d(TAG, "isDoodleEmpty");
        return mDoodles.isEmpty();
    }

    public void undoDoodle() {
        Log.d(TAG, "undoDoodle");
        if (!mDoodles.isEmpty()) {
            mDoodles.remove(mDoodles.size() - 1);
        }
    }

    public void undoMosaic() {
        Log.d(TAG, "undoMosaic");
        if (!mMosaics.isEmpty()) {
            mMosaics.remove(mMosaics.size() - 1);
        }
    }

    public RectF getClipFrame() {
        Log.d(TAG, "getClipFrame");
        return mClipFrame;
    }

    /**
     * 裁剪区域旋转回原始角度后形成新的裁剪区域，旋转中心发生变化，
     * 因此需要将视图窗口平移到新的旋转中心位置。
     */
    public IMGHoming clip(float scrollX, float scrollY) {
        Log.d(TAG, "clip");
        RectF frame = mClipWin.getOffsetFrame(scrollX, scrollY);

        M.setRotate(-getRotate(), mClipFrame.centerX(), mClipFrame.centerY());
        M.mapRect(mClipFrame, frame);

        return new IMGHoming(
                scrollX + (mClipFrame.centerX() - frame.centerX()),
                scrollY + (mClipFrame.centerY() - frame.centerY()),
                getScale(), getRotate()
        );
    }

    public void toBackupClip() {
        Log.d(TAG, "toBackupClip");
        M.setScale(getScale(), getScale());
        M.postTranslate(mFrame.left, mFrame.top);
        M.mapRect(mClipFrame, mBackupClipFrame);
        setTargetRotate(mBackupClipRotate);
        isRequestToBaseFitting = true;
    }

    public void resetClip() {
        Log.d(TAG, "resetClip");
        // TODO 就近旋转
        setTargetRotate(getRotate() - getRotate() % 360);
        mClipFrame.set(mFrame);
        mClipWin.reset(mClipFrame, getTargetRotate());
    }

    /**
     * 创建同样的马赛克图和马赛克画笔
     */
    private void makeMosaicBitmap() {
        Log.d(TAG, "makeMosaicBitmap");
        if (mMosaicImage != null || mImage == null) {
            return;
        }

        if (mMode == IMGMode.MOSAIC) {

            // 原图的宽高相除64
            int w = Math.round(mImage.getWidth() / 64f);
            int h = Math.round(mImage.getHeight() / 64f);

            // 取最大值，即不能小于8
            w = Math.max(w, 8);
            h = Math.max(h, 8);

            // 马赛克画刷
            if (mMosaicPaint == null) {
                mMosaicPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                mMosaicPaint.setFilterBitmap(false);
                mMosaicPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            }

            // 创建马赛克图
            mMosaicImage = Bitmap.createScaledBitmap(mImage, w, h, false);
        }
    }

    private void onImageChanged() {
        Log.d(TAG, "onImageChanged");
        isInitialHoming = false;
        onWindowChanged(mWindow.width(), mWindow.height());

        if (mMode == IMGMode.CLIP) {
            mClipWin.reset(mClipFrame, getTargetRotate());
        }
    }

    public RectF getFrame() {
        return mFrame;
    }

    public boolean onClipHoming() {
        Log.d(TAG, "onClipHoming");
        return mClipWin.homing();
    }

    public IMGHoming getStartHoming(float scrollX, float scrollY) {
        Log.d(TAG, "getStartHoming: scrollX(" + scrollX + ") scrollY(" + scrollY + ") getScale(" + getScale() + ") getRotate(" + getRotate() + ")");
        return new IMGHoming(scrollX, scrollY, getScale(), getRotate());
    }

    public IMGHoming getEndHoming(float scrollX, float scrollY) {
        IMGHoming homing = new IMGHoming(scrollX, scrollY, getScale(), getTargetRotate());
        Log.d(TAG, "getEndHoming: homing.x(" + homing.x + ") homing.y(" + homing.y + ") homing.scale(" + homing.scale + ") homing.rotate(" + homing.rotate + ")");
        if (mMode == IMGMode.CLIP) {
            RectF frame = new RectF(mClipWin.getTargetFrame());
            frame.offset(scrollX, scrollY);
            if (mClipWin.isResetting()) {

                RectF clipFrame = new RectF();
                M.setRotate(getTargetRotate(), mClipFrame.centerX(), mClipFrame.centerY());
                M.mapRect(clipFrame, mClipFrame);

                homing.rConcat(IMGUtils.fill(frame, clipFrame));
            } else {
                RectF cFrame = new RectF();

                // cFrame要是一个暂时clipFrame
                if (mClipWin.isHoming()) {
//
//                    M.mapRect(cFrame, mClipFrame);

//                    mClipWin
                    // TODO 偏移中心

                    M.setRotate(getTargetRotate() - getRotate(), mClipFrame.centerX(), mClipFrame.centerY());
                    M.mapRect(cFrame, mClipWin.getOffsetFrame(scrollX, scrollY));

                    homing.rConcat(IMGUtils.fitHoming(frame, cFrame, mClipFrame.centerX(), mClipFrame.centerY()));


                } else {
                    M.setRotate(getTargetRotate(), mClipFrame.centerX(), mClipFrame.centerY());
                    M.mapRect(cFrame, mFrame);
                    homing.rConcat(IMGUtils.fillHoming(frame, cFrame, mClipFrame.centerX(), mClipFrame.centerY()));
                }

            }
        } else {
            RectF clipFrame = new RectF();
            M.setRotate(getTargetRotate(), mClipFrame.centerX(), mClipFrame.centerY());
            M.mapRect(clipFrame, mClipFrame);

            RectF win = new RectF(mWindow);
            win.offset(scrollX, scrollY);
            homing.rConcat(IMGUtils.fitHoming(win, clipFrame, isRequestToBaseFitting));
            isRequestToBaseFitting = false;
        }

        Log.d(TAG, "getEndHoming: homing.x(" + homing.x + ") homing.y(" + homing.y + ") homing.scale(" + homing.scale + ") homing.rotate(" + homing.rotate + ")");
        return homing;
    }

    public <S extends IMGSticker> void addSticker(S sticker) {
        Log.d(TAG, "addSticker");
        if (sticker != null) {
            moveToForeground(sticker);
        }
    }

    /**
     * addPath方法详解：
     * M.setTranslate(sx, sy);
     * 矩阵平移到跟view的xy轴一样,注意，是getScrollX()和getScrolly()
     *
     * M.postTranslate(-mFrame.left, -mFrame.top);
     * 如果按照getScrollX()直接绘制进手机屏幕上是会出格的，因为view能缩放到比手机屏幕还要大，那么就需要减掉mFrame的x和y，剩下的就是手机绘制的正确的点
     */
    public void addPath(IMGPath path, float sx, float sy) {
        if (path == null) return;

        float scale = 1f / getScale();
        Log.d(TAG, "addPath getScale()" + getScale());
        Log.d(TAG, "addPath scale" + scale);
        M.setTranslate(sx, sy);
        Log.d(TAG, "addPath sx" + sx);
        Log.d(TAG, "addPath sy" + sy);
        M.postRotate(-getRotate(), mClipFrame.centerX(), mClipFrame.centerY());
        Log.d(TAG, "addPath -getRotate()" + -getRotate());
        Log.d(TAG, "addPath mClipFrame.centerX()" + mClipFrame.centerX());
        Log.d(TAG, "addPath mClipFrame.centerY()" + mClipFrame.centerY());
        M.postTranslate(-mFrame.left, -mFrame.top);
        Log.d(TAG, "addPath -mFrame.left" + -mFrame.left);
        Log.d(TAG, "addPath -mFrame.top" + -mFrame.top);
        M.postScale(scale, scale);
        Log.d(TAG, "addPath scale" + scale);
        // 矩阵变换
        path.transform(M);

        switch (path.getMode()) {
            case DOODLE:
                mDoodles.add(path);
                break;
            case MOSAIC:
                path.setWidth(path.getWidth() * scale);
                mMosaics.add(path);
                break;
        }
    }

    private void moveToForeground(IMGSticker sticker) {
        Log.d(TAG, "moveToForeground");
        if (sticker == null) return;

        moveToBackground(mForeSticker);

        if (sticker.isShowing()) {
            mForeSticker = sticker;
            // 从BackStickers中移除
            mBackStickers.remove(sticker);
        } else sticker.show();
    }

    private void moveToBackground(IMGSticker sticker) {
        Log.d(TAG, "moveToBackground");
        if (sticker == null) return;

        if (!sticker.isShowing()) {
            // 加入BackStickers中
            if (!mBackStickers.contains(sticker)) {
                mBackStickers.add(sticker);
            }

            if (mForeSticker == sticker) {
                mForeSticker = null;
            }
        } else sticker.dismiss();
    }

    public void stickAll() {
        Log.d(TAG, "stickAll");
        moveToBackground(mForeSticker);
    }

    public void onDismiss(IMGSticker sticker) {
        Log.d(TAG, "onDismiss");
        moveToBackground(sticker);
    }

    public void onShowing(IMGSticker sticker) {
        Log.d(TAG, "onShowing");
        if (mForeSticker != sticker) {
            moveToForeground(sticker);
        }
    }

    public void onRemoveSticker(IMGSticker sticker) {
        Log.d(TAG, "onRemoveSticker");
        if (mForeSticker == sticker) {
            mForeSticker = null;
        } else {
            mBackStickers.remove(sticker);
        }
    }

    public void onWindowChanged(float width, float height) {
        Log.d(TAG, "onWindowChanged");
        if (width == 0 || height == 0) {
            return;
        }

        mWindow.set(0, 0, width, height);

        if (!isInitialHoming) {
            onInitialHoming(width, height);
        } else {

            // Pivot to fit window.
            M.setTranslate(mWindow.centerX() - mClipFrame.centerX(), mWindow.centerY() - mClipFrame.centerY());
            M.mapRect(mFrame);
            M.mapRect(mClipFrame);
        }

        mClipWin.setClipWinSize(width, height);
    }

    private void onInitialHoming(float width, float height) {
        Log.d(TAG, "onInitialHoming");
        mFrame.set(0, 0, mImage.getWidth(), mImage.getHeight());
        mClipFrame.set(mFrame);
        mClipWin.setClipWinSize(width, height);

        if (mClipFrame.isEmpty()) {
            return;
        }

        toBaseHoming();

        isInitialHoming = true;
        onInitialHomingDone();
    }

    private void toBaseHoming() {
        Log.d(TAG, "toBaseHoming");
        if (mClipFrame.isEmpty()) {
            // Bitmap invalidate.
            return;
        }

        float scale = Math.min(
                mWindow.width() / mClipFrame.width(),
                mWindow.height() / mClipFrame.height()
        );

        // Scale to fit window.
        M.setScale(scale, scale, mClipFrame.centerX(), mClipFrame.centerY());
        M.postTranslate(mWindow.centerX() - mClipFrame.centerX(), mWindow.centerY() - mClipFrame.centerY());
        M.mapRect(mFrame);
        M.mapRect(mClipFrame);
    }

    private void onInitialHomingDone() {
        Log.d(TAG, "onInitialHomingDone");
        if (mMode == IMGMode.CLIP) {
            mClipWin.reset(mClipFrame, getTargetRotate());
        }
    }

    public void onDrawImage(Canvas canvas) {
        Log.d(TAG, "onDrawImage");

        // 裁剪区域
        canvas.clipRect(mClipWin.isClipping() ? mFrame : mClipFrame);

        // 绘制图片
        canvas.drawBitmap(mImage, null, mFrame, null);

        if (DEBUG) {
            // Clip 区域
            mPaint.setColor(Color.RED);
            mPaint.setStrokeWidth(6);
            canvas.drawRect(mFrame, mPaint);
            canvas.drawRect(mClipFrame, mPaint);
        }
    }

    /**
     * 绘制马赛克路径
     */
    public int onDrawMosaicsPath(Canvas canvas) {
        Log.d(TAG, "onDrawMosaicsPath");
        int layerCount = canvas.saveLayer(mFrame, null, Canvas.ALL_SAVE_FLAG);

        if (!isMosaicEmpty()) {
            canvas.save();
            float scale = getScale();
            canvas.translate(mFrame.left, mFrame.top);
            canvas.scale(scale, scale);
            for (IMGPath path : mMosaics) {
                path.onDrawMosaic(canvas, mPaint);
            }
            canvas.restore();
        }

        return layerCount;
    }

    /**
     * 绘制马赛克
     */
    public void onDrawMosaic(Canvas canvas, int layerCount) {
        Log.d(TAG, "onDrawMosaic");
        canvas.drawBitmap(mMosaicImage, null, mFrame, mMosaicPaint);
        canvas.restoreToCount(layerCount);
    }

    public void onDrawDoodles(Canvas canvas) {
        Log.d(TAG, "onDrawDoodles");
        if (!isDoodleEmpty()) {
            canvas.save();
            float scale = getScale();
            canvas.translate(mFrame.left, mFrame.top);
            canvas.scale(scale, scale);
            for (IMGPath path : mDoodles) {
                path.onDrawDoodle(canvas, mPaint);
            }
            canvas.restore();
        }
    }

    public void onDrawStickerClip(Canvas canvas) {
        Log.d(TAG, "onDrawStickerClip");
        M.setRotate(getRotate(), mClipFrame.centerX(), mClipFrame.centerY());
        M.mapRect(mTempClipFrame, mClipWin.isClipping() ? mFrame : mClipFrame);
        canvas.clipRect(mTempClipFrame);
    }

    public void onDrawStickers(Canvas canvas) {
        Log.d(TAG, "onDrawStickers");
        if (mBackStickers.isEmpty()) return;
        canvas.save();
        for (IMGSticker sticker : mBackStickers) {
            if (!sticker.isShowing()) {
                float tPivotX = sticker.getX() + sticker.getPivotX();
                float tPivotY = sticker.getY() + sticker.getPivotY();

                canvas.save();
                M.setTranslate(sticker.getX(), sticker.getY());
                M.postScale(sticker.getScale(), sticker.getScale(), tPivotX, tPivotY);
                M.postRotate(sticker.getRotation(), tPivotX, tPivotY);

                canvas.concat(M);
                sticker.onSticker(canvas);
                canvas.restore();
            }
        }
        canvas.restore();
    }

    public void onDrawShade(Canvas canvas) {
        Log.d(TAG, "onDrawShade");
        if (mMode == IMGMode.CLIP && isSteady) {
            mShade.reset();
            mShade.addRect(mFrame.left - 2, mFrame.top - 2, mFrame.right + 2, mFrame.bottom + 2, Path.Direction.CW);
            mShade.addRect(mClipFrame, Path.Direction.CCW);
            canvas.drawPath(mShade, mShadePaint);
        }
    }

    public void onDrawClip(Canvas canvas, float scrollX, float scrollY) {
        Log.d(TAG, "onDrawClip");
        if (mMode == IMGMode.CLIP) {
            mClipWin.onDraw(canvas);
        }
    }

    public void onTouchDown(float x, float y) {
        Log.d(TAG, "onTouchDown");
        isSteady = false;
        moveToBackground(mForeSticker);
        if (mMode == IMGMode.CLIP) {
            mAnchor = mClipWin.getAnchor(x, y);
        }
    }

    public void onTouchUp(float scrollX, float scrollY) {
        Log.d(TAG, "onTouchUp");
        if (mAnchor != null) {
            mAnchor = null;
        }
    }

    public void onSteady(float scrollX, float scrollY) {
        Log.d(TAG, "onSteady");
        isSteady = true;
        onClipHoming();
        mClipWin.setShowShade(true);
    }

    public void onScaleBegin() {

    }

    public IMGHoming onScroll(float scrollX, float scrollY, float dx, float dy) {
        Log.d(TAG, "onScroll");
        if (mMode == IMGMode.CLIP) {
            mClipWin.setShowShade(false);
            if (mAnchor != null) {
                mClipWin.onScroll(mAnchor, dx, dy);

                RectF clipFrame = new RectF();
                M.setRotate(getRotate(), mClipFrame.centerX(), mClipFrame.centerY());
                M.mapRect(clipFrame, mFrame);

                RectF frame = mClipWin.getOffsetFrame(scrollX, scrollY);
                IMGHoming homing = new IMGHoming(scrollX, scrollY, getScale(), getTargetRotate());
                homing.rConcat(IMGUtils.fillHoming(frame, clipFrame, mClipFrame.centerX(), mClipFrame.centerY()));
                return homing;
            }
        }
        return null;
    }

    public float getTargetRotate() {
        Log.d(TAG, "getTargetRotate");
        return mTargetRotate;
    }

    public void setTargetRotate(float targetRotate) {
        Log.d(TAG, "setTargetRotate");
        this.mTargetRotate = targetRotate;
    }

    /**
     * 在当前基础上旋转
     */
    public void rotate(int rotate) {
        Log.d(TAG, "rotate");
        mTargetRotate = Math.round((mRotate + rotate) / 90f) * 90;
        mClipWin.reset(mClipFrame, getTargetRotate());
    }

    public float getRotate() {
        Log.d(TAG, "getRotate");
        return mRotate;
    }

    public void setRotate(float rotate) {
        Log.d(TAG, "setRotate");
        mRotate = rotate;
    }

    /**
     * 1 * view缩放后的宽度 / 图片固定宽度 = 缩放比例
     */
    public float getScale() {
        Log.d(TAG, "getScale");
        return 1f * mFrame.width() / mImage.getWidth();
    }

    public void setScale(float scale) {
        Log.d(TAG, "setScale");
        setScale(scale, mClipFrame.centerX(), mClipFrame.centerY());
    }

    public void setScale(float scale, float focusX, float focusY) {
        Log.d(TAG, "setScale");
        onScale(scale / getScale(), focusX, focusY);
    }

    public void onScale(float factor, float focusX, float focusY) {
        Log.d(TAG, "onScale");

        if (factor == 1f) return;

        if (Math.max(mClipFrame.width(), mClipFrame.height()) >= MAX_SIZE
                || Math.min(mClipFrame.width(), mClipFrame.height()) <= MIN_SIZE) {
            factor += (1 - factor) / 2;
        }

        M.setScale(factor, factor, focusX, focusY);
        M.mapRect(mFrame);
        M.mapRect(mClipFrame);

        // 修正clip 窗口
        if (!mFrame.contains(mClipFrame)) {
            // TODO
//            mClipFrame.intersect(mFrame);
        }

        for (IMGSticker sticker : mBackStickers) {
            M.mapRect(sticker.getFrame());
            float tPivotX = sticker.getX() + sticker.getPivotX();
            float tPivotY = sticker.getY() + sticker.getPivotY();
            sticker.addScale(factor);
            sticker.setX(sticker.getX() + sticker.getFrame().centerX() - tPivotX);
            sticker.setY(sticker.getY() + sticker.getFrame().centerY() - tPivotY);
        }
    }

    public void onScaleEnd() {
        Log.d(TAG, "onScaleEnd");
    }

    public void onHomingStart(boolean isRotate) {
        Log.d(TAG, "onHomingStart");
        isAnimCanceled = false;
        isDrawClip = true;
    }

    public void onHoming(float fraction) {
        Log.d(TAG, "onHoming");
        mClipWin.homing(fraction);
    }

    public boolean onHomingEnd(float scrollX, float scrollY, boolean isRotate) {
        Log.d(TAG, "onHomingEnd");
        isDrawClip = true;
        if (mMode == IMGMode.CLIP) {
            // 开启裁剪模式

            boolean clip = !isAnimCanceled;

            mClipWin.setHoming(false);
            mClipWin.setClipping(true);
            mClipWin.setResetting(false);

            return clip;
        } else {
            if (isFreezing && !isAnimCanceled) {
                setFreezing(false);
            }
        }
        return false;
    }

    /**
     * 是否冻结的
     */
    public boolean isFreezing() {
        Log.d(TAG, "isFreezing");
        return isFreezing;
    }

    private void setFreezing(boolean freezing) {
        Log.d(TAG, "setFreezing");
        if (freezing != isFreezing) {
            rotateStickers(freezing ? -getRotate() : getTargetRotate());
            isFreezing = freezing;
        }
    }

    public void onHomingCancel(boolean isRotate) {
        Log.d(TAG, "onHomingCancel");
        isAnimCanceled = true;
        Log.d(TAG, "Homing cancel");
    }

    public void release() {
        Log.d(TAG, "release");
        if (mImage != null && !mImage.isRecycled()) {
            mImage.recycle();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        Log.d(TAG, "finalize");
        super.finalize();
        if (DEFAULT_IMAGE != null) {
            DEFAULT_IMAGE.recycle();
        }
    }
}
