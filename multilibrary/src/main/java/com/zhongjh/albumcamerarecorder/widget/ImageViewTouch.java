package com.zhongjh.albumcamerarecorder.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.github.chrisbanes.photoview.PhotoView;

/**
 * 独立出来继承单纯是为了后期如果更换第三方库的时候，别人使用的话是不会也跟着修改
 * @author zhongjh
 * @date 2022/8/18
 */
public class ImageViewTouch extends PhotoView {
    public ImageViewTouch(Context context) {
        super(context);
    }

    public ImageViewTouch(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public ImageViewTouch(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
    }
}
