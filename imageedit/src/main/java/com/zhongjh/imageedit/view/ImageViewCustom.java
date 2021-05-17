package com.zhongjh.imageedit.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.zhongjh.imageedit.core.ImageCustom;
import com.zhongjh.imageedit.core.ImageMode;
import com.zhongjh.imageedit.core.ImagePath;
import com.zhongjh.imageedit.core.ImageText;
import com.zhongjh.imageedit.core.anim.ImageHomingAnimator;
import com.zhongjh.imageedit.core.homing.ImageHoming;
import com.zhongjh.imageedit.core.sticker.ImageSticker;
import com.zhongjh.imageedit.core.sticker.ImageStickerPortrait;

/**
 * ImageView
 * clip外不加入path
 *
 * @author zhongjh
 * @date 2017/11/14 下午6:43
 */
public class ImageViewCustom extends FrameLayout implements Runnable, ScaleGestureDetector.OnScaleGestureListener,
        ValueAnimator.AnimatorUpdateListener, ImageStickerPortrait.Callback, Animator.AnimatorListener {

    private static final String TAG = "IMGView";
    private final static int SCALE_MAX = 20;

    private ImageMode mPreMode = ImageMode.NONE;

    private final ImageCustom mImage = new ImageCustom();

    private GestureDetector mGestureDetector;

    private ScaleGestureDetector mScaleGestureDetector;

    private ImageHomingAnimator mHomingAnimator;

    private final Pen mPen = new Pen();

    private int mPointerCount = 0;

    private final Paint mDoodlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint mMosaicPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final boolean DEBUG = true;

    public interface Listener {

        /**
         * 双手触控屏幕的时候会重置当前模式
         */
        void resetModel();
    }

    private Listener listener;

    {
        // 涂鸦画刷
        mDoodlePaint.setStyle(Paint.Style.STROKE);
        mDoodlePaint.setStrokeWidth(ImagePath.BASE_DOODLE_WIDTH);
        mDoodlePaint.setColor(Color.RED);
        mDoodlePaint.setPathEffect(new CornerPathEffect(ImagePath.BASE_DOODLE_WIDTH));
        mDoodlePaint.setStrokeCap(Paint.Cap.ROUND);
        mDoodlePaint.setStrokeJoin(Paint.Join.ROUND);

        // 马赛克画刷
        mMosaicPaint.setStyle(Paint.Style.STROKE);
        mMosaicPaint.setStrokeWidth(ImagePath.BASE_MOSAIC_WIDTH);
        mMosaicPaint.setColor(Color.BLACK);
        mMosaicPaint.setPathEffect(new CornerPathEffect(ImagePath.BASE_MOSAIC_WIDTH));
        mMosaicPaint.setStrokeCap(Paint.Cap.ROUND);
        mMosaicPaint.setStrokeJoin(Paint.Join.ROUND);
    }

    public ImageViewCustom(Context context) {
        this(context, null, 0);
    }

    public ImageViewCustom(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageViewCustom(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {
        mPen.setMode(mImage.getMode());
        // 手势监听类
        mGestureDetector = new GestureDetector(context, new MoveAdapter());
        // 用于处理缩放的工具类
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
    }

    public void setImageBitmap(Bitmap image) {
        mImage.setBitmap(image);
        invalidate();
    }

    public void addListener(Listener listener) {
        this.listener = listener;
    }

    /**
     * 设置模式
     *
     * @param mode 模式
     */
    public void setMode(ImageMode mode) {
        Log.d(TAG, "setMode");
        // 保存现在的编辑模式
        mPreMode = mImage.getMode();

        // 设置新的编辑模式
        mImage.setMode(mode);
        mPen.setMode(mode);

        // 矫正区域
        onHoming();

    }

    /**
     * 是否真正修正归位
     */
    boolean isHoming() {
        Log.d(TAG, "isHoming");
        return mHomingAnimator != null
                && mHomingAnimator.isRunning();
    }

    /**
     * 矫正区域
     * 假设移动图片到某个区别或者放大缩小时，改方法用于变回原样
     */
    private void onHoming() {
        Log.d(TAG, "onHoming");
        invalidate();
        stopHoming();
        startHoming(mImage.getStartHoming(getScrollX(), getScrollY()),
                mImage.getEndHoming(getScrollX(), getScrollY()));
    }

    /**
     * 开始了矫正动画
     */
    private void startHoming(ImageHoming sHoming, ImageHoming eHoming) {
        Log.d(TAG, "startHoming");
        if (mHomingAnimator == null) {
            mHomingAnimator = new ImageHomingAnimator();
            mHomingAnimator.addUpdateListener(this);
            mHomingAnimator.addListener(this);
        }
        mHomingAnimator.setHomingValues(sHoming, eHoming);
        mHomingAnimator.start();
    }

    /**
     * 停止当前的矫正区域动画
     */
    private void stopHoming() {
        Log.d(TAG, "stopHoming");
        if (mHomingAnimator != null) {
            mHomingAnimator.cancel();
        }
    }

    public void doRotate() {
        Log.d(TAG, "doRotate");
        if (!isHoming()) {
            mImage.rotate(-90);
            onHoming();
        }
    }

    public void resetClip() {
        Log.d(TAG, "resetClip");
        mImage.resetClip();
        onHoming();
    }

    public void doClip() {
        Log.d(TAG, "doClip");
        mImage.clip(getScrollX(), getScrollY());
        setMode(mPreMode);
        onHoming();
    }

    public void cancelClip() {
        Log.d(TAG, "cancelClip");
        mImage.toBackupClip();
        setMode(mPreMode);
    }

    public void setPenColor(int color) {
        Log.d(TAG, "setPenColor");
        mPen.setColor(color);
    }

    public boolean isDoodleEmpty() {
        Log.d(TAG, "isDoodleEmpty");
        return mImage.isDoodleEmpty();
    }

    public void undoDoodle() {
        Log.d(TAG, "undoDoodle");
        mImage.undoDoodle();
        invalidate();
    }

    public boolean isMosaicEmpty() {
        Log.d(TAG, "isMosaicEmpty");
        return mImage.isMosaicEmpty();
    }

    public void undoMosaic() {
        Log.d(TAG, "undoMosaic");
        mImage.undoMosaic();
        invalidate();
    }

    /**
     * 获取当前模式
     *
     * @return 模式
     */
    public ImageMode getMode() {
        Log.d(TAG, "getMode");
        return mImage.getMode();
    }

    /**
     * 重新在IMGView画图
     */
    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw");
        onDrawImages(canvas);
    }

    /**
     * 重新在IMGView画图
     */
    private void onDrawImages(Canvas canvas) {
        Log.d(TAG, "onDrawImages");
        canvas.save();

        // clip 中心旋转
        RectF clipFrame = mImage.getClipFrame();
        canvas.rotate(mImage.getRotate(), clipFrame.centerX(), clipFrame.centerY());

        // 图片
        mImage.onDrawImage(canvas);

        // 马赛克
        boolean isMosaic = !mImage.isMosaicEmpty() || (mImage.getMode() == ImageMode.MOSAIC && !mPen.isEmpty());
        if (isMosaic) {
            int count = mImage.onDrawMosaicsPath(canvas);
            if (mImage.getMode() == ImageMode.MOSAIC && !mPen.isEmpty()) {
                mDoodlePaint.setStrokeWidth(ImagePath.BASE_MOSAIC_WIDTH);
                canvas.save();
                RectF frame = mImage.getClipFrame();
                canvas.rotate(-mImage.getRotate(), frame.centerX(), frame.centerY());
                canvas.translate(getScrollX(), getScrollY());
                canvas.drawPath(mPen.getPath(), mDoodlePaint);
                canvas.restore();
            }
            mImage.onDrawMosaic(canvas, count);
        }

        // 涂鸦
        mImage.onDrawDoodles(canvas);
        if (mImage.getMode() == ImageMode.DOODLE && !mPen.isEmpty()) {
            mDoodlePaint.setColor(mPen.getColor());
            mDoodlePaint.setStrokeWidth(ImagePath.BASE_DOODLE_WIDTH * mImage.getScale());
            canvas.save();
            RectF frame = mImage.getClipFrame();
            canvas.rotate(-mImage.getRotate(), frame.centerX(), frame.centerY());
            canvas.translate(getScrollX(), getScrollY());
            canvas.drawPath(mPen.getPath(), mDoodlePaint);
            canvas.restore();
        }

        // TODO
        if (mImage.isFreezing()) {
            // 文字贴片
            mImage.onDrawStickers(canvas);
        }

        mImage.onDrawShade(canvas);

        canvas.restore();

        // TODO
        if (!mImage.isFreezing()) {
            // 文字贴片
            mImage.onDrawStickerClip(canvas);
            mImage.onDrawStickers(canvas);
        }

        // 裁剪
        if (mImage.getMode() == ImageMode.CLIP) {
            canvas.save();
            canvas.translate(getScrollX(), getScrollY());
            mImage.onDrawClip(canvas, getScrollX(), getScrollY());
            canvas.restore();
        }
    }

    public Bitmap saveBitmap() {
        Log.d(TAG, "saveBitmap");
        mImage.stickAll();

        float scale = 1f / mImage.getScale();

        RectF frame = new RectF(mImage.getClipFrame());

        // 旋转基画布
        Matrix m = new Matrix();
        m.setRotate(mImage.getRotate(), frame.centerX(), frame.centerY());
        m.mapRect(frame);

        // 缩放基画布
        m.setScale(scale, scale, frame.left, frame.top);
        m.mapRect(frame);

        Bitmap bitmap = Bitmap.createBitmap(Math.round(frame.width()),
                Math.round(frame.height()), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        // 平移到基画布原点&缩放到原尺寸
        canvas.translate(-frame.left, -frame.top);
        canvas.scale(scale, scale, frame.left, frame.top);

        onDrawImages(canvas);

        return bitmap;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.d(TAG, "onLayout");
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mImage.onWindowChanged(right - left, bottom - top);
        }
    }

    public <V extends View & ImageSticker> void addStickerView(V stickerView, LayoutParams params) {
        Log.d(TAG, "addStickerView");
        if (stickerView != null) {

            addView(stickerView, params);

            stickerView.registerCallback(this);
            mImage.addSticker(stickerView);
        }
    }

    public void addStickerText(ImageText text) {
        Log.d(TAG, "addStickerText");
        ImageStickerTextView textView = new ImageStickerTextView(getContext());

        textView.setText(text);

        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );

        // Center of the drawing window.
        layoutParams.gravity = Gravity.CENTER;

        textView.setX(getScrollX());
        textView.setY(getScrollY());

        addStickerView(textView, layoutParams);
    }

    /**
     * 处理点击分发事件
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.d(TAG, "onInterceptTouchEvent");
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            return onInterceptTouch(ev) || super.onInterceptTouchEvent(ev);
        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 处理可以直接中断当前伸缩，继续按照自己意愿伸缩，增强流畅度
     */
    boolean onInterceptTouch(MotionEvent event) {
        Log.d(TAG, "onInterceptTouch");
        if (isHoming()) {
            stopHoming();
            Log.d(TAG, "onInterceptTouch true stopHoming");
            return true;
        } else if (mImage.getMode() == ImageMode.CLIP) {
            Log.d(TAG, "onInterceptTouch true IMGMode.CLIP");
            return true;
        }
        Log.d(TAG, "onInterceptTouch false");
        return false;
    }

    /**
     * 处理触屏事件，里面的延迟和取消延迟也是为了伸缩图片体验性提高
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent");
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // 取消延迟
                removeCallbacks(this);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // 取消或者离开触屏延迟1.2秒
                postDelayed(this, 1200);
                break;
            default:
                break;
        }
        return onTouch(event);
    }

    /**
     * 处理触屏事件.详情
     */
    boolean onTouch(MotionEvent event) {
        Log.d(TAG, "onTouch");

        if (isHoming()) {
            // Homing
            return false;
        }

        mPointerCount = event.getPointerCount();

        boolean handled = mScaleGestureDetector.onTouchEvent(event);

        ImageMode mode = mImage.getMode();

        if (mode == ImageMode.NONE || mode == ImageMode.CLIP) {
            handled |= onTouchNone(event);
        } else if (mPointerCount > 1) {
            onPathDone();
            handled |= onTouchNone(event);

            // 取消涂鸦或者别的模式
            this.listener.resetModel();
            setMode(ImageMode.NONE);
        } else {
            handled |= onTouchPath(event);
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mImage.onTouchDown(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mImage.onTouchUp(getScrollX(), getScrollY());
                onHoming();
                break;
            default:
                break;
        }

        return handled;
    }


    private boolean onTouchNone(MotionEvent event) {
        Log.d(TAG, "onTouchNone");
        return mGestureDetector.onTouchEvent(event);
    }

    /**
     * 画笔线
     */
    private boolean onTouchPath(MotionEvent event) {
        Log.d(TAG, "onTouchPath");
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                return onPathBegin(event);
            case MotionEvent.ACTION_MOVE:
                return onPathMove(event);
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                return mPen.isIdentity(event.getPointerId(0)) && onPathDone();
            default:
                break;
        }
        return false;
    }

    /**
     * 钢笔初始化
     */
    private boolean onPathBegin(MotionEvent event) {
        Log.d(TAG, "onPathBegin");
        mPen.reset(event.getX(), event.getY());
        mPen.setIdentity(event.getPointerId(0));
        return true;
    }

    /**
     * 画线
     */
    private boolean onPathMove(MotionEvent event) {
        Log.d(TAG, "onPathMove");
        if (mPen.isIdentity(event.getPointerId(0))) {
            mPen.lineTo(event.getX(), event.getY());
            invalidate();
            return true;
        }
        return false;
    }

    /**
     * 画线完成
     */
    private boolean onPathDone() {
        Log.d(TAG, "onPathDone");
        if (mPen.isEmpty()) {
            return false;
        }
        mImage.addPath(mPen.toPath(), getScrollX(), getScrollY());
        mPen.reset();
        invalidate();
        return true;
    }

    @Override
    public void run() {
        Log.d(TAG, "run");
        // 稳定触发
        if (!onSteady()) {
            postDelayed(this, 500);
        }
    }

    boolean onSteady() {
        if (DEBUG) {
            Log.d(TAG, "onSteady: isHoming=" + isHoming());
        }
        if (!isHoming()) {
            mImage.onSteady(getScrollX(), getScrollY());
            onHoming();
            return true;
        }
        return false;
    }

    @Override
    protected void onDetachedFromWindow() {
        Log.d(TAG, "onDetachedFromWindow");
        super.onDetachedFromWindow();
        removeCallbacks(this);
        mImage.release();
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        Log.d(TAG, "onScale");
        if (mPointerCount > 1) {
            // 当图片本身大于20倍的时候并且缩放操作要放大的时候取消缩放。缩放大于20倍的时候，返回上一次的变形，防止裁剪因为高度不大于0而导致闪退
            if (mImage.getScale() > SCALE_MAX && detector.getScaleFactor() > 1) {
                return true;
            }
            mImage.onScale(detector.getScaleFactor(),
                    getScrollX() + detector.getFocusX(),
                    getScrollY() + detector.getFocusY());
            invalidate();
            return true;
        }
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        Log.d(TAG, "onScaleBegin");
        if (mPointerCount > 1) {
            mImage.onScaleBegin();
            return true;
        }
        return false;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        Log.d(TAG, "onScaleEnd");
        mImage.onScaleEnd();
    }

    /**
     * 标记着动画的更新
     */
    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        Log.d(TAG, "onAnimationUpdate");
        mImage.onHoming(animation.getAnimatedFraction());
        toApplyHoming((ImageHoming) animation.getAnimatedValue());
    }

    /**
     * 设置图片回归原位
     */
    private void toApplyHoming(ImageHoming homing) {
        Log.d(TAG, "toApplyHoming " +
                "homing.scale(" + homing.scale + ")homing.rotate(" + homing.rotate + ")homing.x(" + homing.x + ")homing.y" + homing.y + ")");
        mImage.setScale(homing.scale);
        mImage.setRotate(homing.rotate);
        if (!onScrollTo(Math.round(homing.x), Math.round(homing.y))) {
            invalidate();
        }
    }

    /**
     * 设置图片回归原位
     */
    private boolean onScrollTo(int x, int y) {
        Log.d(TAG, "onScrollTo");
        Log.d(TAG, "onScrollTo x" + x);
        Log.d(TAG, "onScrollTo y" + y);
        if (getScrollX() != x || getScrollY() != y) {
            scrollTo(x, y);
            return true;
        }
        return false;
    }

    @Override
    public <V extends View & ImageSticker> void onDismiss(V stickerView) {
        Log.d(TAG, "onDismiss");
        mImage.onDismiss(stickerView);
        invalidate();
    }

    @Override
    public <V extends View & ImageSticker> void onShowing(V stickerView) {
        Log.d(TAG, "onShowing");
        mImage.onShowing(stickerView);
        invalidate();
    }

    @Override
    public <V extends View & ImageSticker> boolean onRemove(V stickerView) {
        Log.d(TAG, "onRemove");
        mImage.onRemoveSticker(stickerView);
        stickerView.unregisterCallback(this);
        ViewParent parent = stickerView.getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(stickerView);
        }
        return true;
    }

    /**
     * 标记着动画的开始
     */
    @Override
    public void onAnimationStart(Animator animation) {
        Log.d(TAG, "onAnimationStart");
        if (DEBUG) {
            Log.d(TAG, "onAnimationStart");
        }
        mImage.onHomingStart();
    }

    /**
     * 标记着动画的结束
     */
    @Override
    public void onAnimationEnd(Animator animation) {
        Log.d(TAG, "onAnimationEnd");
        if (DEBUG) {
            Log.d(TAG, "onAnimationEnd");
        }
        if (mImage.onHomingEnd(getScrollX(), getScrollY(), mHomingAnimator.isRotate())) {
            toApplyHoming(mImage.clip(getScrollX(), getScrollY()));
        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        Log.d(TAG, "onAnimationCancel");
        if (DEBUG) {
            Log.d(TAG, "onAnimationCancel");
        }
        mImage.onHomingCancel(mHomingAnimator.isRotate());
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
        Log.d(TAG, "onAnimationRepeat");
        // empty implementation.
    }

    private boolean onScroll(float dx, float dy) {
        Log.d(TAG, "onScroll");
        Log.d("Scroll ScrollX", getScaleX() + "");
        Log.d("Scroll ScrollY", getScrollY() + "");
        ImageHoming homing = mImage.onScroll(getScrollX(), getScrollY(), -dx, -dy);
        if (homing != null) {
            toApplyHoming(homing);
            return true;
        }
        return onScrollTo(getScrollX() + Math.round(dx), getScrollY() + Math.round(dy));
    }

    private class MoveAdapter extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return ImageViewCustom.this.onScroll(distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // TODO
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    /**
     * 钢笔实体
     */
    private static class Pen extends ImagePath {

        /**
         * event的身份证
         */
        private int identity = Integer.MIN_VALUE;

        void reset() {
            this.path.reset();
            this.identity = Integer.MIN_VALUE;
        }

        void reset(float x, float y) {
            this.path.reset();
            this.path.moveTo(x, y);
            this.identity = Integer.MIN_VALUE;
        }

        void setIdentity(int identity) {
            this.identity = identity;
        }

        boolean isIdentity(int identity) {
            return this.identity == identity;
        }

        void lineTo(float x, float y) {
            this.path.lineTo(x, y);
        }

        boolean isEmpty() {
            return this.path.isEmpty();
        }

        ImagePath toPath() {
            return new ImagePath(new Path(this.path), getMode(), getColor(), getWidth());
        }
    }
}
