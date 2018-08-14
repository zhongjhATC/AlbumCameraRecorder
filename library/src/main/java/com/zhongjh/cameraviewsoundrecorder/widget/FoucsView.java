package com.zhongjh.cameraviewsoundrecorder.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.zhongjh.cameraviewsoundrecorder.util.DisplayMetricsSPUtils;

/**
 * 摄像对焦框
 * Created by zhongjh on 2018/8/10.
 */
public class FoucsView extends View {

    private int mSize;
    private int mCenterX;
    private int mCenterY;
    private int mLength;
    private Paint mPaint;

    public FoucsView(Context context) {
        super(context);
    }

    public FoucsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FoucsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mSize = DisplayMetricsSPUtils.getScreenWidth(context) / 3;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        // 绿色的焦点
        mPaint.setColor(0xEE16AE16);
        mPaint.setStrokeWidth(4);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mCenterX = (int) (mSize / 2.0);
        mCenterY = (int) (mSize / 2.0);
        mLength = (int) (mSize / 2.0) - 2;
        // 决定当前View的大小
        setMeasuredDimension(mSize, mSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(mCenterX - mLength, mCenterY - mLength, mCenterX + mLength, mCenterY + mLength, mPaint);
        canvas.drawLine(2, getHeight() / 2, mSize / 10, getHeight() / 2, mPaint);
        canvas.drawLine(getWidth() - 2, getHeight() / 2, getWidth() - mSize / 10, getHeight() / 2, mPaint);
        canvas.drawLine(getWidth() / 2, 2, getWidth() / 2, mSize / 10, mPaint);
        canvas.drawLine(getWidth() / 2, getHeight() - 2, getWidth() / 2, getHeight() - mSize / 10, mPaint);
    }


}
