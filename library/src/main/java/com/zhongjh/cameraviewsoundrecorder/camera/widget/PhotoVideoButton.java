package com.zhongjh.cameraviewsoundrecorder.camera.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.zhongjh.cameraviewsoundrecorder.camera.listener.ClickOrLongListener;
import com.zhongjh.cameraviewsoundrecorder.camera.util.PermissionUtil;

import static com.zhongjh.cameraviewsoundrecorder.camera.common.Constants.BUTTON_STATE_BOTH;
import static com.zhongjh.cameraviewsoundrecorder.camera.common.Constants.BUTTON_STATE_ONLY_CLICK;
import static com.zhongjh.cameraviewsoundrecorder.camera.common.Constants.BUTTON_STATE_ONLY_LONGCLICK;


/**
 * 动作按钮：拍照，录像，录音
 * 作废
 * @deprecated
 * Created by zhongjh on 2018/7/23.
 */
public class PhotoVideoButton extends View {

    public static final int STATE_IDLE = 0x001;        // 空闲状态
    public static final int STATE_PRESS = 0x002;       // 按下状态
    public static final int STATE_LONG_PRESS = 0x003;  // 长按状态
    public static final int STATE_RECORDERING = 0x004; // 录制状态
    public static final int STATE_BAN = 0x005;         // 禁止状态

    private int mState;              // 当前按钮状态
    private int mButtonState;        // 按钮可执行的功能状态（拍照,录制,两者）

    private int mProgressColor = 0xEE16AE16;            // 进度条颜色
    private int mOutsideColor = 0xEEDCDCDC;             // 外圆背景色
    private int mInsideColor = 0xFFFFFFFF;              // 内圆背景色
    private int mButtonSize;                // 按钮大小
    private float mButtonRadius;            // 按钮半径
    private float mButtonOutsideRadius;     // 外圆半径
    private float mButtonInsideRadius;      // 内圆半径
    private int mOutsideAddSize;         // 长按外圆半径变大的Size
    private int mInsideReduceSize;       // 长安内圆缩小的Size
    private float mStrokeWidth;          // 进度条宽度

    private float mProgress;         //录制视频的进度
    private int mDuration;           //录制视频最大时间长度
    private int mMinDuration;       //最短录制时间限制
    private int mRecordedTime;      //记录当前录制的时间

    private float event_Y;  // Touch_Event_Down时候记录的Y值
    //中心坐标
    private float mCenterX;     // 圆心的X坐标
    private float mCenterY;     // 圆心的Y坐标

    // 画笔
    private Paint mPaint;

    private RectF mRectF;

    private LongPressRunnable mLongPressRunnable;        //长按后处理的逻辑Runnable
    private ClickOrLongListener mClickOrLongListener;       //按钮回调接口
    private RecordCountDownTimer mTimer;                //计时器

    public PhotoVideoButton(Context context) {
        super(context);
    }

    public PhotoVideoButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoVideoButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        init((int) (getMeasuredWidth() * 0.7)); // 考虑到长按的外圆变大，所以默认圆形的宽度是该控件的70%
        // 计算大小
//        setMeasuredDimension(mButtonSize + mOutsideAddSize * 2, mButtonSize + mOutsideAddSize * 2);
        setMeasuredDimension(getMeasuredWidth(),getMeasuredWidth());
    }


    public void init(int size) {
        if (this.mButtonSize != 0 && this.mButtonSize != -1)
            return;
        this.mButtonSize = size;
        this.mButtonRadius = size / 2.0f; // 计算半径
        mButtonOutsideRadius = mButtonRadius; // 外圆半径
        mButtonInsideRadius = mButtonRadius * 0.75f; // 内圆半径
        mStrokeWidth = size / 15;       // 线条占据直径的1/15
        mOutsideAddSize = size / 5;     // 长按-外圆半径变大的Size
        mInsideReduceSize = size / 8;   // 长按-内圆缩小的Size

        // 实例化画笔
        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        mProgress = 0; // 初始化进度为0
        mLongPressRunnable = new LongPressRunnable(); // 初始化长按线程
        mState = STATE_IDLE;                //初始化为空闲状态
        mButtonState = BUTTON_STATE_BOTH;   // 状态为两者都可以
        mDuration = 10 * 1000;              //默认最长录制时间为10s
        mMinDuration = 1500;              //默认最短录制时间为1.5s

        mCenterX = (mButtonSize + mOutsideAddSize * 2) / 2;
        mCenterY = (mButtonSize + mOutsideAddSize * 2) / 2;

        // 设置该按钮录制进度条的画纸范围
        mRectF = new RectF(
                mCenterX - (mButtonRadius + mOutsideAddSize - mStrokeWidth / 2),
                mCenterY - (mButtonRadius + mOutsideAddSize - mStrokeWidth / 2),
                mCenterX + (mButtonRadius + mOutsideAddSize - mStrokeWidth / 2),
                mCenterY + (mButtonRadius + mOutsideAddSize - mStrokeWidth / 2));

        //录制定时器
        mTimer = new RecordCountDownTimer(mDuration, mDuration / 360);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setStyle(Paint.Style.FILL);

        // 外圆（半透明灰色）
        mPaint.setColor(mOutsideColor);
        canvas.drawCircle(mCenterX, mCenterY, mButtonOutsideRadius, mPaint);

        // 内圆（白色）
        mPaint.setColor(mInsideColor);
        canvas.drawCircle(mCenterX, mCenterY, mButtonInsideRadius, mPaint);

        // 如果状态为录制状态，则绘制录制进度条
        if (mState == STATE_RECORDERING) {
            mPaint.setColor(mProgressColor);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mStrokeWidth);
            canvas.drawArc(mRectF, -90, mProgress, false, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mClickOrLongListener != null)
                    mClickOrLongListener.actionDown();
                // 按下触发事件,必须当前状态是空闲状态
                if (event.getPointerCount() > 1 || mState != STATE_IDLE)
                    break;
                event_Y = event.getY(); // 记录Y值
                mState = STATE_PRESS;        //修改当前状态为点击按下

                // 判断按钮状态是否为可录制状态
                if ((mButtonState == BUTTON_STATE_ONLY_LONGCLICK || mButtonState == BUTTON_STATE_BOTH))
                    postDelayed(mLongPressRunnable, 500);    //同时延长500启动长按后处理的逻辑Runnable
                break;
            case MotionEvent.ACTION_MOVE:
                if (mClickOrLongListener != null
                        && mState == STATE_RECORDERING
                        && (mButtonState == BUTTON_STATE_ONLY_LONGCLICK || mButtonState == BUTTON_STATE_BOTH)) {
                    // 记录当前Y值与按下时候Y值的差值，调用缩放回调接口
                    mClickOrLongListener.onLongClickZoom(event_Y - event.getY());
                }
                break;
            case MotionEvent.ACTION_UP:
                // 根据当前按钮的状态进行相应的处理
                handlerUnpressByState();
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

    /**
     * 长按线程
     * 通过按钮 按下后经过500毫秒则会修改当前状态为长按状态
     */
    private class LongPressRunnable implements Runnable {
        @Override
        public void run() {
            // 判断如果当前是休闲状态则不做任何事
            if (mState == STATE_IDLE)
                return;

            mState = STATE_LONG_PRESS;
            //没有录制权限
            if (PermissionUtil.getRecordState() != PermissionUtil.STATE_SUCCESS) {
                mState = STATE_IDLE;
                if (mClickOrLongListener != null) {
                    mClickOrLongListener.onLongClickError();
                    return;
                }
            }



            //按住按钮的动画：外圆变大，内圆缩小
            startRecordAnimation(
                    mButtonOutsideRadius,
                    mButtonOutsideRadius + mOutsideAddSize,
                    mButtonInsideRadius,
                    mButtonInsideRadius - mInsideReduceSize
            );
        }
    }

    /**
     * 录制视频计时器
     * 每隔一秒，进度条进一格子
     */
    private class RecordCountDownTimer extends CountDownTimer {

        /**
         * @param millisInFuture    millisInFuture秒后结束调用方法
         * @param countDownInterval 每隔countDownInterval秒后执行一次
         */
        RecordCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            updateProgress(millisUntilFinished);
        }

        @Override
        public void onFinish() {
            // 完成后设置进度为0
            updateProgress(0);
            // 录制结束
            recordEnd();
        }

    }

    /**
     * 更新进度条
     *
     * @param millisUntilFinished
     */
    private void updateProgress(long millisUntilFinished) {
        mRecordedTime = (int) (mDuration - millisUntilFinished); // 最大时间长度 - 前进的进度 = 当前进度
        mProgress = 360f - millisUntilFinished / (float) mDuration * 360f;
        // 重新绘画
        invalidate();
    }

    /**
     * 当手指松开按钮时候处理的逻辑
     */
    private void handlerUnpressByState() {
        removeCallbacks(mLongPressRunnable); //移除长按逻辑的Runnable
        //根据当前状态处理
        switch (mState) {
            // 当前是点击按下
            case STATE_PRESS:
                if (mClickOrLongListener != null && (mButtonState == BUTTON_STATE_ONLY_CLICK || mButtonState ==
                        BUTTON_STATE_BOTH)) {
                    // 拍照
                    startCaptureAnimation(mButtonInsideRadius);
                } else {
                    mState = STATE_IDLE;
                }
                break;
            // 当前是长按状态
            case STATE_RECORDERING:
                mTimer.cancel(); //停止计时器
                recordEnd();    //录制结束
                break;
        }
    }

    /**
     * 录制结束
     */
    private void recordEnd() {
        if (mClickOrLongListener != null) {
            if (mRecordedTime < mMinDuration)
                mClickOrLongListener.onLongClickShort(mRecordedTime);//回调录制时间过短
            else
                mClickOrLongListener.onLongClickEnd(mRecordedTime);  //回调录制结束
        }
        resetRecordAnim();  //重置按钮状态
    }

    /**
     * 重置状态
     */
    private void resetRecordAnim() {
        mState = STATE_BAN;  // 重置状态
        mProgress = 0;       // 重置进度
        invalidate();
        //还原按钮初始状态动画
        startRecordAnimation(
                mButtonOutsideRadius,
                mButtonRadius,
                mButtonInsideRadius,
                mButtonRadius * 0.75f
        );
    }

    /**
     * 内圆动画
     *
     * @param inside_start 内圆半径
     */
    private void startCaptureAnimation(float inside_start) {
        ValueAnimator inside_anim = ValueAnimator.ofFloat(inside_start, inside_start * 0.75f, inside_start);
        inside_anim.addUpdateListener(animation -> {
            mButtonInsideRadius = (float) animation.getAnimatedValue();
            invalidate();
        });
        inside_anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // 回调拍照接口
                mClickOrLongListener.onClick();
                mState = STATE_BAN;
            }
        });
        inside_anim.setDuration(100);
        inside_anim.start();
    }

    /**
     * 内外圆同时发生的动画
     *
     * @param outsideStart 外圆半径
     * @param outsideEnd   变大后的外圆半径
     * @param insideStart  内圆半径
     * @param insideEnd    变小后的内圆半径
     */
    private void startRecordAnimation(float outsideStart, float outsideEnd, float insideStart, float insideEnd) {
        // 属性动画，从默认变大和从默认变小
        ValueAnimator outsideAnim = ValueAnimator.ofFloat(outsideStart, outsideEnd);
        ValueAnimator insideAnim = ValueAnimator.ofFloat(insideStart, insideEnd);
        // 外圆动画监听
        outsideAnim.addUpdateListener(animation -> {
            mButtonOutsideRadius = (float) animation.getAnimatedValue();
            invalidate();
        });
        // 内圆动画监听
        insideAnim.addUpdateListener(animation -> {
            mButtonInsideRadius = (float) animation.getAnimatedValue();
            invalidate();
        });
        AnimatorSet set = new AnimatorSet();
        // 当动画结束后启动录像Runnable并且回调录像开始接口
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //设置为录制状态
                if (mState == STATE_LONG_PRESS) {
                    if (mClickOrLongListener != null)
                        mClickOrLongListener.onLongClick();
                    mState = STATE_RECORDERING;
                    mTimer.start();
                }
            }
        });
        set.playTogether(outsideAnim, insideAnim);
        set.setDuration(100);
        set.start();
    }


    // region 对外方法

    /**
     * 设置最长录制时间
     *
     * @param duration 时间
     */
    public void setDuration(int duration) {
        mDuration = duration;
        mTimer = new RecordCountDownTimer(duration, duration / 360);    //录制定时器
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
     * 设置按钮功能（拍照和录像）
     *
     * @param buttonStateBoth {@link com.zhongjh.cameraviewsoundrecorder.camera.common.Constants#BUTTON_STATE_ONLY_CLICK 只能拍照
     * @link com.zhongjh.cameraviewsoundrecorder.camera.common.Constants#BUTTON_STATE_ONLY_LONGCLICK 只能录像
     * @link com.zhongjh.cameraviewsoundrecorder.camera.common.Constants#BUTTON_STATE_BOTH 两者皆可
     * }
     */
    public void setButtonFeatures(int buttonStateBoth) {
        this.mButtonState = buttonStateBoth;
    }

    /**
     * 重置状态
     */
    public void resetState() {
        mState = STATE_IDLE;
    }


    // endregion

}
