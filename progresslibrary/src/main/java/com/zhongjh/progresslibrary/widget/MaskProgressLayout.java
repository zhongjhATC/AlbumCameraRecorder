package com.zhongjh.progresslibrary.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.zhongjh.progresslibrary.R;

/**
 * 这是返回图片（视频、录音）等文件后，显示的Layout
 * Created by zhongjh on 2018/10/17.
 */
public class MaskProgressLayout extends FrameLayout {

    public ViewHolder mViewHolder; // 控件集合

    public MaskProgressLayout(@NonNull Context context) {
        this(context, null);
    }

    public MaskProgressLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaskProgressLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    /**
     * 初始化view
     */
    private void initView() {
        // 自定义View中如果重写了onDraw()即自定义了绘制，那么就应该在构造函数中调用view的setWillNotDraw(false).
        setWillNotDraw(false);

        mViewHolder = new ViewHolder(View.inflate(getContext(), R.layout.layout_mask_progress, this));
    }


    public static class ViewHolder {
        public View rootView;
        public RecyclerView rv_imagePicker;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.rv_imagePicker = rootView.findViewById(R.id.rv_imagePicker);
        }

    }
}
