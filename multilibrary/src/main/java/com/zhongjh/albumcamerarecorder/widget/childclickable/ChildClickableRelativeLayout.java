package com.zhongjh.albumcamerarecorder.widget.childclickable;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * 是否允许子控件可以点击
 * 继承于[RelativeLayout]
 * @author zhongjh
 * @date 2018/12/27
 */
public class ChildClickableRelativeLayout extends RelativeLayout implements IChildClickableLayout {

    /**
     * 子控件是否可以接受点击事件
     */
    private boolean childClickable = true;

    public ChildClickableRelativeLayout(Context context) {
        super(context);
    }

    public ChildClickableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChildClickableRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
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
