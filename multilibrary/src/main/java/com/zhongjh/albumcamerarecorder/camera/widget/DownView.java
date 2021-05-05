package com.zhongjh.albumcamerarecorder.camera.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/**
 * 一个类似三角形的向下控件
 *
 * @author zhongjh
 * @date 2018/8/7
 */
public class DownView extends View {

    private int mSize;
    private int mCenterX;
    private int mCenterY;
    /**
     * 线
     */
    private float mStrokeWidth;
    /**
     * 画笔
     */
    private Paint mPaint;
    private Path mPath;


    public DownView(Context context) {
        super(context);
    }

    public DownView(Context context, int size) {
        this(context);
        this.mSize = size;
        mCenterX = size / 2;
        mCenterY = size / 2;

        mStrokeWidth = size / 15f;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);

        mPath = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mSize, mSize / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPath.moveTo(mStrokeWidth, mStrokeWidth / 2);
        mPath.lineTo(mCenterX, mCenterY - mStrokeWidth / 2);
        mPath.lineTo(mSize - mStrokeWidth, mStrokeWidth / 2);
        canvas.drawPath(mPath, mPaint);
    }

}
