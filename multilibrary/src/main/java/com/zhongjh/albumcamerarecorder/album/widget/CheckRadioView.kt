package com.zhongjh.albumcamerarecorder.album.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.res.ResourcesCompat;

import com.zhongjh.albumcamerarecorder.R;

import com.zhongjh.common.utils.ColorFilterUtil;

/**
 * 单选框
 * @author zhongjh
 */
public class CheckRadioView extends AppCompatImageView {

    private Drawable mDrawable;

    private int mSelectedColor;
    private int mUnSelectUdColor;

    public CheckRadioView(Context context) {
        super(context);
        init();
    }



    public CheckRadioView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mSelectedColor = ResourcesCompat.getColor(
                getResources(), R.color.blue_item_checkCircle_backgroundColor,
                getContext().getTheme());
        mUnSelectUdColor = ResourcesCompat.getColor(
                getResources(), R.color.blue_check_original_radio_disable,
                getContext().getTheme());
        setChecked(false);
    }

    public void setChecked(boolean enable) {
        if (enable) {
            setImageResource(R.drawable.ic_radio_button_checked_white_24dp);
            mDrawable = getDrawable();
            ColorFilterUtil.setColorFilterSrcIn(mDrawable,mSelectedColor);
        } else {
            setImageResource(R.drawable.ic_radio_button_unchecked_white_24dp);
            mDrawable = getDrawable();
            ColorFilterUtil.setColorFilterSrcIn(mDrawable,mUnSelectUdColor);
        }
    }


    public void setColor(int color) {
        if (mDrawable == null) {
            mDrawable = getDrawable();
        }
        ColorFilterUtil.setColorFilterSrcIn(mDrawable,color);
    }
}
