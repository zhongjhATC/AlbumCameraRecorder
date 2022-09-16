package com.zhongjh.imageedit.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.appcompat.widget.AppCompatRadioButton;

import com.zhongjh.imageedit.R;

/**
 *
 * @author felix
 * @date 2017/12/1 下午2:50
 */

public class ImageColorRadio extends AppCompatRadioButton implements ValueAnimator.AnimatorUpdateListener {

    private int mColor = Color.WHITE;

    private int mStrokeColor = Color.WHITE;

    private float mRadiusRatio = 0f;

    private ValueAnimator mAnimator;

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final float RADIUS_BASE = 0.6f;

    private static final float RADIUS_RING = 0.9f;

    private static final float RADIUS_BALL = 0.72f;

    public ImageColorRadio(Context context) {
        this(context, null, 0);
    }

    public ImageColorRadio(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public ImageColorRadio(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs);
    }

    private void initialize(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ZIMGColorRadio);

        mColor = a.getColor(R.styleable.ZIMGColorRadio_z_image_color, Color.WHITE);
        mStrokeColor = a.getColor(R.styleable.ZIMGColorRadio_z_image_stroke_color, Color.WHITE);

        a.recycle();

        setButtonDrawable(null);

        mPaint.setColor(mColor);
        mPaint.setStrokeWidth(5f);
    }

    private ValueAnimator getAnimator() {
        if (mAnimator == null) {
            mAnimator = ValueAnimator.ofFloat(0f, 1f);
            mAnimator.addUpdateListener(this);
            mAnimator.setDuration(200);
            mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        }
        return mAnimator;
    }

    public void setColor(int color) {
        mColor = color;
        mPaint.setColor(mColor);
    }

    public int getColor() {
        return mColor;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        float hw = getWidth() / 2f, hh = getHeight() / 2f;
        float radius = Math.min(hw, hh);

        canvas.save();
        mPaint.setColor(mColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(hw, hh, getBallRadius(radius), mPaint);

        mPaint.setColor(mStrokeColor);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(hw, hh, getRingRadius(radius), mPaint);
        canvas.restore();
    }

    private float getBallRadius(float radius) {
        return radius * ((RADIUS_BALL - RADIUS_BASE) * mRadiusRatio + RADIUS_BASE);
    }

    private float getRingRadius(float radius) {
        return radius * ((RADIUS_RING - RADIUS_BASE) * mRadiusRatio + RADIUS_BASE);
    }

    @Override
    public void setChecked(boolean checked) {
        boolean isChanged = checked != isChecked();

        super.setChecked(checked);

        if (isChanged) {
            ValueAnimator animator = getAnimator();

            if (checked) {
                animator.start();
            } else {
                animator.reverse();
            }
        }
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        mRadiusRatio = (float) animation.getAnimatedValue();
        invalidate();
    }
}
