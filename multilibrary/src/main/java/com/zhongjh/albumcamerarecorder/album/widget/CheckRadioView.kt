package com.zhongjh.albumcamerarecorder.album.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import com.zhongjh.albumcamerarecorder.R
import com.zhongjh.common.utils.ColorFilterUtil.setColorFilterSrcIn

/**
 * 单选框
 * @author zhongjh
 */
class CheckRadioView : AppCompatImageView {

    private var mSelectedColor = 0
    private var mUnSelectUdColor = 0

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    private fun init() {
        mSelectedColor = ResourcesCompat.getColor(
            resources, R.color.blue_item_checkCircle_backgroundColor,
            context.theme
        )
        mUnSelectUdColor = ResourcesCompat.getColor(
            resources, R.color.blue_check_original_radio_disable,
            context.theme
        )
        setChecked(false)
    }

    fun setChecked(enable: Boolean) {
        if (enable) {
            setImageResource(R.drawable.ic_radio_button_checked_white_24dp)
            setColorFilterSrcIn(drawable, mSelectedColor)
        } else {
            setImageResource(R.drawable.ic_radio_button_unchecked_white_24dp)
            setColorFilterSrcIn(drawable, mUnSelectUdColor)
        }
    }

    fun setColor(color: Int) {
        setColorFilterSrcIn(drawable, color)
    }
}