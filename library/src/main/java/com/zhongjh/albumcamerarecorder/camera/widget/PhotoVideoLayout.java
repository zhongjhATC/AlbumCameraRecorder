package com.zhongjh.albumcamerarecorder.camera.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.widget.OperationLayout;

/**
 * 跟父类一样
 * Created by zhongjh on 2018/10/16.
 */
public class PhotoVideoLayout extends OperationLayout {

    public PhotoVideoLayout(@NonNull Context context) {
        super(context);
    }

    public PhotoVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public ViewHolder newViewHolder() {
        return new ViewHolder(View.inflate(getContext(), R.layout.layout_photovideo_operae, this));
    }

}
