package com.zhongjh.albumcamerarecorder.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

/**
 * 用于专门服务于app:layout_behavior的，通过自定义view的自定义事件，进行联动
 *
 * @author zhongjh
 * @date 2022/8/11
 */
public class FrameLayoutBehavior extends FrameLayout {

    public FrameLayoutBehavior(Context context) {
        this(context, null);
    }

    public FrameLayoutBehavior(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FrameLayoutBehavior(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public FrameLayoutBehavior(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public Listener getOnListener() {
        return mOnListener;
    }

    public void setOnListener(Listener mOnListener) {
        this.mOnListener = mOnListener;
    }

    /**
     * 点击或长按监听结束后的 确认取消事件监控
     */
    private Listener mOnListener;

    /**
     * 操作按钮的Listener
     */
    public interface Listener {

        /**
         * 触发滑动事件
         */
        void onDependentViewChanged(float translationY);
    }

}
