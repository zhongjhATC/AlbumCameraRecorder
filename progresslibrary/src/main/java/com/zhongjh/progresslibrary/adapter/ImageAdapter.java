package com.zhongjh.progresslibrary.adapter;

import android.content.Context;
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
import com.zhongjh.progresslibrary.entity.MultiMedia;
import com.zhongjh.progresslibrary.listener.MaskProgressLayoutListener;
import com.zhongjh.progresslibrary.widget.MaskProgressView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhongjh on 2018/10/17.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private final static String ADD = "ADD_ADD_ADD_ADD_ADD_ADD_ADD_ADD_ADD_ADD_ADD_图标";     // 用于判断最后一个添加符号标签图片

    private Context mContext;
    private LayoutInflater mInflater;
    private List<MultiMedia> mData = new ArrayList<>();
    private int maxMediaCount;  // 设置最多显示多少个图片或者视频

    public boolean isExistingVideo;// 是否存在视频,如果为true,那么第一个必定是视频类型

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

        checkLastImages();
        notifyDataSetChanged();
    }

    public void setMaskProgressLayoutListener(MaskProgressLayoutListener listener) {
        this.listener = listener;
    }

    /**
     * 添加视频
     */
    public MultiMedia addVideo(String video) {
        isExistingVideo = true;
        // 记录添加数据的position
        int positionFirst = getItemDataCount();
        // 添加前，如果最后一个是添加的图标，那么就先删除它，后面再检查是否需要添加 “添加图标”
        if (mData.size() > 0 && mData.get(mData.size() - 1).getPath().equals(ADD)) {
            mData.remove(mData.size() - 1);
        }
        // 添加新增的数据
        MultiMedia multiMedia = new MultiMedia(video, 0);
        mData.add(0, multiMedia);
        checkLastImages();
        notifyDataSetChanged();
        return multiMedia;
    }

    /**
     * 添加数据
     * 添加数据后，返回他们在当前列表中处于的位置
     *
     * @param data 数据源
     */
    public List<MultiMedia> addImages(List<String> data) {
        // 记录添加数据的position
        int positionFirst = getItemDataCount();
        // 添加前，如果最后一个是添加的图标，那么就先删除它，后面再检查是否需要添加 “添加图标”
        if (mData.size() > 0 && mData.get(mData.size() - 1).getPath().equals(ADD)) {
            mData.remove(mData.size() - 1);
        }
        // 添加新增的数据
        for (int i = 0; i < data.size(); i++) {
            MultiMedia multiMedia = new MultiMedia(data.get(i), getImagesCount());
            mData.add(multiMedia);
        }
        checkLastImages();
        notifyDataSetChanged();
        return mData.subList(positionFirst, getItemDataCount());
    }

    /**
     * 检查最后一个是否是添加
     */
    private void checkLastImages() {
        if (getItemCount() < maxMediaCount) {
            if (mData.size() > 0 && mData.get(mData.size() - 1).getPath().equals(ADD)) {
                return;
            }
            // 未满上限数量可以继续设置
            MultiMedia multiMedia = new MultiMedia(ADD, mData.size());
            mData.add(multiMedia);
        }
    }

    /**
     * @return 返回纯图片的地址
     */
    public List<MultiMedia> getImages() {
        int positionFirst = 0;
        if (isExistingVideo)
            // 如果存在视频，那么索引从第二个算起
            positionFirst = 1;
        if (mData.size() > 0 && mData.get(mData.size() - 1).getPath().equals(ADD))
            return mData.subList(positionFirst, mData.size() - 1);
        else
            return mData.subList(positionFirst, mData.size());
    }

    /**
     * @return 返回纯图片的数据长度
     */
    public int getImagesCount() {
        int size;
        if (mData.size() > 0 && mData.get(mData.size() - 1).getPath().equals(ADD))
            size = mData.size() - 1 < 0 ? 0 : mData.size() - 1;
        else
            size = mData.size();
        if (isExistingVideo)
            // 如果存在视频，那么索引从第二个算起
            return size - 1;
        else
            return size;
    }

    /**
     * @return 返回图片/视频的所有数据
     */
    public List<MultiMedia> getItemData() {
        if (mData.size() > 0 && mData.get(mData.size() - 1).getPath().equals(ADD))
            return mData.subList(0, mData.size() - 1);
        else
            return mData.subList(0, mData.size());
    }

    /**
     * @return 返回图片/视频的所有数据长度
     */
    public int getItemDataCount() {
        if (mData.size() > 0 && mData.get(mData.size() - 1).getPath().equals(ADD))
            return mData.size() - 1 < 0 ? 0 : mData.size() - 1;
        else
            return mData.size();
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

        public MaskProgressView mpvImage;
        private ImageView imgClose;
        private ImageView imgPlay;
        private int position;

        public ViewHolder(View itemView) {
            super(itemView);
            mpvImage = itemView.findViewById(R.id.mpvImage);
            imgClose = itemView.findViewById(R.id.imgClose);
            imgPlay = itemView.findViewById(R.id.imgPlay);
        }

        public void bind(int position) {
//            this.position = position;
//            //设置条目的点击事件
//            itemView.setOnClickListener(this);
//            //根据条目位置设置图片
//            MultiMedia multiMedia = mData.get(position);
//
//            if (mData.get(position).getPath().equals(ADD)) {
//                // 加载➕图
//                mpvImage.setImageResource(R.drawable.selector_image_add);
//                // 隐藏close
//                imgClose.setVisibility(View.GONE);
//                imgClose.setOnClickListener(null);
//                imgPlay.setVisibility(View.GONE);
//            } else {
//                // 加载图片
//                mImageEngine.loadThumbnail(mContext, mpvImage.getWidth(), mPlaceholder,
//                        mpvImage, Uri.fromFile(new File(multiMedia.getPath())));
//                // 显示close
//                imgClose.setVisibility(View.VISIBLE);
//                imgClose.setOnClickListener(v -> {
//                    if (listener != null)
//                        listener.onItemClose(v, position);
//                    // 如果删除是第一个并且是视频，那么把视频关闭
//                    if (position == 0 && isExistingVideo) {
//                        isExistingVideo = false;
//                    }
//                    mData.remove(position);
//                    //删除动画
//                    notifyItemRemoved(position);
//                    checkLastImages();
//                    notifyDataSetChanged();
//                });
//                // 判断是否显示播放按钮
//                if (isExistingVideo && position == 0) {
//                    imgPlay.setVisibility(View.VISIBLE);
//                } else {
//                    imgPlay.setVisibility(View.GONE);
//                }
//            }
        }

        @Override
        public void onClick(View v) {
//            if (listener != null)
//                if (mData.get(position).getPath().equals(ADD)) {
//                    // 加载➕图
//                    listener.onItemAdd(v, position, getImages().size(), isExistingVideo ? 1 : 0);
//                } else {
//                    // 加载图片
//                    listener.onItemImage(v, position);
//                }
        }

    }

}
