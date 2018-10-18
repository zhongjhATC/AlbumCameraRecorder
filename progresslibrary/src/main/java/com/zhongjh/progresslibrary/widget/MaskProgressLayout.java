package com.zhongjh.progresslibrary.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
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
import com.zhongjh.progresslibrary.listener.MaskProgressLayoutListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 这是返回图片（视频、录音）等文件后，显示的Layout
 * Created by zhongjh on 2018/10/17.
 */
public class MaskProgressLayout extends FrameLayout implements ImageAdapter.OnRecyclerViewItemClickListener {

    public ViewHolder mViewHolder;          // 控件集合
    private ImageAdapter mImageAdapter;     // 适配器
    private ArrayList<String> mImageList;   //当前选择的所有图片
    private ImageEngine mImageEngine;       // 图片加载方式

    private MaskProgressLayoutListener listener;   // 点击事件

    public void setOnRecyclerViewItemClickListener(MaskProgressLayoutListener listener) {
        this.listener = listener;
    }


    public MaskProgressLayout(@NonNull Context context) {
        this(context, null);
    }

    public MaskProgressLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaskProgressLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }

    /**
     * 初始化view
     */
    private void initView(AttributeSet attrs) {
        // 自定义View中如果重写了onDraw()即自定义了绘制，那么就应该在构造函数中调用view的setWillNotDraw(false).
        setWillNotDraw(false);

        mViewHolder = new ViewHolder(View.inflate(getContext(), R.layout.layout_mask_progress, this));

        // 初始化数据
        mImageList = new ArrayList<>();

        //获取自定义属性。
        TypedArray maskProgressLayoutStyle = getContext().obtainStyledAttributes(attrs, R.styleable.MaskProgressLayoutStyle);
        // 获取默认图片
        Drawable drawable = maskProgressLayoutStyle.getDrawable(R.styleable.MaskProgressLayoutStyle_album_thumbnail_placeholder);

        if (drawable == null){
            drawable =
        }

        mImageAdapter = new ImageAdapter(this.getContext(), mImageList, 5, mImageEngine, drawable);
        mImageAdapter.setOnRecyclerViewItemClickListener(this);

        mViewHolder.rvMedia.setLayoutManager(new GridLayoutManager(this.getContext(), 4));
        mViewHolder.rvMedia.setHasFixedSize(true);
        mViewHolder.rvMedia.setAdapter(mImageAdapter);
    }

    /**
     * 外放API初始化
     *
     * @param imageEngine 显示图片方式
     */
    public void init(ImageEngine imageEngine) {
        mImageEngine = imageEngine;
    }

    /**
     * 设置图片、视频地址同时更新表格
     */
    public void setPath(List<String> photoAndVideo, String recording) {
        mImageList.addAll(photoAndVideo);
        mImageAdapter.setImages(mImageList);
    }

    @Override
    public void onItemClick(View view, int position) {
        // 点击事件
        mImageAdapter.setOnRecyclerViewItemClickListener(new ImageAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                listener.onItemClick(view, position);
            }
        });
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
