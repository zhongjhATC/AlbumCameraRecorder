package com.zhongjh.albumcamerarecorder.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.zhongjh.albumcamerarecorder.R;

/**
 * 操作按钮：目前仅仅是点击或长按完成后弹出的确认和返回按钮
 * Created by zhongjh on 2018/8/7.
 */
public class OperationButton extends View {

    private int mType;

    private Paint mPaint;
    private Path mPath;
    private float mStrokeWidth;

    private int mButtonType; // 当前按钮类型
    private int mButtonSize; // 当前按钮大小
    private float mButtonRadius; // 当前按钮半径

    private float mCenterX;// X坐标
    private float mCenterY; // Y坐标

    private float mIndex;
    private RectF mRectF;


    public OperationButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public OperationButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 获取属性
        TypedArray operaeButtonArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.OperationButton, defStyleAttr, 0);
        // 获取类型
        mType = operaeButtonArray.getInt(R.styleable.OperationButton_type,0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        init(mType,getMeasuredWidth());
        setMeasuredDimension(mButtonSize, mButtonSize);
    }

    public void init( int type, int size) {
        this.mButtonType = type;
        this.mButtonSize = size;
        this.mButtonRadius = size / 2.0f;

        this.mCenterX = size / 2.0f;
        this.mCenterY = size / 2.0f;

        mPaint = new Paint();
        mPath = new Path();
        mStrokeWidth = size / 50f;
        mIndex = mButtonSize / 12f;
        mRectF = new RectF(mCenterX, mCenterY - mIndex, mCenterX + mIndex * 2, mCenterY + mIndex);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        switch (mButtonType){
            case 1:
                mPaint.setAntiAlias(true);
                mPaint.setColor(0xFFFFFFFF);
                mPaint.setStyle(Paint.Style.FILL);
                // 画圆
                canvas.drawCircle(mCenterX, mCenterY, mButtonRadius, mPaint);
                mPaint.setAntiAlias(true);
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setColor(0xFF00CC00);
                mPaint.setStrokeWidth(mStrokeWidth);

                mPath.moveTo(mCenterX - mButtonSize / 6f, mCenterY);
                mPath.lineTo(mCenterX - mButtonSize / 21.2f, mCenterY + mButtonSize / 7.7f);
                mPath.lineTo(mCenterX + mButtonSize / 4.0f, mCenterY - mButtonSize / 8.5f);
                mPath.lineTo(mCenterX - mButtonSize / 21.2f, mCenterY + mButtonSize / 9.4f);
                mPath.close();
                canvas.drawPath(mPath, mPaint);
                break;
            case 0:
                mPaint.setAntiAlias(true);
                mPaint.setColor(0xEEDCDCDC);
                mPaint.setStyle(Paint.Style.FILL);
                // 画圆
                canvas.drawCircle(mCenterX, mCenterY, mButtonRadius, mPaint);

                mPaint.setColor(Color.BLACK);
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(mStrokeWidth);

                mPath.moveTo(mCenterX - mIndex / 7, mCenterY + mIndex);
                mPath.lineTo(mCenterX + mIndex, mCenterY + mIndex);

                mPath.arcTo(mRectF, 90, -180);
                mPath.lineTo(mCenterX - mIndex, mCenterY - mIndex);
                canvas.drawPath(mPath, mPaint);
                mPaint.setStyle(Paint.Style.FILL);
                mPath.reset();
                mPath.moveTo(mCenterX - mIndex, (float) (mCenterY - mIndex * 1.5));
                mPath.lineTo(mCenterX - mIndex, (float) (mCenterY - mIndex / 2.3));
                mPath.lineTo((float) (mCenterX - mIndex * 1.6), mCenterY - mIndex);
                mPath.close();
                canvas.drawPath(mPath, mPaint);
                break;
        }
    }

}
