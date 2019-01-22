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
import com.zhongjh.progresslibrary.entity.MultiMedia;
import com.zhongjh.progresslibrary.listener.MaskProgressLayoutListener;

import java.util.List;

/**
 * 这是返回图片（视频、录音）等文件后，显示的Layout
 * Created by zhongjh on 2018/10/17.
 */
public class MaskProgressLayout extends FrameLayout implements MaskProgressLayoutListener {

    public ViewHolder mViewHolder;          // 控件集合
    private ImageAdapter mImageAdapter;     // 适配器
    private ImageEngine mImageEngine;       // 图片加载方式

    private MaskProgressLayoutListener listener;   // 点击事件

    public void setMaskProgressLayoutListener(MaskProgressLayoutListener listener) {
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

        // 获取自定义属性。
        TypedArray maskProgressLayoutStyle = getContext().obtainStyledAttributes(attrs, R.styleable.MaskProgressLayoutStyle);
        // 获取默认图片
        Drawable drawable = maskProgressLayoutStyle.getDrawable(R.styleable.MaskProgressLayoutStyle_album_thumbnail_placeholder);
        // 获取显示图片的类
        String imageEngineStr = maskProgressLayoutStyle.getString(R.styleable.MaskProgressLayoutStyle_image_engine);
        // 获取最多显示多少个图片
        int imageCount = maskProgressLayoutStyle.getInteger(R.styleable.MaskProgressLayoutStyle_image_count, 5);
        if (imageEngineStr == null) {
            throw new RuntimeException("必须定义image_engine属性，指定某个显示图片类");
        } else {
            Class<?> imageEngineClass;//完整类名
            try {
                imageEngineClass = Class.forName(imageEngineStr);
                mImageEngine = (ImageEngine) imageEngineClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (drawable == null) {
            drawable = getResources().getDrawable(R.color.thumbnail_placeholder);
        }

        mImageAdapter = new ImageAdapter(this.getContext(), imageCount, mImageEngine, drawable);
        mImageAdapter.setMaskProgressLayoutListener(this);
        mViewHolder.rvMedia.setLayoutManager(new GridLayoutManager(this.getContext(), 4));
        mViewHolder.rvMedia.setHasFixedSize(true);
        mViewHolder.rvMedia.setAdapter(mImageAdapter);
    }

    /**
     * 设置视频地址
     */
    public MultiMedia setVideo(List<String> videoPath) {
        return mImageAdapter.addVideo(videoPath.get(0));
    }

    /**
     * 设置图片同时更新表格
     *
     * @param imagePaths 图片数据源
     * @param recording
     */
    public List<MultiMedia> addImages(List<String> imagePaths, String recording) {
        return mImageAdapter.addImages(imagePaths);
    }

    /**
     * 设置视频进度展示
     *
     * @param position   图片的索引
     * @param percentage 百分比值，1=1%
     * @return 本身控件
     */
    public ImageAdapter.ViewHolder setVideoProgress(int position, int percentage) {
        ImageAdapter.ViewHolder viewHolder = ((ImageAdapter.ViewHolder) (mViewHolder.rvMedia.findViewHolderForAdapterPosition(position)));
        if (viewHolder != null)
            viewHolder.mpvImage.setPercentage(percentage);
        return viewHolder;
    }

    /**
     * 设置图片进度展示
     *
     * @param position   图片的索引
     * @param percentage 百分比值，1=1%
     * @return 本身控件
     */
    public ImageAdapter.ViewHolder setImageProgress(int position, int percentage) {
        // 判断有没有视频
        if (mImageAdapter.isExistingVideo)
            position++;
        ImageAdapter.ViewHolder viewHolder = ((ImageAdapter.ViewHolder) (mViewHolder.rvMedia.findViewHolderForAdapterPosition(position)));
        if (viewHolder != null)
            viewHolder.mpvImage.setPercentage(percentage);
        return viewHolder;
    }

    @Override
    public void onItemAdd(View view, int position, int alreadyImageCount, int alreadyVideoCount) {
        listener.onItemAdd(view, position, alreadyImageCount, alreadyVideoCount);
    }

    @Override
    public void onItemImage(View view, int position) {
        listener.onItemImage(view, position);
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
