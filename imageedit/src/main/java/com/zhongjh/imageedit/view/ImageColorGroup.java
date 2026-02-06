package com.zhongjh.imageedit.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.RadioGroup;

/**
 *
 * @author felix
 * @date 2017/12/1 下午3:07
 */

public class ImageColorGroup extends RadioGroup {

    public ImageColorGroup(Context context) {
        super(context);
    }

    public ImageColorGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public int getCheckColor() {
        int checkedId = getCheckedRadioButtonId();
        ImageColorRadio radio = findViewById(checkedId);
        if (radio != null) {
            return radio.getColor();
        }
        return Color.WHITE;
    }

    public void setCheckColor(int color) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            ImageColorRadio radio = (ImageColorRadio) getChildAt(i);
            if (radio.getColor() == color) {
                radio.setChecked(true);
                break;
            }
        }
    }
}
