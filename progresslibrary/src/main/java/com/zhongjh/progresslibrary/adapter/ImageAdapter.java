package com.zhongjh.progresslibrary.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhongjh.progresslibrary.R;
import com.zhongjh.progresslibrary.engine.ImageEngine;
import com.zhongjh.progresslibrary.widget.MaskProgressView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhongjh on 2018/10/17.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<String> mData;
    private int maxMediaCount;  // 设置最多显示多少个图片或者视频
    private boolean isLast;   // 是否最后一个图片
    private ImageEngine mImageEngine;   // 图片加载方式
    private final Drawable mPlaceholder; // 默认图片
    private OnRecyclerViewItemClickListener listener;   // 点击事件

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * 设置数据
     *
     * @param data 数据源
     */
    public void setImages(List<String> data) {
        mData = new ArrayList<>(data);
        if (getItemCount() < maxMediaCount) {
            // 未满上限数量可以继续设置
            mData.add(null);
            isLast = false;
        } else {
            isLast = true;
        }
        notifyDataSetChanged();
    }

    /**
     * 由于 图片/视频 未选满时，最后一张显示添加 图片/视频 ，因此这个方法返回真正的已选 图片/视频
     *
     * @return 返回所有地址
     */
    public List<String> getImages() {
        if (!isLast)
            return new ArrayList<>(mData.subList(0, mData.size() - 1));
        else
            return mData;
    }

    /**
     * 设置百分比
     * @param percentage 百分比值，1=1%
     */
    public void setPercentage(ViewHolder viewHolder,int percentage){
        viewHolder.mpvImage.setPercentage(percentage);
    }

    /**
     * 构造
     *
     * @param mContext      上下文
     * @param data          数据源
     * @param maxMediaCount 最大显示 图片/视频
     * @param imageEngine   图片加载方式
     */
    public ImageAdapter(Context mContext, List<String> data, int maxMediaCount, ImageEngine imageEngine) {
        this.mContext = mContext;
        this.maxMediaCount = maxMediaCount;
        this.mInflater = LayoutInflater.from(mContext);
        this.mImageEngine = imageEngine;

        // 默认过渡图片/颜色
        TypedArray ta = mContext.getTheme().obtainStyledAttributes(
                new int[]{R.attr.thumbnail_placeholder});
        mPlaceholder = ta.getDrawable(0);
        setImages(data);
    }

    @NonNull
    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.list_item_image, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ImageAdapter.ViewHolder viewHolder, int position) {
        viewHolder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private MaskProgressView mpvImage;
        private int position;

        public ViewHolder(View itemView) {
            super(itemView);
            mpvImage = itemView.findViewById(R.id.mpvImage);
        }

        public void bind(int position) {
            //设置条目的点击事件
            itemView.setOnClickListener(this);
            //根据条目位置设置图片
            String path = mData.get(position);

            if (!isLast && position == getItemCount() - 1) {
                // 加载➕图
                mpvImage.setImageResource(R.drawable.selector_image_add);
            } else {
                // 加载图片
                mImageEngine.loadThumbnail(mContext, mpvImage.getWidth(), mPlaceholder,
                        mpvImage, Uri.fromFile(new File(path)));
            }

        }

        @Override
        public void onClick(View v) {
//            if (listener != null) listener.onItemClick(v, clickPosition);
        }
    }

}
