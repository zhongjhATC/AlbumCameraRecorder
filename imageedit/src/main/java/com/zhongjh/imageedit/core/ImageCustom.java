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

import com.zhongjh.imageedit.core.clip.ImageClip;
import com.zhongjh.imageedit.core.clip.ImageClipWindow;
import com.zhongjh.imageedit.core.homing.ImageHoming;
import com.zhongjh.imageedit.core.sticker.ImageSticker;
import com.zhongjh.imageedit.core.util.ImageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author felix
 * @date 2017/11/21 下午10:03
 */
public class ImageCustom {

    private static final String TAG = "IMGImage";
    private final static float SCALE_MAX = 1f;

    private Bitmap mImage, mMosaicImage;

    /**
     * 完整图片边框
     */
    private final RectF mFrame = new RectF();

    /**
     * 裁剪图片边框（显示的图片区域）
     */
    private final RectF mClipFrame = new RectF();

    private final RectF mTempClipFrame = new RectF();

    /**
     * 裁剪模式前状态备份
     */
    private final RectF mBackupClipFrame = new RectF();

    private float mBackupClipRotate = 0;

    private float mRotate = 0, mTargetRotate = 0;

    private boolean isRequestToBaseFitting = false;

    private boolean isAnimCanceled = false;

    /**
     * 裁剪模式时当前触摸锚点
     */
    private ImageClip.Anchor mAnchor;

    private boolean isSteady = true;

    private final Path mShade = new Path();

    /**
     * 裁剪窗口
     */
    private final ImageClipWindow mClipWin = new ImageClipWindow();

    /**
     * 编辑模式
     */
    private ImageMode mMode = ImageMode.NONE;

    /**
     * 是否冻结的
     */
    private boolean isFreezing = false;

    /**
     * 可视区域，无Scroll 偏移区域
     */
    private final RectF mWindow = new RectF();

    /**
     * 是否初始位置
     */
    private boolean isInitialHoming = false;

    /**
     * 当前选中贴片
     */
    private ImageSticker mForeSticker;

    /**
     * 为被选中贴片
     */
    private final List<ImageSticker> mBackStickers = new ArrayList<>();

    /**
     * 涂鸦路径
     */
    private final List<ImagePath> mDoodles = new ArrayList<>();

    /**
     * 马赛克路径
     */
    private final List<ImagePath> mMosaics = new ArrayList<>();

    private static final int MIN_SIZE = 500;

    private static final int MAX_SIZE = 10000;

    private final Paint mPaint;
    private Paint mMosaicPaint;
    private Paint mShadePaint;

    private final Matrix mMatrix = new Matrix();

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
        mPaint.setStrokeWidth(ImagePath.BASE_DOODLE_WIDTH);
        mPaint.setColor(Color.RED);
        mPaint.setPathEffect(new CornerPathEffect(ImagePath.BASE_DOODLE_WIDTH));
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
    }

    public ImageCustom() {
        Log.d(TAG, "IMGImage");
        mImage = DEFAULT_IMAGE;

        if (mMode == ImageMode.CLIP) {
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

    public ImageMode getMode() {
        Log.d(TAG, "getMode");
        return mMode;
    }

    public void setMode(ImageMode mode) {
        Log.d(TAG, "setMode");

        if (this.mMode == mode) {
            return;
        }

        moveToBackground(mForeSticker);

        if (mode == ImageMode.CLIP) {
            setFreezing(true);
        }

        this.mMode = mode;

        if (mMode == ImageMode.CLIP) {

            // 初始化Shade 画刷
            initShadePaint();

            // 备份裁剪前Clip 区域
            mBackupClipRotate = getRotate();
            mBackupClipFrame.set(mClipFrame);

            float scale = 1 / getScale();
            mMatrix.setTranslate(-mFrame.left, -mFrame.top);
            mMatrix.postScale(scale, scale);
            mMatrix.mapRect(mBackupClipFrame);

            // 重置裁剪区域
            mClipWin.reset(mClipFrame, getTargetRotate());
        } else {

            if (mMode == ImageMode.MOSAIC) {
                makeMosaicBitmap();
            }

            mClipWin.setClipping(false);
        }
    }

    private void rotateStickers(float rotate) {
        Log.d(TAG, "rotateStickers");
        mMatrix.setRotate(rotate, mClipFrame.centerX(), mClipFrame.centerY());
        for (ImageSticker sticker : mBackStickers) {
            mMatrix.mapRect(sticker.getFrame());
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
    public ImageHoming clip(float scrollX, float scrollY) {
        Log.d(TAG, "clip");
        RectF frame = mClipWin.getOffsetFrame(scrollX, scrollY);

        mMatrix.setRotate(-getRotate(), mClipFrame.centerX(), mClipFrame.centerY());
        mMatrix.mapRect(mClipFrame, frame);

        return new ImageHoming(
                scrollX + (mClipFrame.centerX() - frame.centerX()),
                scrollY + (mClipFrame.centerY() - frame.centerY()),
                getScale(), getRotate()
        );
    }

    public void toBackupClip() {
        Log.d(TAG, "toBackupClip");
        mMatrix.setScale(getScale(), getScale());
        mMatrix.postTranslate(mFrame.left, mFrame.top);
        mMatrix.mapRect(mClipFrame, mBackupClipFrame);
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

        if (mMode == ImageMode.MOSAIC) {

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

        if (mMode == ImageMode.CLIP) {
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

    public ImageHoming getStartHoming(float scrollX, float scrollY) {
        Log.d(TAG, "getStartHoming: scrollX(" + scrollX + ") scrollY(" + scrollY + ") getScale(" + getScale() + ") getRotate(" + getRotate() + ")");
        return new ImageHoming(scrollX, scrollY, getScale(), getRotate());
    }

    public ImageHoming getEndHoming(float scrollX, float scrollY) {
        ImageHoming homing = new ImageHoming(scrollX, scrollY, getScale(), getTargetRotate());
        Log.d(TAG, "getEndHoming: homing.x(" + homing.x + ") homing.y(" + homing.y + ") homing.scale(" + homing.scale + ") homing.rotate(" + homing.rotate + ")");
        if (mMode == ImageMode.CLIP) {
            RectF frame = new RectF(mClipWin.getTargetFrame());
            frame.offset(scrollX, scrollY);
            if (mClipWin.isResetting()) {

                RectF clipFrame = new RectF();
                mMatrix.setRotate(getTargetRotate(), mClipFrame.centerX(), mClipFrame.centerY());
                mMatrix.mapRect(clipFrame, mClipFrame);

                homing.rConcat(ImageUtils.fill(frame, clipFrame));
            } else {
                RectF cFrame = new RectF();

                // cFrame要是一个暂时clipFrame
                if (mClipWin.isHoming()) {

                    mMatrix.setRotate(getTargetRotate() - getRotate(), mClipFrame.centerX(), mClipFrame.centerY());
                    mMatrix.mapRect(cFrame, mClipWin.getOffsetFrame(scrollX, scrollY));

                    homing.rConcat(ImageUtils.fitHoming(frame, cFrame, mClipFrame.centerX(), mClipFrame.centerY()));


                } else {
                    mMatrix.setRotate(getTargetRotate(), mClipFrame.centerX(), mClipFrame.centerY());
                    mMatrix.mapRect(cFrame, mFrame);
                    homing.rConcat(ImageUtils.fillHoming(frame, cFrame, mClipFrame.centerX(), mClipFrame.centerY()));
                }

            }
        } else {
            RectF clipFrame = new RectF();
            mMatrix.setRotate(getTargetRotate(), mClipFrame.centerX(), mClipFrame.centerY());
            mMatrix.mapRect(clipFrame, mClipFrame);

            RectF win = new RectF(mWindow);
            win.offset(scrollX, scrollY);
            homing.rConcat(ImageUtils.fitHoming(win, clipFrame, isRequestToBaseFitting));
            isRequestToBaseFitting = false;
        }

        Log.d(TAG, "getEndHoming: homing.x(" + homing.x + ") homing.y(" + homing.y + ") homing.scale(" + homing.scale + ") homing.rotate(" + homing.rotate + ")");
        return homing;
    }

    public <S extends ImageSticker> void addSticker(S sticker) {
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
    public void addPath(ImagePath path, float sx, float sy) {
        if (path == null) {
            return;
        }

        float scale = 1f / getScale();
        Log.d(TAG, "addPath getScale()" + getScale());
        Log.d(TAG, "addPath scale" + scale);
        mMatrix.setTranslate(sx, sy);
        Log.d(TAG, "addPath sx" + sx);
        Log.d(TAG, "addPath sy" + sy);
        mMatrix.postRotate(-getRotate(), mClipFrame.centerX(), mClipFrame.centerY());
        Log.d(TAG, "addPath -getRotate()" + -getRotate());
        Log.d(TAG, "addPath mClipFrame.centerX()" + mClipFrame.centerX());
        Log.d(TAG, "addPath mClipFrame.centerY()" + mClipFrame.centerY());
        mMatrix.postTranslate(-mFrame.left, -mFrame.top);
        Log.d(TAG, "addPath -mFrame.left" + -mFrame.left);
        Log.d(TAG, "addPath -mFrame.top" + -mFrame.top);
        mMatrix.postScale(scale, scale);
        Log.d(TAG, "addPath scale" + scale);
        // 矩阵变换
        path.transform(mMatrix);

        switch (path.getMode()) {
            case DOODLE:
                mDoodles.add(path);
                break;
            case MOSAIC:
                path.setWidth(path.getWidth() * scale);
                mMosaics.add(path);
                break;
            default:
                break;
        }
    }

    private void moveToForeground(ImageSticker sticker) {
        Log.d(TAG, "moveToForeground");
        if (sticker == null) {
            return;
        }

        moveToBackground(mForeSticker);

        if (sticker.isShowing()) {
            mForeSticker = sticker;
            // 从BackStickers中移除
            mBackStickers.remove(sticker);
        } else {
            sticker.show();
        }
    }

    private void moveToBackground(ImageSticker sticker) {
        Log.d(TAG, "moveToBackground");
        if (sticker == null) {
            return;
        }

        if (!sticker.isShowing()) {
            // 加入BackStickers中
            if (!mBackStickers.contains(sticker)) {
                mBackStickers.add(sticker);
            }

            if (mForeSticker == sticker) {
                mForeSticker = null;
            }
        } else {
            sticker.dismiss();
        }
    }

    public void stickAll() {
        Log.d(TAG, "stickAll");
        moveToBackground(mForeSticker);
    }

    public void onDismiss(ImageSticker sticker) {
        Log.d(TAG, "onDismiss");
        moveToBackground(sticker);
    }

    public void onShowing(ImageSticker sticker) {
        Log.d(TAG, "onShowing");
        if (mForeSticker != sticker) {
            moveToForeground(sticker);
        }
    }

    public void onRemoveSticker(ImageSticker sticker) {
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
            mMatrix.setTranslate(mWindow.centerX() - mClipFrame.centerX(), mWindow.centerY() - mClipFrame.centerY());
            mMatrix.mapRect(mFrame);
            mMatrix.mapRect(mClipFrame);
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
        mMatrix.setScale(scale, scale, mClipFrame.centerX(), mClipFrame.centerY());
        mMatrix.postTranslate(mWindow.centerX() - mClipFrame.centerX(), mWindow.centerY() - mClipFrame.centerY());
        mMatrix.mapRect(mFrame);
        mMatrix.mapRect(mClipFrame);
    }

    private void onInitialHomingDone() {
        Log.d(TAG, "onInitialHomingDone");
        if (mMode == ImageMode.CLIP) {
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
        int layerCount = canvas.saveLayer(mFrame, null);

        if (!isMosaicEmpty()) {
            canvas.save();
            float scale = getScale();
            canvas.translate(mFrame.left, mFrame.top);
            canvas.scale(scale, scale);
            for (ImagePath path : mMosaics) {
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
            for (ImagePath path : mDoodles) {
                path.onDrawDoodle(canvas, mPaint);
            }
            canvas.restore();
        }
    }

    public void onDrawStickerClip(Canvas canvas) {
        Log.d(TAG, "onDrawStickerClip");
        mMatrix.setRotate(getRotate(), mClipFrame.centerX(), mClipFrame.centerY());
        mMatrix.mapRect(mTempClipFrame, mClipWin.isClipping() ? mFrame : mClipFrame);
        canvas.clipRect(mTempClipFrame);
    }

    public void onDrawStickers(Canvas canvas) {
        Log.d(TAG, "onDrawStickers");
        if (mBackStickers.isEmpty()) {
            return;
        }
        canvas.save();
        for (ImageSticker sticker : mBackStickers) {
            if (!sticker.isShowing()) {
                float tPivotX = sticker.getX() + sticker.getPivotX();
                float tPivotY = sticker.getY() + sticker.getPivotY();

                canvas.save();
                mMatrix.setTranslate(sticker.getX(), sticker.getY());
                mMatrix.postScale(sticker.getScale(), sticker.getScale(), tPivotX, tPivotY);
                mMatrix.postRotate(sticker.getRotation(), tPivotX, tPivotY);

                canvas.concat(mMatrix);
                sticker.onSticker(canvas);
                canvas.restore();
            }
        }
        canvas.restore();
    }

    public void onDrawShade(Canvas canvas) {
        Log.d(TAG, "onDrawShade");
        if (mMode == ImageMode.CLIP && isSteady) {
            mShade.reset();
            mShade.addRect(mFrame.left - 2, mFrame.top - 2, mFrame.right + 2, mFrame.bottom + 2, Path.Direction.CW);
            mShade.addRect(mClipFrame, Path.Direction.CCW);
            canvas.drawPath(mShade, mShadePaint);
        }
    }

    public void onDrawClip(Canvas canvas, float scrollX, float scrollY) {
        Log.d(TAG, "onDrawClip");
        if (mMode == ImageMode.CLIP) {
            mClipWin.onDraw(canvas);
        }
    }

    public void onTouchDown(float x, float y) {
        Log.d(TAG, "onTouchDown");
        isSteady = false;
        moveToBackground(mForeSticker);
        if (mMode == ImageMode.CLIP) {
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

    public ImageHoming onScroll(float scrollX, float scrollY, float dx, float dy) {
        Log.d(TAG, "onScroll");
        if (mMode == ImageMode.CLIP) {
            mClipWin.setShowShade(false);
            if (mAnchor != null) {
                mClipWin.onScroll(mAnchor, dx, dy);

                RectF clipFrame = new RectF();
                mMatrix.setRotate(getRotate(), mClipFrame.centerX(), mClipFrame.centerY());
                mMatrix.mapRect(clipFrame, mFrame);

                RectF frame = mClipWin.getOffsetFrame(scrollX, scrollY);
                ImageHoming homing = new ImageHoming(scrollX, scrollY, getScale(), getTargetRotate());
                homing.rConcat(ImageUtils.fillHoming(frame, clipFrame, mClipFrame.centerX(), mClipFrame.centerY()));
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

        if (Math.abs(factor) == Math.abs(SCALE_MAX)) {
            return;
        }

        if (Math.max(mClipFrame.width(), mClipFrame.height()) >= MAX_SIZE
                || Math.min(mClipFrame.width(), mClipFrame.height()) <= MIN_SIZE) {
            factor += (1 - factor) / 2;
        }

        mMatrix.setScale(factor, factor, focusX, focusY);
        mMatrix.mapRect(mFrame);
        mMatrix.mapRect(mClipFrame);

        for (ImageSticker sticker : mBackStickers) {
            mMatrix.mapRect(sticker.getFrame());
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

    public void onHomingStart() {
        Log.d(TAG, "onHomingStart");
        isAnimCanceled = false;
    }

    public void onHoming(float fraction) {
        Log.d(TAG, "onHoming");
        mClipWin.homing(fraction);
    }

    public boolean onHomingEnd(float scrollX, float scrollY, boolean isRotate) {
        Log.d(TAG, "onHomingEnd");
        if (mMode == ImageMode.CLIP) {
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
