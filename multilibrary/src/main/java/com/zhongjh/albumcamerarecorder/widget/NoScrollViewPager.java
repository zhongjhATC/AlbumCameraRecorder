package com.zhongjh.albumcamerarecorder.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 可以控制是否能滑动的 ViewPager
 * Created by zhongjh on 2018/10/10.
 */
public class NoScrollViewPager extends ViewPager {

    private boolean isScroll = true;

    public NoScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoScrollViewPager(Context context) {
        super(context);
    }

    /**
     * 是否拦截
     * 拦截:会走到自己的onTouchEvent方法里面来
     * 不拦截:事件传递给子孩子
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        return isScroll && super.onInterceptTouchEvent(ev);
        return false;
    }

    /**
     * 是否消费事件
     * 消费:事件就结束
     * 不消费:往父控件传
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
//        if (isScroll){
//            return super.onTouchEvent(ev);
//        }else {
        // 禁止滑动
        return true;
//        }
    }

    /**
     * 设置是否能滑动
     *
     * @param scroll 是否滑动
     */
    public void setScroll(boolean scroll) {
        isScroll = scroll;
    }


}
