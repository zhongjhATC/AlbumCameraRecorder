package com.zhongjh.albumcamerarecorder.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.zhongjh.common.utils.StatusBarUtils;

/**
 * 自定义的一个Behavior，滑动控件时自动隐藏底部控件
 * 为了性能效率考虑，只服务于FrameLayoutBehavior
 *
 * @author zhongjh
 * @date 2022/8/10
 */
public class BottomBarBehavior extends CoordinatorLayout.Behavior<ConstraintLayoutBehavior> {

    /**
     * 状态栏高度
     */
    int statusBarHeight;

    public BottomBarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        statusBarHeight = StatusBarUtils.getStatusBarHeight(context);
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull ConstraintLayoutBehavior child, @NonNull View dependency) {
        // 说明子控件依赖AppBarLayout
        return dependency instanceof AppBarLayout;
    }


    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull ConstraintLayoutBehavior child, @NonNull View dependency) {
        // 顶部的AppBarLayout是paddingTop状态栏高度的
        child.setTranslationY(Math.abs(dependency.getTop() - statusBarHeight));
        if (child.getOnListener() != null) {
            child.getOnListener().onDependentViewChanged(Math.abs(dependency.getTop() - statusBarHeight));
        }
        return true;
    }

}
