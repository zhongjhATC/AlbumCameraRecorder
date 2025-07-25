package com.zhongjh.multimedia.camera.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zhongjh.multimedia.R;
import com.zhongjh.multimedia.widget.BaseOperationLayout;

/**
 * 用于录制，拍摄的一系列控件按钮
 *
 * @author zhongjh
 * @date 2018/10/16
 */
public class PhotoVideoLayout extends BaseOperationLayout {

    public ViewHolder getViewHolder() {
        return (ViewHolder) viewHolder;
    }

    public PhotoVideoLayout(@NonNull Context context) {
        this(context, null);
    }

    public PhotoVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public ViewHolder newViewHolder() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.layout_photovideo_operate_zjh, this, true);
        return new ViewHolder(view);
    }

    @Override
    protected void initListener() {
        super.initListener();
    }

    public static class ViewHolder extends BaseOperationLayout.ViewHolder {

        public final RelativeLayout rlEdit;

        public ViewHolder(View rootView) {
            super(rootView);
            this.rlEdit = rootView.findViewById(R.id.rlEdit);
        }

    }

}
