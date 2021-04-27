package com.zhongjh.progresslibrary.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;


import androidx.core.content.ContextCompat;

import com.zhongjh.progresslibrary.R;

import java.util.ArrayList;

/**
 * @author yanzhikai_yjk@qq.com
 * 一个在当前进度中心带图片和动画的ProgressBar
 */
public class PictureProgressBar extends View {
    private final String TAG = "PictureProgressBar";
    //画笔
    private Paint paintBackGround, paintBar;
    //颜色
    private int backGroundColor = Color.GRAY, barColor = Color.RED;
    //图片
    private Drawable drawable = null;
    //进度改变监听器
    private OnProgressChangeListener onProgressChangeListener;
    //Drawable的宽高半径
    private int halfDrawableWidth = 0, halfDrawableHeight = 0;
    //Drawable高度偏移量
    private int drawableHeightOffset = 0;
    //是否为圆角
    private boolean isRound = true;
    //圆角半轴
    private int roundX = 20, roundY = 20;
    //进度值和最大值
    private int progress = 0, max = 100;
    //进度条百分比
    private float progressPercentage = 0;
    //进度条当前进度中心点
    private int x, y;
    //是否令设进度条宽高
    private boolean isSetBar = false;
    //进度条宽高
    private int progressWidth = 100, progressHeight = 30;
    //进度条高度偏移量
    private int progressHeightOffset = 0;
    //    //进度条开始结束位置padding
//    private int paddingStart = 10,paddingEnd = 0;
    //进度条刷新时间
    private int refreshTime = 50;

    private final RectF rectFBG = new RectF();
    private final RectF rectFPB = new RectF();

    //是否使用颜色渐变器
    private boolean isGradient = false;
    //颜色渐变器
    private LinearGradient linearGradient;
    //背景图片和进度条图片
    private BitmapDrawable barDrawable, backgroundDrawable;
    private int barDrawableId = 0, backgroundDrawableId = 0;

    private boolean isAnimRun = true;
    //动画模式
    private final static int ANIM_NULL = 0;
    private final static int ANIM_ROTATE = 1;
    private final static int ANIM_SCALE = 2;
    private final static int ANIM_ROTATE_SCALE = 3;
    private final static int ANIM_FRAME = 4;
    private int animMode = ANIM_NULL;

    private int rotateRate = 10;
    private int rotateDegree = 0;
    private float scaleMax = 1.5f, scaleMin = 0.5f;
    private float scaleLevel = 1;
    private float scaleRate = 0.1f;
    private boolean isScaleIncrease = true;

    //帧动画图片
    private int[] drawableIds;
    private final ArrayList<Drawable> drawableList = new ArrayList<>();

    private int frameIndex = 0;
    private int gradientStartColor = Color.RED, gradientEndColor = Color.YELLOW;


    public PictureProgressBar(Context context) {
        super(context);
        init();
    }


    public PictureProgressBar(Context context, AttributeSet attrs) throws Exception {
        super(context, attrs);
        //获取xml属性
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PictureProgressBar, 0, 0);
        backGroundColor = typedArray.getColor(R.styleable.PictureProgressBar_backGroundColor, Color.GRAY);
        barColor = typedArray.getColor(R.styleable.PictureProgressBar_barColor, Color.RED);
        drawable = typedArray.getDrawable(R.styleable.PictureProgressBar_drawable);
        halfDrawableWidth = typedArray.getDimensionPixelSize(R.styleable.PictureProgressBar_halfDrawableWidth, 35);
        halfDrawableHeight = typedArray.getDimensionPixelSize(R.styleable.PictureProgressBar_halfDrawableHeight, 35);
        drawableHeightOffset = typedArray.getDimensionPixelSize(R.styleable.PictureProgressBar_drawableHeightOffset, 0);
        isRound = typedArray.getBoolean(R.styleable.PictureProgressBar_isRound, true);
        roundX = typedArray.getDimensionPixelSize(R.styleable.PictureProgressBar_roundX, 20);
        roundY = typedArray.getDimensionPixelSize(R.styleable.PictureProgressBar_roundY, 20);
        progress = typedArray.getInt(R.styleable.PictureProgressBar_progress, 0);
        max = typedArray.getInt(R.styleable.PictureProgressBar_max, 100);
        isSetBar = typedArray.getBoolean(R.styleable.PictureProgressBar_isSetBar, false);
        progressHeight = typedArray.getDimensionPixelSize(R.styleable.PictureProgressBar_progressHeight, 30);
        progressHeightOffset = typedArray.getDimensionPixelSize(R.styleable.PictureProgressBar_progressHeightOffset, 0);
        refreshTime = typedArray.getInt(R.styleable.PictureProgressBar_refreshTime, 100);
        animMode = typedArray.getInt(R.styleable.PictureProgressBar_animMode, ANIM_NULL);
        rotateRate = typedArray.getInt(R.styleable.PictureProgressBar_rotateRate, 10);
        rotateDegree = typedArray.getInt(R.styleable.PictureProgressBar_rotateDegree, 0);
        scaleMax = typedArray.getFloat(R.styleable.PictureProgressBar_scaleMax, 2);
        scaleMin = typedArray.getFloat(R.styleable.PictureProgressBar_scaleMin, 1);
        scaleRate = typedArray.getFloat(R.styleable.PictureProgressBar_scaleRate, 0.1f);
        gradientStartColor = typedArray.getColor(R.styleable.PictureProgressBar_gradientStartColor, Color.RED);
        gradientEndColor = typedArray.getColor(R.styleable.PictureProgressBar_gradientEndColor, Color.YELLOW);
        isGradient = typedArray.getBoolean(R.styleable.PictureProgressBar_isGradient, false);
        backgroundDrawableId = typedArray.getResourceId(R.styleable.PictureProgressBar_backgroundDrawable, 0);
        barDrawableId = typedArray.getResourceId(R.styleable.PictureProgressBar_barDrawable, 0);
        typedArray.recycle();
        init();
    }

    public PictureProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    //初始化
    private void init() {
        //初始化画笔
        paintBackGround = new Paint();
        paintBackGround.setColor(backGroundColor);

        paintBar = new Paint();
        paintBar.setColor(barColor);

        //在PreDraw时获取View属性,因为在初始化的时候View还没进行Measure
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);
                if (barDrawableId != 0 && backgroundDrawableId != 0) {
                    try {
                        setBarDrawableId(barDrawableId);
                        setBarBackgroundDrawableId(backgroundDrawableId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                //是否需要渐变器
                if (isGradient) {
                    if (barDrawable == null) {
                        linearGradient = new LinearGradient(0, progressHeight / 2, progressWidth, progressHeight / 2, gradientStartColor, gradientEndColor, Shader.TileMode.CLAMP);
                        paintBar.setShader(linearGradient);
                    }
                }
                return false;
            }
        });


    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
//        getHandler().removeCallbacksAndMessages(null);
        //获取进度条当前进度的中心点坐标
        x = (int) ((progressWidth - halfDrawableWidth) * progressPercentage + halfDrawableWidth);
        y = getHeight() / 2;

        drawBar(canvas);
        drawAnimPicture(canvas);
        //回调Draw过程
        postInvalidateDelayed(refreshTime);
    }

    /**
     * 画动画
     * @param canvas canvas
     */
    private void drawAnimPicture(Canvas canvas) {
        if (isAnimRun) {
            switch (animMode) {
                case ANIM_NULL:
                    drawPicture(canvas);
                    break;
                case ANIM_ROTATE:
                    rotateCanvas(canvas);
                    drawPicture(canvas);
                    break;
                case ANIM_SCALE:
                    scaleCanvas(canvas);
                    drawPicture(canvas);
                    break;
                case ANIM_ROTATE_SCALE:
                    rotateCanvas(canvas);
                    scaleCanvas(canvas);
                    drawPicture(canvas);
                    break;
                case ANIM_FRAME:
                    drawable = drawableList.get(frameIndex);
                    if (drawable == null) {
                        drawable = ContextCompat.getDrawable(getContext(), drawableIds[frameIndex]);
                    }
                    drawPicture(canvas);
                    if (frameIndex >= drawableIds.length - 1) {
                        frameIndex = 0;
                    } else {
                        frameIndex++;
                    }
                    break;
                default:
                    break;
            }
        } else {
            drawPicture(canvas);
        }
    }

    // 对backgroundDrawable和barDrawable的图片进行缩放以适应进度条的高度，平铺填充
    private void updateDrawableBounds(int h) {
        if (backgroundDrawable != null && barDrawable != null) {
            int bgWidth;
            int barWidth;
            // 根据Drawable资源的宽高计算缩放比例。
            int intrinsicWidth = backgroundDrawable.getIntrinsicWidth();
            int intrinsicHeight = backgroundDrawable.getIntrinsicHeight();
            int barIntrinsicWidth = barDrawable.getIntrinsicWidth();
            int barIntrinsicHeight = barDrawable.getIntrinsicHeight();
            final float bgIntrinsicAspect = (float) intrinsicWidth / intrinsicHeight;
            final float barIntrinsicAspect = (float) barIntrinsicWidth / barIntrinsicHeight;

            bgWidth = (int) (h * bgIntrinsicAspect);
            barWidth = (int) (h * barIntrinsicAspect);

            float bgScaleX = (float) bgWidth / intrinsicWidth;
            float bgScaleY = (float) h / intrinsicHeight;
            float barScaleX = (float) barWidth / barIntrinsicWidth;
            float barScaleY = (float) h / barIntrinsicHeight;

            Matrix bgMatrix = new Matrix();
            bgMatrix.postScale(bgScaleX, bgScaleY);
            BitmapShader bgBitmapShader = new BitmapShader(backgroundDrawable.getBitmap(), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            bgBitmapShader.setLocalMatrix(bgMatrix);

            Matrix barMatrix = new Matrix();
            barMatrix.postScale(barScaleX, barScaleY);
            BitmapShader barBitmapShader = new BitmapShader(barDrawable.getBitmap(), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
            barBitmapShader.setLocalMatrix(bgMatrix);
//            Log.d(TAG, "updateDrawableBoun;ds: width" + bgWidth);
//            Log.d(TAG, "updateDrawableBounds: height" + bgHeight);

            paintBackGround.setShader(bgBitmapShader);
            paintBar.setShader(barBitmapShader);

            // 设置Drawable的绘制区域。
//            barDrawable.setBounds(0, 0, bgWidth, bgHeight);
//            backgroundDrawable.setBounds(0, 0, bgWidth, bgHeight);
        }
    }

    //画进度条
    private void drawBar(Canvas canvas) {
        if (backgroundDrawable != null && barDrawable != null) {
            //图片进度条
            canvas.save();
            canvas.translate(halfDrawableWidth, y - progressHeight / 2 + progressHeightOffset);
            rectFBG.set(0, 0,
                    progressWidth - halfDrawableWidth, progressHeight);
            rectFPB.set(0, 0,
                    x - halfDrawableWidth, progressHeight);
//            rectFBG.set(0, 0, getWidth(), getHeight());
            canvas.drawRect(rectFBG, paintBackGround);
            canvas.drawRect(rectFPB, paintBar);
            canvas.restore();
        } else {
            //非图片背景处理
            rectFBG.set(halfDrawableWidth, y - progressHeight / 2 + progressHeightOffset,
                    progressWidth, y + progressHeight / 2 + progressHeightOffset);
            rectFPB.set(halfDrawableWidth, y - progressHeight / 2 + progressHeightOffset,
                    x, y + progressHeight / 2 + progressHeightOffset);
            if (isRound) {
                canvas.drawRoundRect(rectFBG, roundX, roundY, paintBackGround);
                if (x > halfDrawableWidth * 2) {
                    canvas.drawRoundRect(rectFPB, roundX, roundY, paintBar);
                }
            } else {
                canvas.drawRect(rectFBG, paintBackGround);
                canvas.drawRect(rectFPB, paintBar);
            }
        }
    }

    //旋转画布
    private void rotateCanvas(Canvas canvas) {
        canvas.rotate(rotateDegree % 360, x, y + drawableHeightOffset);
        rotateDegree += rotateRate;
    }

    //伸缩画布
    private void scaleCanvas(Canvas canvas) {
        if (scaleLevel >= scaleMax) {
            isScaleIncrease = false;
        } else if (scaleLevel <= scaleMin) {
            isScaleIncrease = true;
        }
        if (isScaleIncrease) {
            scaleLevel += scaleRate;
        } else {
            scaleLevel -= scaleRate;
        }
        canvas.scale(scaleLevel, scaleLevel, x, y + drawableHeightOffset);
    }

    //画图
    private void drawPicture(Canvas canvas) {
        if (drawable == null && animMode != ANIM_NULL) {
            Log.e(TAG, "drawable is null");
            return;
        }
        assert drawable != null;
        drawable.setBounds(x - halfDrawableWidth,
                getHeight() / 2 - halfDrawableHeight + drawableHeightOffset,
                x + halfDrawableWidth,
                getHeight() / 2 + halfDrawableHeight + drawableHeightOffset);
        drawable.draw(canvas);
    }

    //重写onMeasure，以自定义获取进度条的宽高
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);


        if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            //在这里实现计算需要wrap_content时需要的宽
            width = halfDrawableWidth * 2;
        }
        if (getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            //在这里实现计算需要wrap_content时需要的高
            height = halfDrawableHeight * 2;
        }

        progressWidth = width;
        //如果不是自定义设置进度条高度，就直接把高度当作进度条高度
        if (!isSetBar) {
            progressHeight = height;
        }

        //如果有图片，就为图片预留空间
        if (drawable != null || drawableIds != null) {
            progressWidth = width - halfDrawableWidth;
        }

        Log.d(TAG, "onMeasure: progressWidth " + progressWidth);
        //传入处理后的宽高
        setMeasuredDimension(width, height);
    }


    // 设置进度
    public synchronized void setProgress(int progress) {
        if (progress <= max) {
            this.progress = progress;
        } else if (progress < 0) {
            this.progress = 0;
        } else {
            this.progress = max;
        }
        progressPercentage = progress / (float) max;
        doProgressRefresh();
    }

    //进行进度改变之后的操作
    private synchronized void doProgressRefresh() {
        if (onProgressChangeListener != null) {
            onProgressChangeListener.onOnProgressChange(progress);
            if (progress >= max) {
                onProgressChangeListener.onOnProgressFinish();
            }
        }
    }


    // 设置动画开关
    public void setAnimRun(boolean isAnimRun) {
        this.isAnimRun = isAnimRun;
    }

    // 设置帧动画时要传入的图片ID数组
    public void setDrawableIds(int[] drawableIds) {
        this.drawableIds = drawableIds;
        drawableList.clear();
        for (int id : drawableIds) {
            drawableList.add(ContextCompat.getDrawable(getContext(), id));
        }
    }

    // 设置图片
    public void setPicture(int id) {
        drawable = ContextCompat.getDrawable(getContext(), id);
    }

    // 设置颜色渐变器
    public void setLinearGradient(LinearGradient linearGradient) {
        this.linearGradient = linearGradient;
    }

    // 设置进度条图片
    public void setBarBackgroundDrawableId(int id) throws Exception {
        Drawable drawable = ContextCompat.getDrawable(getContext(), id);
        if (drawable instanceof BitmapDrawable) {
            backgroundDrawable = (BitmapDrawable) drawable;
            updateDrawableBounds(progressHeight);
        } else {
            throw new Exception("输入的id不是BitmapDrawable的id");
        }
    }

    /**
     * 设置进度条背景图片
     *
     * @param id 图片id
     * @throws Exception 异常
     */
    public void setBarDrawableId(int id) throws Exception {
        Drawable drawable = ContextCompat.getDrawable(getContext(), id);
        if (drawable instanceof BitmapDrawable) {
            barDrawable = (BitmapDrawable) drawable;
            updateDrawableBounds(progressHeight);
        } else {
            throw new Exception("输入的id不是BitmapDrawable的id");
        }
    }

    public LinearGradient getLinearGradient() {
        return linearGradient;
    }

    public int getProgress() {
        return progress;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMax() {
        return max;
    }

    public void setBackGroundColor(int backGroundColor) {
        this.backGroundColor = backGroundColor;
    }

    public int getBackGroundColor() {
        return backGroundColor;
    }

    public void setBarColor(int barColor) {
        this.barColor = barColor;
    }

    public int getBarColor() {
        return barColor;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setIsRound(boolean isRound) {
        this.isRound = isRound;
    }

    public boolean getIsRound() {
        return isRound;
    }

    public void setProgressHeight(int progressHeight) {
        this.progressHeight = progressHeight;
    }

    public int getProgressHeight() {
        return progressHeight;
    }

    public void setProgressHeightOffset(int progressHeightOffset) {
        this.progressHeightOffset = progressHeightOffset;
    }

    public int getProgressHeightOffset() {
        return progressHeightOffset;
    }

    public void setHalfDrawableWidth(int halfDrawableWidth) {
        this.halfDrawableWidth = halfDrawableWidth;
    }

    public int getHalfDrawableWidth() {
        return halfDrawableWidth;
    }

    public void setDrawableHeightOffset(int drawableHeightOffset) {
        this.drawableHeightOffset = drawableHeightOffset;
    }

    public int getDrawableHeightOffset() {
        return drawableHeightOffset;
    }

    public void setHalfDrawableHeight(int halfDrawableHeight) {
        this.halfDrawableHeight = halfDrawableHeight;
    }

    public int getHalfDrawableHeight() {
        return halfDrawableHeight;
    }

    public void setAnimMode(int animMode) {
        this.animMode = animMode;
    }

    public int getAnimMode() {
        return animMode;
    }

    public void setRotateRate(int rotateRate) {
        this.rotateRate = rotateRate;
    }

    public int getRotateRate() {
        return rotateRate;
    }

    public void setRefreshTime(int refreshTime) {
        this.refreshTime = refreshTime;
    }

    public int getRefreshTime() {
        return refreshTime;
    }

    public void setGradientStartColor(int gradientStartColor) {
        this.gradientStartColor = gradientStartColor;
    }

    public void setGradientEndColor(int gradientEndColor) {
        this.gradientEndColor = gradientEndColor;
    }

    public void setScaleMin(float scaleMin) {
        this.scaleMin = scaleMin;
    }

    public float getScaleMin() {
        return scaleMin;
    }

    public void setScaleMax(float scaleMax) {
        this.scaleMax = scaleMax;
    }

    public float getScaleMax() {
        return scaleMax;
    }

    public void setScaleRate(float scaleRate) {
        this.scaleRate = scaleRate;
    }

    public float getScaleRate() {
        return scaleRate;
    }

    public void setRotateDegree(int rotateDegree) {
        this.rotateDegree = rotateDegree;
    }

    public int getRotateDegree() {
        return rotateDegree;
    }

    public void setRoundX(int roundX) {
        this.roundX = roundX;
    }

    public void setRoundY(int roundY) {
        this.roundY = roundY;
    }


    //设置进度监听器
    public void setOnProgressChangeListener(OnProgressChangeListener onProgressChangeListener) {
        this.onProgressChangeListener = onProgressChangeListener;
    }

    //进度监听器
    public interface OnProgressChangeListener {
        //进度改变时的回调
        void onOnProgressChange(int progress);

        //进度完成时的回调
        void onOnProgressFinish();
    }
}
