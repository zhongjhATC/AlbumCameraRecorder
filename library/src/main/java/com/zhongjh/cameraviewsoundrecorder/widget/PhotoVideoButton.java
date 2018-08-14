package com.zhongjh.cameraviewsoundrecorder.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;

import com.zhongjh.cameraviewsoundrecorder.listener.PhotoVideoListener;
import com.zhongjh.cameraviewsoundrecorder.util.PermissionUtil;

import static com.zhongjh.cameraviewsoundrecorder.camera.CameraLayout.BUTTON_STATE_BOTH;
import static com.zhongjh.cameraviewsoundrecorder.camera.CameraLayout.BUTTON_STATE_ONLY_RECORDER;

/**
 * 动作按钮：拍照，录像，录音
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
    private float mCenterX;
    private float mCenterY;

    // 画笔
    private Paint mPaint;

    private RectF mRectF;

    private LongPressRunnable mLongPressRunnable;        //长按后处理的逻辑Runnable
    private PhotoVideoListener mPhotoVideoListener;       //按钮回调接口
    private RecordCountDownTimer mTimer;                //计时器

    public PhotoVideoButton(Context context) {
        super(context);
    }

    public PhotoVideoButton(Context context, int size) {
        super(context);
        this.mButtonSize = size;
        this.mButtonRadius = size / 2.0f; // 计算半径
        mButtonOutsideRadius = mButtonRadius; // 外圆半径
        mButtonInsideRadius = mButtonRadius * 0.75f; // 内圆半径
        mStrokeWidth = size / 15;       // 线条占据直径的1/15
        mOutsideAddSize = size / 5;     // 长按外圆半径变大的Size
        mInsideReduceSize = size / 8;   // 长按内圆缩小的Size

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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 计算大小
        setMeasuredDimension(mButtonSize + mOutsideAddSize * 2, mButtonSize + mOutsideAddSize * 2);
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
                // 按下触发事件,必须当前状态是空闲状态
                if (event.getPointerCount() > 1 || mState != STATE_IDLE)
                    break;
                event_Y = event.getY();      //记录Y值
                mState = STATE_PRESS;        //修改当前状态为点击按下

                // 判断按钮状态是否为可录制状态
                if ((mButtonState == BUTTON_STATE_ONLY_RECORDER || mButtonState == BUTTON_STATE_BOTH))
                    postDelayed(mLongPressRunnable, 500);    //同时延长500启动长按后处理的逻辑Runnable
                break;
            case MotionEvent.ACTION_MOVE:

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
            mState = STATE_LONG_PRESS;
            //没有录制权限
            if (PermissionUtil.getRecordState() != PermissionUtil.STATE_SUCCESS) {
                mState = STATE_IDLE;
                if (mPhotoVideoListener != null) {
                    mPhotoVideoListener.recordError();
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
     * 录制结束
     */
    private void recordEnd() {
        if (mPhotoVideoListener != null) {
            if (mRecordedTime < mMinDuration)
                mPhotoVideoListener.recordShort(mRecordedTime);//回调录制时间过短
            else
                mPhotoVideoListener.recordEnd(mRecordedTime);  //回调录制结束
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
        outsideAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mButtonOutsideRadius = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        // 内圆动画监听
        insideAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mButtonInsideRadius = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        AnimatorSet set = new AnimatorSet();
        // 当动画结束后启动录像Runnable并且回调录像开始接口
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //设置为录制状态
                if (mState == STATE_LONG_PRESS) {
                    if (mPhotoVideoListener != null)
                        mPhotoVideoListener.recordStart();
                    mState = STATE_RECORDERING;
                    mTimer.start();
                }
            }
        });
    }


    // region 对外方法

    /**
     * 设置最长录制时间
     * @param duration 时间
     */
    public void setDuration(int duration){
        mDuration = duration;
        mTimer = new RecordCountDownTimer(duration, duration / 360);    //录制定时器
    }

    /**
     * 设置回调接口
     * @param photoVideoListener 回调接口
     */
    public void setRecordingListener(PhotoVideoListener photoVideoListener) {
        this.mPhotoVideoListener = photoVideoListener;
    }

    // endregion

}
