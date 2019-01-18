package com.zhongjh.progresslibrary.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.zhongjh.progresslibrary.R;
import com.zhongjh.progresslibrary.engine.ImageEngine;
import com.zhongjh.progresslibrary.listener.MaskProgressLayoutListener;
import com.zhongjh.progresslibrary.widget.MaskProgressView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhongjh on 2018/10/17.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private final static String ADD = "ADD_ADD_ADD_ADD_ADD_ADD_ADD_ADD_ADD_ADD_ADD_图标";

    private Context mContext;
    private LayoutInflater mInflater;
    private List<String> mData = new ArrayList<>();
    private int maxMediaCount;  // 设置最多显示多少个图片或者视频

    private boolean isLast;         // 是否最后一个图片
    private boolean isExistingVideo;// 是否存在视频,如果为true,那么第一个必定是视频类型

    private ImageEngine mImageEngine;   // 图片加载方式
    private final Drawable mPlaceholder; // 默认图片
    private MaskProgressLayoutListener listener;   // 点击事件

    /**
     * 构造
     *
     * @param mContext      上下文
     * @param maxMediaCount 最大显示 图片/视频
     * @param imageEngine   图片加载方式
     * @param placeholder   图片
     */
    public ImageAdapter(Context mContext, int maxMediaCount, ImageEngine imageEngine, Drawable placeholder) {
        this.mContext = mContext;
        this.maxMediaCount = maxMediaCount;
        this.mInflater = LayoutInflater.from(mContext);
        this.mImageEngine = imageEngine;

        // 默认过渡图片/颜色
        mPlaceholder = placeholder;

        setImages(new ArrayList<>());
    }

    public void setMaskProgressLayoutListener(MaskProgressLayoutListener listener) {
        this.listener = listener;
    }

    /**
     * 添加视频
     */
    public void addVideo(String video) {
        isExistingVideo = true;
        // 添加前，如果最后一个是添加的图标，那么就先删除它，后面再检查是否需要添加 “添加图标”
        if (mData.size() > 0 && mData.get(mData.size() - 1).equals(ADD)) {
            mData.remove(mData.size() - 1);
        }
        mData.add(0, video);
        checkLastImages();
        notifyDataSetChanged();
    }

    /**
     * 设置数据
     *
     * @param data 数据源
     */
    public void setImages(List<String> data) {
        // 添加前，如果最后一个是添加的图标，那么就先删除它，后面再检查是否需要添加 “添加图标”
        if (mData.size() > 0 && mData.get(mData.size() - 1).equals(ADD)) {
            mData.remove(mData.size() - 1);
        }
        mData.addAll(data);
        checkLastImages();
        notifyDataSetChanged();
    }

    /**
     * 检查最后一个是否是图片
     */
    private void checkLastImages() {
        if (getItemCount() < maxMediaCount) {
            if (mData.size() > 0 && mData.get(mData.size() - 1).equals(ADD)) {
                return;
            }
            // 未满上限数量可以继续设置
            mData.add(ADD);
            isLast = false;
        } else {
            isLast = true;
        }
    }

    /**
     * 由于 图片/视频 未选满时，最后一张显示添加 图片/视频 ，因此这个方法返回真正的已选 图片/视频
     *
     * @return 返回所有地址
     */
    public List<String> getImages() {
        int positionFirst = 0;
        if (isExistingVideo)
            // 如果存在视频，那么索引从第二个算起
            positionFirst = 1;
        if (!isLast)
            return new ArrayList<>(mData.subList(positionFirst, mData.size() - 1));
        else
            return new ArrayList<>(mData.subList(positionFirst, mData.size()));
    }

    /**
     * 设置百分比
     *
     * @param percentage 百分比值，1=1%
     */
    public void setPercentage(ViewHolder viewHolder, int percentage) {
        viewHolder.mpvImage.setPercentage(percentage);
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
        private ImageView imgClose;
        private int position;

        public ViewHolder(View itemView) {
            super(itemView);
            mpvImage = itemView.findViewById(R.id.mpvImage);
            imgClose = itemView.findViewById(R.id.imgClose);
        }

        public void bind(int position) {
            this.position = position;
            //设置条目的点击事件
            itemView.setOnClickListener(this);
            //根据条目位置设置图片
            String path = mData.get(position);

            if (!isLast && position == getItemCount() - 1) {
                // 加载➕图
                mpvImage.setImageResource(R.drawable.selector_image_add);
                // 隐藏close
                imgClose.setVisibility(View.GONE);
                imgClose.setOnClickListener(null);
            } else {
                // 加载图片
                mImageEngine.loadThumbnail(mContext, mpvImage.getWidth(), mPlaceholder,
                        mpvImage, Uri.fromFile(new File(path)));
                // 显示close
                imgClose.setVisibility(View.VISIBLE);
                imgClose.setOnClickListener(v -> {
                    mData.remove(position);
                    //删除动画
                    notifyItemRemoved(position);
                    checkLastImages();
                    notifyDataSetChanged();
                });
            }
        }

        @Override
        public void onClick(View v) {
            if (listener != null)
                if (!isLast && position == getItemCount() - 1) {
                    // 加载➕图
                    listener.onItemAdd(v, position, getItemCount() - 1);
                } else {
                    // 加载图片
                    listener.onItemImage(v, position);
                }
        }

    }

}
