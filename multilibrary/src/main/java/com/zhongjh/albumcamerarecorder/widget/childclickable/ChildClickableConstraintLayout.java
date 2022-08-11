package com.zhongjh.albumcamerarecorder.widget.childclickable;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * 是否允许子控件可以点击
 * 继承于[ConstraintLayout]
 * @author zhongjh
 * @date 2018/12/27
 */
public class ChildClickableConstraintLayout extends ConstraintLayout implements IChildClickableLayout {

    /**
     * 子控件是否可以接受点击事件
     */
    private boolean childClickable = true;

    public ChildClickableConstraintLayout(Context context) {
        super(context);
    }

    public ChildClickableConstraintLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChildClickableConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // 返回true则拦截子控件所有点击事件，如果childClickable为true，则需返回false
        return !childClickable;
    }
    
    @Override
    public void setChildClickable(boolean clickable) {
        childClickable = clickable;
    }

}
