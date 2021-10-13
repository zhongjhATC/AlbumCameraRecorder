package com.zhongjh.progresslibrary.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sdsmdg.harjot.vectormaster.VectorMasterView;
import com.sdsmdg.harjot.vectormaster.models.PathModel;
import com.zhongjh.progresslibrary.R;
import com.zhongjh.progresslibrary.entity.MultiMediaView;
import com.zhongjh.progresslibrary.listener.MaskProgressLayoutListener;
import com.zhongjh.progresslibrary.widget.MaskProgressView;

import java.util.ArrayList;
import java.util.List;

import gaode.zhongjh.com.common.entity.MultiMedia;
import gaode.zhongjh.com.common.enums.MultimediaTypes;

/**
 * 九宫展示数据
 *
 * @author zhongjh
 * @date 2021/10/13
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>
        implements View.OnClickListener {

    private final LayoutInflater mInflater;

    /**
     * 数据源（包括视频和图片）
     */
    private ArrayList<MultiMediaView> list = new ArrayList<>();
    /**
     * 相关事件
     */
    private MaskProgressLayoutListener listener;
    /**
     * 最多显示多少个图片/视频/语音
     */
    private int maxMediaCount;
    /**
     * 是否操作
     */
    private boolean isOperation;

    /**
     * 删除图片的内圆颜色
     */
    private int deleteColor = -1;
    /**
     * 删除图片的资源,优先权比deleteColor高
     */
    private Drawable deleteImage = null;
    /**
     * 添加图片的资源
     */
    private Drawable mAddDrawable;
    /**
     * 有关遮罩层：颜色
     */
    private int maskingColor;
    /**
     * 有关遮罩层：文字大小
     */
    private int maskingTextSize;
    /**
     * 有关遮罩层：文字颜色
     */
    private int maskingTextColor;
    /**
     * 有关遮罩层：文字内容
     */
    private String maskingTextContent;

    public MaskProgressLayoutListener getListener() {
        return listener;
    }

    public void setListener(MaskProgressLayoutListener listener) {
        this.listener = listener;
    }

    public int getMaxMediaCount() {
        return maxMediaCount;
    }

    public void setMaxMediaCount(int maxMediaCount) {
        this.maxMediaCount = maxMediaCount;
    }

    /**
     * @param context     上下文
     * @param isOperation 是否操作
     * @param addDrawable 添加的图片资源
     */
    public PhotoAdapter(Context context, boolean isOperation,
                        int maskingColor, int maskingTextSize, int maskingTextColor, String maskingTextContent,
                        int deleteColor, Drawable deleteImage, Drawable addDrawable) {
        this.mInflater = LayoutInflater.from(context);

        this.isOperation = isOperation;

        this.maskingColor = maskingColor;
        this.maskingTextSize = maskingTextSize;
        this.maskingTextColor = maskingTextColor;
        this.maskingTextContent = maskingTextContent;

        this.deleteColor = deleteColor;
        this.deleteImage = deleteImage;
        // 添加图片的资源
        if (addDrawable != null) {
            mAddDrawable = addDrawable;
        }
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_image, parent, false);
        PhotoViewHolder photoViewHolder = new PhotoViewHolder(view);

        // 判断有没有自定义图片
        if (deleteImage != null) {
            // 使用自定义图片
            photoViewHolder.imgClose.setVisibility(View.VISIBLE);
            photoViewHolder.imgClose.setImageDrawable(deleteImage);
            photoViewHolder.vClose = photoViewHolder.imgClose;
        } else {
            // 使用自定义颜色
            photoViewHolder.vmvClose.setVisibility(View.VISIBLE);
            // find the correct path using name
            PathModel outline = photoViewHolder.vmvClose.getPathModelByName("close");
            // set the stroke color
            outline.setFillColor(deleteColor);
            photoViewHolder.vClose = photoViewHolder.vmvClose;
        }

        photoViewHolder.mpvImage.setMaskingColor(maskingColor);
        photoViewHolder.mpvImage.setTextSize(maskingTextSize);
        photoViewHolder.mpvImage.setTextColor(maskingTextColor);
        photoViewHolder.mpvImage.setTextString(maskingTextContent);

        return photoViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        MultiMediaView multiMediaView = list.get(position);
        if (multiMediaView.getType() == MultimediaTypes.PICTURE || multiMediaView.getType() == MultimediaTypes.VIDEO) {
            multiMediaView.setMaskProgressView(holder.mpvImage);
            multiMediaView.setItemView(holder.itemView);
        }
        // 设置条目的点击事件
        holder.itemView.setOnClickListener(this);
        holder.itemView.setTag(multiMediaView.getType());
    }

    @Override
    public int getItemCount() {
        // 数量如果小于最大值并且允许操作，才+1，这个+1是最后加个可操作的Add方框
        if (list.size() < maxMediaCount && isOperation) {
            return list.size() + 1;
        } else {
            return list.size();
        }
    }

    /**
     * 判断该item是否Add
     *
     * @param position 索引
     * @return 是否Add
     */
    private boolean isShowAddItem(int position) {
        int size = list.size();
        return position == size;
    }

    @Override
    public void onClick(View v) {
        // 防止抖动多次点击
        if (listener != null) {
            long currentTime = System.currentTimeMillis();
            // 经过了足够长的时间，允许点击
            if (currentTime - mLastClickTime > CLICK_INTERVAL) {
                if (!TextUtils.isEmpty(multiMediaView.getPath()) && multiMediaView.getPath().equals(ADD)) {
                    // 加载➕图
                    listener.onItemAdd(v, multiMediaView, imageList.size(), videoList.size(), maskProgressLayout.audioList.size());
                } else {
                    // 点击
                    if (multiMediaView.getType() == MultimediaTypes.PICTURE) {
                        // 如果是图片，直接跳转详情
                        listener.onItemClick(v, multiMediaView);
                    } else {
                        // 如果是视频，判断是否已经下载好（有path就是已经下载好了）
                        if (TextUtils.isEmpty(multiMediaView.getPath()) && multiMediaView.getUri() == null) {
                            // 执行下载事件
                            listener.onItemVideoStartDownload(multiMediaView.getUrl());
                        } else {
                            // 点击事件
                            listener.onItemClick(v, multiMediaView);
                        }
                    }
                }
                mLastClickTime = currentTime;
            }
        }
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {

        public MaskProgressView mpvImage;
        public ImageView imgPlay;
        public VectorMasterView vmvClose;
        public ImageView imgClose;
        public View vClose;

        PhotoViewHolder(View itemView) {
            super(itemView);
            mpvImage = itemView.findViewById(R.id.mpvImage);
            imgPlay = itemView.findViewById(R.id.imgPlay);
            vmvClose = itemView.findViewById(R.id.vmvClose);
            imgClose = itemView.findViewById(R.id.imgClose);
        }
    }

}
