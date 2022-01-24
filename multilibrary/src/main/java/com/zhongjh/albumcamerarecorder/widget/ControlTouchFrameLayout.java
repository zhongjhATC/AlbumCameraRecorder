package com.zhongjh.albumcamerarecorder.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

/**
 * 控制不能触发子View的FrameLayout
 *
 * @author zhongjh
 * @date 2022/1/21
 */
public class ControlTouchFrameLayout extends FrameLayout {

    public ControlTouchFrameLayout(Context context) {
        this(context, null);
    }

    public ControlTouchFrameLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ControlTouchFrameLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ControlTouchFrameLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private boolean enabled = true;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (enabled) {
            return super.dispatchTouchEvent(ev);
        } else {
            return true;
        }
    }
}
