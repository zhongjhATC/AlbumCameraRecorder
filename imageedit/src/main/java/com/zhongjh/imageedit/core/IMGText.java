package com.zhongjh.imageedit.core;

import android.graphics.Color;
import android.text.TextUtils;

/**
 * Created by felix on 2017/12/1 下午2:43.
 */

public class IMGText {

    private String text;

    private int color = Color.WHITE;

    public IMGText(String text, int color) {
        this.text = text;
        this.color = color;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isEmpty() {
        return TextUtils.isEmpty(text);
    }

    public int length() {
        return isEmpty() ? 0 : text.length();
    }

    @Override
    public String toString() {
        return "IMGText{" +
                "text='" + text + '\'' +
                ", color=" + color +
                '}';
    }
}
