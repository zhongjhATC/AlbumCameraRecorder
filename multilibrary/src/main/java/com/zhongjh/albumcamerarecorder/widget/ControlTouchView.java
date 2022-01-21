package com.zhongjh.albumcamerarecorder.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * @author 8
 * @date 2022/1/21
 */
public class ControlTouchView extends View {
    public ControlTouchView(Context context) {
        super(context);
    }

    public ControlTouchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ControlTouchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ControlTouchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }
}
