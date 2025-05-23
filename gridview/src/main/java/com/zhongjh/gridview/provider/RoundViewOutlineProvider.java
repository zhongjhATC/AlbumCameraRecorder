package com.zhongjh.gridview.provider;

import android.graphics.Outline;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewOutlineProvider;

/**
 * 为了给imageView设置圆角
 */
public class RoundViewOutlineProvider extends ViewOutlineProvider {
    private final float radius;
    public RoundViewOutlineProvider(float radius) {
        this.radius = radius;
    }

    @Override
    public void getOutline(View view, Outline outline) {
        int leftMargin = 0;
        int topMargin = 0;
        Rect selfRect = new Rect(leftMargin, topMargin, view.getWidth(), view.getHeight());
        outline.setRoundRect(selfRect, radius);
    }
}
