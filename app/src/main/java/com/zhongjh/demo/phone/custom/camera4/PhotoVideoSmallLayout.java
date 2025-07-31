package com.zhongjh.demo.phone.custom.camera4;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zhongjh.multimedia.camera.widget.PhotoVideoLayout;
import com.zhongjh.demo.R;

/**
 * 用于录制，拍摄的一系列控件按钮
 *
 * @author zhongjh
 * @date 2018/10/16
 */
public class PhotoVideoSmallLayout extends PhotoVideoLayout {

    public PhotoVideoSmallLayout(@NonNull Context context) {
        this(context, null);
    }

    public PhotoVideoSmallLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoVideoSmallLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @NonNull
    @Override
    public BaseViewHolder newViewHolder() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.layout_photovideo_operate_zjh_small, this, true);
        return new BaseViewHolder(view);
    }

}
