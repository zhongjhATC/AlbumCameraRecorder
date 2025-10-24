package com.zhongjh.imageedit.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.RadioGroup

/**
 *
 * @author zhongjh
 * @date 2025/10/24
 */
class ImageColorGroup : RadioGroup {
    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    var checkColor: Int
        get() {
            val checkedId = checkedRadioButtonId
            val radio = findViewById<ImageColorRadio>(checkedId)
            if (radio != null) {
                return radio.color
            }
            return Color.WHITE
        }
        set(color) {
            val count = childCount
            for (i in 0 until count) {
                val radio = getChildAt(i) as ImageColorRadio
                if (radio.color == color) {
                    radio.isChecked = true
                    break
                }
            }
        }
}
