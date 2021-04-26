package com.zhongjh.albumcamerarecorder.widget.clickorlongbutton;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Looper;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.camera.listener.ClickOrLongListener;
import gaode.zhongjh.com.common.utils.DisplayMetricsUtils;

import java.util.ArrayList;

import static com.zhongjh.albumcamerarecorder.camera.common.Constants.BUTTON_STATE_BOTH;
import static com.zhongjh.albumcamerarecorder.camera.common.Constants.BUTTON_STATE_ONLY_CLICK;
import static com.zhongjh.albumcamerarecorder.camera.common.Constants.BUTTON_STATE_ONLY_LONGCLICK;

/**
 * 点击或者长按的按钮
 */
public class ClickOrLongButton extends View {

    private static final String TAG = "ClickOrLongButton";
    private float timeLimitInMils = 10000.0F;       // 录制时间
    private final ArrayList<Float> mCurrentLocation = new ArrayList<>();// 当前录制位置集，计算时间后的百分比
    private Float mCurrentSumNumberDegrees = 0F; // 当前录制的节点，以360度为单位
    private Long mCurrentSumTime = 0L; // 当前录制的时间点
    private int mMinDuration = 1500;       // 最短录制时间限制
    private long mRecordedTime;             // 记录当前录制多长的时间秒
    private static final float PROGRESS_LIM_TO_FINISH_STARTING_ANIM = 0.1F;
    private int BOUNDING_BOX_SIZE;
    private int OUT_CIRCLE_WIDTH;
    private int OUTER_CIRCLE_WIDTH_INC;
    private float INNER_CIRCLE_RADIUS;

    private TouchTimeHandler touchTimeHandler;
    private boolean touchable;
    private boolean recordable;

    private Paint centerCirclePaint;
    private Paint outBlackCirclePaint;
    private Paint outMostBlackCirclePaint;
    private float innerCircleRadiusToDraw;
    private RectF outMostCircleRect; // 外圈的画布
    private float outBlackCircleRadius;
    private float outMostBlackCircleRadius;
    private int colorRoundBorder;
    private int colorRecord;
    private int colorWhiteP60;
    private int colorInterval;
    //top
    private float startAngle270;
    private float percentInDegree;
    private float centerX;
    private float centerY;
    private Paint processBarPaint; // 按下去显示的进度外圈
    private Paint outMostWhiteCirclePaint; // 静止状态时的外圈
    private Paint outProcessCirclePaint; // 静止状态时的进度外圈
    private Paint outProcessIntervalCirclePaint; // 静止状态时的进度外圈
    private Paint translucentPaint;
    private int translucentCircleRadius = 0;
    private float outMostCircleRadius;
    private float innerCircleRadiusWhenRecord;
    private long btnPressTime;
    private int outBlackCircleRadiusInc;
    private int recordState;                        // 当前状态
    private static final int RECORD_NOT_STARTED = 0; // 未启动状态
    private static final int RECORD_STARTED = 1;     // 启动状态
    private static final int RECORD_ENDED = 2;       // 结束状态

    private int mButtonState;        // 按钮可执行的功能状态（拍照,录制,两者）
    private boolean mIsSectionMode; // 是否分段录制的模式


    private TouchTimeHandler.Task updateUITask = new TouchTimeHandler.Task() {
        @Override
        public void run() {
            if (mIsSectionMode && mCurrentLocation.size() > 0) {
                // 当处于分段录制模式并且有分段数据的时候，关闭启动前奏
                mMinDuration = 0;
            }
            long timeLapse = System.currentTimeMillis() - btnPressTime;
            mRecordedTime = (timeLapse - mMinDuration);
            mRecordedTime = mRecordedTime + mCurrentSumTime;
            float percent = mRecordedTime / timeLimitInMils;
            Log.d(TAG, "mCurrentSumTime " + mCurrentSumTime);
            Log.d(TAG, "mRecordedTime " + mRecordedTime);
            if (!isActionDown && timeLapse >= 1) {
                if (mClickOrLongListener != null && (mButtonState == BUTTON_STATE_ONLY_CLICK || mButtonState == BUTTON_STATE_BOTH)) {
                    // 如果禁止点击也不能触发该事件
                    mClickOrLongListener.actionDown();
                    isActionDown = true;
                }
            }

            if (timeLapse >= mMinDuration) {
                synchronized (ClickOrLongButton.this) {
                    if (recordState == RECORD_NOT_STARTED) {
                        recordState = RECORD_STARTED;
                        if (mClickOrLongListener != null) {
                            mClickOrLongListener.onLongClick();
                            // 如果禁止点击，那么就轮到长按触发actionDown
                            if (!isActionDown && mClickOrLongListener != null && mButtonState == BUTTON_STATE_ONLY_LONGCLICK) {
                                // 如果禁止点击也不能触发该事件
                                mClickOrLongListener.actionDown();
                                isActionDown = true;
                            }
                        }
                    }
                }
                if (!recordable) {
                    return;
                }
                centerCirclePaint.setColor(colorRecord);
                outMostWhiteCirclePaint.setColor(colorRoundBorder);
                percentInDegree = (360.0F * percent);
                if (mIsSectionMode) {
                    if (mCurrentLocation.size() > 0 || (timeLapse - mMinDuration) >= mMinDuration) {
                        mCurrentSumNumberDegrees = percentInDegree;
                    }
                }

                Log.d(TAG, "timeLapse:" + timeLapse);
                Log.d(TAG, "percent:" + percent);
                Log.d(TAG, "percentInDegree:" + percentInDegree);

                if (percent <= 1.0F) {
                    if (percent <= PROGRESS_LIM_TO_FINISH_STARTING_ANIM) {
                        float calPercent = percent / PROGRESS_LIM_TO_FINISH_STARTING_ANIM;
                        float outIncDis = outBlackCircleRadiusInc * calPercent;
                        float curOutCircleWidth = OUT_CIRCLE_WIDTH + OUTER_CIRCLE_WIDTH_INC * calPercent;
                        processBarPaint.setStrokeWidth(curOutCircleWidth);
                        outProcessCirclePaint.setStrokeWidth(curOutCircleWidth);
                        outProcessIntervalCirclePaint.setStrokeWidth(curOutCircleWidth);
                        outMostWhiteCirclePaint.setStrokeWidth(curOutCircleWidth);
                        outBlackCircleRadius = (outMostCircleRadius + outIncDis - curOutCircleWidth / 2.0F);
                        outMostBlackCircleRadius = (curOutCircleWidth / 2.0F + (outMostCircleRadius + outIncDis));
                        outMostCircleRect = new RectF(centerX - outMostCircleRadius - outIncDis, centerY - outMostCircleRadius - outIncDis, centerX + outMostCircleRadius + outIncDis, centerY + outMostCircleRadius + outIncDis);
                        translucentCircleRadius = (int) (outIncDis + outMostCircleRadius);
                        innerCircleRadiusToDraw = calPercent * innerCircleRadiusWhenRecord;
                    }
                    invalidate();
                } else {
                    reset();
                }
            }
        }
    };

    public ClickOrLongButton(Context paramContext) {
        super(paramContext);
        init();
    }

    public ClickOrLongButton(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        init();
    }

    public ClickOrLongButton(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
        init();
    }

    private void init() {
        touchable = recordable = true;
        BOUNDING_BOX_SIZE = DisplayMetricsUtils.dip2px(100.0F); // 整块
        OUT_CIRCLE_WIDTH = DisplayMetricsUtils.dip2px(2.3F);// 外线宽度
        OUTER_CIRCLE_WIDTH_INC = DisplayMetricsUtils.dip2px(4.3F);
        INNER_CIRCLE_RADIUS = DisplayMetricsUtils.dip2px(32.0F);

        // 调取样式中的颜色
        TypedArray arrayRoundBorder = getContext().getTheme().obtainStyledAttributes(new int[]{R.attr.click_long_button_round_border});
        int defaultRoundBorderColor = ResourcesCompat.getColor(
                getResources(), R.color.click_long_button_round_border,
                getContext().getTheme());
        TypedArray arrayInnerCircleInOperation = getContext().getTheme().obtainStyledAttributes(new int[]{R.attr.click_long_button_inner_circle_in_operation});
        int defaultInnerCircleInOperationColor = ResourcesCompat.getColor(
                getResources(), R.color.click_long_button_inner_circle_in_operation,
                getContext().getTheme());
        TypedArray arrayInnerCircleNoOperation = getContext().getTheme().obtainStyledAttributes(new int[]{R.attr.click_long_button_inner_circle_no_operation});
        int defaultInnerCircleNoOperationColor = ResourcesCompat.getColor(
                getResources(), R.color.click_long_button_inner_circle_no_operation,
                getContext().getTheme());

        TypedArray arrayInnerCircleNoOperationInterval = getContext().getTheme().obtainStyledAttributes(new int[]{R.attr.click_button_inner_circle_in_operation_interval});
        int defaultInnerCircleNoOperationColorInterval = ResourcesCompat.getColor(
                getResources(), R.color.click_button_inner_circle_no_operation_interval,
                getContext().getTheme());

        colorRecord = arrayInnerCircleInOperation.getColor(0, defaultInnerCircleInOperationColor);
        colorRoundBorder = arrayRoundBorder.getColor(0, defaultRoundBorderColor);
        colorWhiteP60 = arrayInnerCircleNoOperation.getColor(0, defaultInnerCircleNoOperationColor);
        colorInterval = arrayInnerCircleNoOperationInterval.getColor(0, defaultInnerCircleNoOperationColorInterval);
        int colorBlackP40 = ContextCompat.getColor(getContext(), R.color.black_forty_percent);
        int colorBlackP80 = ContextCompat.getColor(getContext(), R.color.black_eighty_percent);
        int colorTranslucent = ContextCompat.getColor(getContext(), R.color.circle_shallow_translucent_bg);
        // 内圈操作中样式
        processBarPaint = new Paint();
        processBarPaint.setColor(colorRecord);
        processBarPaint.setAntiAlias(true);
        processBarPaint.setStrokeWidth(OUT_CIRCLE_WIDTH);
        processBarPaint.setStyle(Style.STROKE);
        processBarPaint.setStrokeCap(Cap.ROUND);
        // 外圈样式
        outMostWhiteCirclePaint = new Paint();
        outMostWhiteCirclePaint.setColor(colorRoundBorder);
        outMostWhiteCirclePaint.setAntiAlias(true);
        outMostWhiteCirclePaint.setStrokeWidth(OUT_CIRCLE_WIDTH);
        outMostWhiteCirclePaint.setStyle(Style.STROKE);

        outProcessCirclePaint = new Paint();
        outProcessCirclePaint.setColor(colorRecord);
        outProcessCirclePaint.setAntiAlias(true);
        outProcessCirclePaint.setStrokeWidth(OUT_CIRCLE_WIDTH);
        outProcessCirclePaint.setStyle(Style.STROKE);

        outProcessIntervalCirclePaint = new Paint();
        outProcessIntervalCirclePaint.setColor(colorInterval);
        outProcessIntervalCirclePaint.setAntiAlias(true);
        outProcessIntervalCirclePaint.setStrokeWidth(OUT_CIRCLE_WIDTH);
        outProcessIntervalCirclePaint.setStyle(Style.STROKE);

        // 内圈未操作中样式
        centerCirclePaint = new Paint();
        centerCirclePaint.setColor(colorWhiteP60);
        centerCirclePaint.setAntiAlias(true);
        centerCirclePaint.setStyle(Style.FILL_AND_STROKE);
        outBlackCirclePaint = new Paint();
        outBlackCirclePaint.setColor(colorBlackP40);
        outBlackCirclePaint.setAntiAlias(true);
        outBlackCirclePaint.setStyle(Style.STROKE);
        outBlackCirclePaint.setStrokeWidth(1.0F);
        outMostBlackCirclePaint = new Paint();
        outMostBlackCirclePaint.setColor(colorBlackP80);
        outMostBlackCirclePaint.setAntiAlias(true);
        outMostBlackCirclePaint.setStyle(Style.STROKE);
        outMostBlackCirclePaint.setStrokeWidth(1.0F);
        translucentPaint = new Paint();
        translucentPaint.setColor(colorTranslucent);
        translucentPaint.setAntiAlias(true);
        translucentPaint.setStyle(Style.FILL_AND_STROKE);
        centerX = (BOUNDING_BOX_SIZE / 2);
        centerY = (BOUNDING_BOX_SIZE / 2);
        outMostCircleRadius = DisplayMetricsUtils.dip2px(37.0F);
        outBlackCircleRadiusInc = DisplayMetricsUtils.dip2px(7.0F);
        innerCircleRadiusWhenRecord = DisplayMetricsUtils.dip2px(35.0F);
        innerCircleRadiusToDraw = INNER_CIRCLE_RADIUS;
        outBlackCircleRadius = (outMostCircleRadius - OUT_CIRCLE_WIDTH / 2.0F);
        outMostBlackCircleRadius = (outMostCircleRadius + OUT_CIRCLE_WIDTH / 2.0F);
        startAngle270 = 270.0F;
        percentInDegree = 0.0F;
        outMostCircleRect = new RectF(centerX - outMostCircleRadius, centerY - outMostCircleRadius, centerX + outMostCircleRadius, centerY + outMostCircleRadius);
        touchTimeHandler = new TouchTimeHandler(Looper.getMainLooper(), updateUITask);
        mButtonState = BUTTON_STATE_BOTH;   // 状态为两者都可以
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(centerX, centerY, translucentCircleRadius, translucentPaint);

        //center white-p40 circle  中心点+半径32，所以直接64就是内圈的宽高度了
        canvas.drawCircle(centerX, centerY, innerCircleRadiusToDraw, centerCirclePaint);

        // 静止状态时的外圈
        canvas.drawArc(outMostCircleRect, startAngle270, 360F, false, outMostWhiteCirclePaint);

        // 点击时的外圈进度
        canvas.drawArc(outMostCircleRect, startAngle270, percentInDegree, false, processBarPaint);

        // 静止状态时的外圈进度
        canvas.drawArc(outMostCircleRect, startAngle270, mCurrentSumNumberDegrees, false, outProcessCirclePaint);
        Log.d(TAG, "onDraw percentInDegree" + percentInDegree);
        Log.d(TAG, "onDraw mCurrentSumNumberDegrees" + mCurrentSumNumberDegrees);

        // 从这个顺序来看，即是从270为开始
        for (Float item : mCurrentLocation) {
            canvas.drawArc(outMostCircleRect, item, 3, false, outProcessIntervalCirclePaint);
            Log.d(TAG, "canvas.drawArc " + item);
        }

        canvas.drawCircle(centerX, centerY, outBlackCircleRadius, outBlackCirclePaint);
        canvas.drawCircle(centerX, centerY, outMostBlackCircleRadius, outMostBlackCirclePaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(BOUNDING_BOX_SIZE, BOUNDING_BOX_SIZE);
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        if (!touchable) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "onTouchEvent: down");
                // 禁止长按方式
                if (mClickOrLongListener != null
                        && (mButtonState == BUTTON_STATE_ONLY_LONGCLICK || mButtonState == BUTTON_STATE_BOTH)) {
                    startTicking();
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "onTouchEvent: up");
                reset();
                break;

        }
        return true;
    }

    /**
     * 重置
     */
    private void reset() {
        Log.d(TAG, "reset: " + recordState);
        synchronized (ClickOrLongButton.this) {
            if (recordState == RECORD_STARTED) {
                if (mClickOrLongListener != null) {
                    if (mRecordedTime < mMinDuration) {
                        mClickOrLongListener.onLongClickShort(mRecordedTime); // 回调录制时间过短
                    } else {
                        mClickOrLongListener.onLongClickEnd(mRecordedTime); // 回调录制结束
                    }
                }
                recordState = RECORD_ENDED;
            } else if (recordState == RECORD_ENDED) {
                recordState = RECORD_NOT_STARTED; // 回到初始状态
            } else {
                if (mClickOrLongListener != null) {
                    mClickOrLongListener.onClick(); // 拍照
                }
            }
        }
        isActionDown = false;
        touchTimeHandler.clearMsg();
        percentInDegree = 0.0F;
        centerCirclePaint.setColor(colorWhiteP60);
        outMostWhiteCirclePaint.setColor(colorRoundBorder);
        innerCircleRadiusToDraw = INNER_CIRCLE_RADIUS;
        outMostCircleRect = new RectF(centerX - outMostCircleRadius, centerY - outMostCircleRadius, centerX + outMostCircleRadius, centerY + outMostCircleRadius);
        translucentCircleRadius = 0;
        processBarPaint.setStrokeWidth(OUT_CIRCLE_WIDTH);
        outProcessCirclePaint.setStrokeWidth(OUT_CIRCLE_WIDTH);
        outMostWhiteCirclePaint.setStrokeWidth(OUT_CIRCLE_WIDTH);
        outProcessIntervalCirclePaint.setStrokeWidth(OUT_CIRCLE_WIDTH);
        outBlackCircleRadius = (outMostCircleRadius - OUT_CIRCLE_WIDTH / 2.0F);
        outMostBlackCircleRadius = (outMostCircleRadius + OUT_CIRCLE_WIDTH / 2.0F);
        invalidate();
    }

    public boolean isTouchable() {
        return touchable;
    }

    public boolean isRecordable() {
        return recordable;
    }

    public void setRecordable(boolean recordable) {
        this.recordable = recordable;
    }

    public void setTouchable(boolean touchable) {
        this.touchable = touchable;
    }

    private void startTicking() {
        synchronized (ClickOrLongButton.this) {
            if (recordState != RECORD_NOT_STARTED) {
                recordState = RECORD_NOT_STARTED;
            }
        }
        btnPressTime = System.currentTimeMillis();
        touchTimeHandler.sendLoopMsg(0L, 16L);
    }

    /**
     * 数据设置成适合当前圆形
     * <p>
     * // 计算方式1：270至360是一个初始点，类似0-90
     * // 计算方式2: 所以如果是小于90点，就直接+270
     * // 计算方式3：如果大于等于90点，就直接-90
     *
     * @return numberDegrees
     */
    private float getNumberDegrees(float numberDegrees) {
        if (numberDegrees >= 90) {
            numberDegrees = numberDegrees - 90;
        } else {
            numberDegrees = numberDegrees + 270;
        }
        return numberDegrees;
    }

    private ClickOrLongListener mClickOrLongListener;       // 按钮回调接口
    private boolean isActionDown;                           // 判断是否已经调用过isActionDwon,结束后重置此值

    // region 对外方法

    /**
     * 设置最长录制时间
     *
     * @param duration 时间
     */
    public void setDuration(int duration) {
        timeLimitInMils = duration;
    }

    /**
     * 最短录制时间
     *
     * @param duration 时间
     */
    public void setMinDuration(int duration) {
        mMinDuration = duration;
    }

    /**
     * 设置当前已录制的时间，用于分段录制
     */
    public void setCurrentTime(ArrayList<Long> currentTimes) {
        mCurrentLocation.clear();
        mCurrentSumNumberDegrees = 0F;
        mCurrentSumTime = 0L;
        // 当前录制时间集
        // 计算百分比红点
        for (int i = 0; i < currentTimes.size(); i++) {
            // 获取当前时间占比
            float percent = currentTimes.get(i) / timeLimitInMils;
            // 根据360度，以这个占比计算是具体多少度
            float numberDegrees = percent * 360;
            // 数据设置规范,适合当前圆形
            mCurrentLocation.add(getNumberDegrees(numberDegrees));
            mCurrentSumNumberDegrees = numberDegrees;

            mCurrentSumTime = currentTimes.get(i);
            Log.d(TAG, "setCurrentTime mCurrentSumTime " + mCurrentSumTime);
            Log.d(TAG, "setCurrentTime mCurrentSumNumberDegrees " + mCurrentSumNumberDegrees);
        }
    }

    /**
     * 设置是否分段模式，分段录制的动画稍微不一样
     */
    public void setSectionMode(boolean isSectionMode) {
        this.mIsSectionMode = isSectionMode;
    }

    /**
     * 设置回调接口
     *
     * @param clickOrLongListener 回调接口
     */
    public void setRecordingListener(ClickOrLongListener clickOrLongListener) {
        this.mClickOrLongListener = clickOrLongListener;
    }

    /**
     * 设置按钮功能（点击和长按）
     *
     * @param buttonStateBoth {@link com.zhongjh.albumcamerarecorder.camera.common.Constants#BUTTON_STATE_ONLY_CLICK 只能点击
     * @link com.zhongjh.albumcamerarecorder.camera.common.Constants#BUTTON_STATE_ONLY_LONGCLICK 只能长按
     * @link com.zhongjh.albumcamerarecorder.camera.common.Constants#BUTTON_STATE_BOTH 两者皆可
     * }
     */
    public void setButtonFeatures(int buttonStateBoth) {
        this.mButtonState = buttonStateBoth;
    }

    /**
     * 重置状态
     */
    public void resetState() {
        recordState = RECORD_NOT_STARTED;// 回到初始状态
    }

    // endregion

}
