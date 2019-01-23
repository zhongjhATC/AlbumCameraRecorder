package com.zhongjh.albumcamerarecorder.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 可以控制是否能滑动的 ViewPager
 * Created by zhongjh on 2018/10/10.
 */
public class NoScrollViewPager extends ViewPager {

    private boolean isScroll = true;

    public NoScrollViewPager(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public NoScrollViewPager(Context context) {
        super(context);
    }

    /**
     * 1.dispatchTouchEvent一般情况不做处理
     *,如果修改了默认的返回值,子孩子都无法收到事件
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);   // return true;不行
    }
    /**
     * 是否拦截
     * 拦截:会走到自己的onTouchEvent方法里面来
     * 不拦截:事件传递给子孩子
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // return false;//可行,不拦截事件,
        // return true;//不行,孩子无法处理事件
        //return super.onInterceptTouchEvent(ev);//不行,会有细微移动
        if (isScroll){
            return super.onInterceptTouchEvent(ev);
        }else{
            return false;
        }
    }
    /**
     * 是否消费事件
     * 消费:事件就结束
     * 不消费:往父控件传
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //return false;// 可行,不消费,传给父控件
        //return true;// 可行,消费,拦截事件
        //super.onTouchEvent(ev); //不行,
        //虽然onInterceptTouchEvent中拦截了,
        //但是如果viewpage里面子控件不是viewgroup,还是会调用这个方法.
        if (isScroll){
            return super.onTouchEvent(ev);
        }else {
            return true;// 可行,消费,拦截事件
        }
    }

    /**
     * 设置是否能滑动
     * @param scroll 是否滑动
     */
    public void setScroll(boolean scroll) {
        isScroll = scroll;
    }


}
