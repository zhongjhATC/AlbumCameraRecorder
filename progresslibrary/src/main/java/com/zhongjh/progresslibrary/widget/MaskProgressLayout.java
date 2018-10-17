package com.zhongjh.progresslibrary.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.zhongjh.progresslibrary.R;
import com.zhongjh.progresslibrary.adapter.ImageAdapter;
import com.zhongjh.progresslibrary.engine.ImageEngine;

import java.util.ArrayList;
import java.util.List;

/**
 * 这是返回图片（视频、录音）等文件后，显示的Layout
 * Created by zhongjh on 2018/10/17.
 */
public class MaskProgressLayout extends FrameLayout {

    public ViewHolder mViewHolder;          // 控件集合
    private ImageAdapter mImageAdapter;     // 适配器
    private ArrayList<String> mImageList;   //当前选择的所有图片
    private ImageEngine mImageEngine;       // 图片加载方式

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

        // 初始化数据
        mImageList = new ArrayList<>();
        mImageAdapter = new ImageAdapter(this.getContext(), mImageList, 5, mImageEngine);
        mImageAdapter.setOnItemClickListener(this);

        imagePickereRV.setLayoutManager(new GridLayoutManager(this, 4));
        imagePickereRV.setHasFixedSize(true);
        imagePickereRV.setAdapter(adapter);
    }

    /**
     * 设置地址
     */
    public void setPath(List<String> photoAndVideo, String recording) {

    }


    public static class ViewHolder {
        public View rootView;
        public RecyclerView rvMedia;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.rvMedia = rootView.findViewById(R.id.rvMedia);
        }

    }
}
