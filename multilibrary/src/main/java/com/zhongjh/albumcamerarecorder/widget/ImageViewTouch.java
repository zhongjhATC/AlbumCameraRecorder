package com.zhongjh.albumcamerarecorder.widget;

import android.content.Context;
import android.util.AttributeSet;

/**
 * 独立出来继承单纯是为了后期如果更换第三方库的时候，别人使用的话是不会也跟着修改
 * @author zhongjh
 * @date 2022/8/18
 */
public class ImageViewTouch extends it.sephiroth.android.library.imagezoom.ImageViewTouch {
    public ImageViewTouch(Context context) {
        super(context);
    }

    public ImageViewTouch(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageViewTouch(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
