package com.zhongjh.albumcamerarecorder.album.widget;

import android.content.Context;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.widget.AppCompatImageView;

import android.os.Build;
import android.util.AttributeSet;

import com.zhongjh.albumcamerarecorder.R;

/**
 * 单选框
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mDrawable.setColorFilter(new BlendModeColorFilter(mSelectedColor, BlendMode.SRC_IN));
            } else {
                mDrawable.setColorFilter(mSelectedColor, PorterDuff.Mode.SRC_IN);
            }
        } else {
            setImageResource(R.drawable.ic_radio_button_unchecked_white_24dp);
            mDrawable = getDrawable();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mDrawable.setColorFilter(new BlendModeColorFilter(mUnSelectUdColor, BlendMode.SRC_IN));
            } else {
                mDrawable.setColorFilter(mUnSelectUdColor, PorterDuff.Mode.SRC_IN);
            }
        }
    }


    public void setColor(int color) {
        if (mDrawable == null) {
            mDrawable = getDrawable();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mDrawable.setColorFilter(new BlendModeColorFilter(color, BlendMode.SRC_IN));
        } else {
            mDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
    }
}
