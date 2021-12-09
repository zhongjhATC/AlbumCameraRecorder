package com.zhongjh.progresslibrary.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sdsmdg.harjot.vectormaster.VectorMasterView;
import com.sdsmdg.harjot.vectormaster.models.PathModel;
import com.zhongjh.progresslibrary.R;
import com.zhongjh.progresslibrary.engine.ImageEngine;
import com.zhongjh.progresslibrary.entity.MultiMediaView;
import com.zhongjh.progresslibrary.listener.MaskProgressLayoutListener;
import com.zhongjh.progresslibrary.widget.MaskProgressLayout;
import com.zhongjh.progresslibrary.widget.MaskProgressView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.zhongjh.common.enums.MultimediaTypes;
import com.zhongjh.common.listener.OnMoreClickListener;

/**
 * 九宫展示数据
 *
 * @author zhongjh
 * @date 2021/10/13
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private final Context mContext;
    private final GridLayoutManager mGridLayoutManage;

    private final static String TAG = PhotoAdapter.class.getSimpleName();
    private final LayoutInflater mInflater;

    /**
     * 数据源（包括视频和图片）
     */
    private final ArrayList<MultiMediaView> list = new ArrayList<>();
    /**
     * 每次添加数据增长的id，用于在相同地址的情况下区分两张图等
     */
    private long mId;
    /**
     * 图片数据数量
     */
    private int mImageCount = 0;
    /**
     * 视频数据数量
     */
    private int mVideoCount = 0;

    private final MaskProgressLayout maskProgressLayout;
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
    private final int deleteColor;
    /**
     * 删除图片的资源,优先权比deleteColor高
     */
    private final Drawable deleteImage;
    /**
     * 添加图片的资源
     */
    private Drawable mAddDrawable;
    /**
     * 图片加载方式
     */
    private final ImageEngine imageEngine;
    /**
     * 默认图片
     */
    private final Drawable placeholder;
    /**
     * 有关遮罩层：颜色
     */
    private final int maskingColor;
    /**
     * 有关遮罩层：文字大小
     */
    private final int maskingTextSize;
    /**
     * 有关遮罩层：文字颜色
     */
    private final int maskingTextColor;
    /**
     * 有关遮罩层：文字内容
     */
    private final String maskingTextContent;

    private int mItemHeight;
    MultiMediaView mMultiMediaViewAdd = new MultiMediaView(MultimediaTypes.ADD);

    public MaskProgressLayoutListener getListener() {
        return listener;
    }

    public void setListener(MaskProgressLayoutListener listener) {
        this.listener = listener;
    }

    public void removeListener() {
        this.listener = null;
    }

    public int getMaxMediaCount() {
        return maxMediaCount;
    }

    public void setMaxMediaCount(int maxMediaCount) {
        this.maxMediaCount = maxMediaCount;
    }

    public void setOperation(boolean operation) {
        isOperation = operation;
    }

    /**
     * @param context            上下文
     * @param maskProgressLayout 父控件
     * @param isOperation        是否操作
     * @param maskingColor       有关遮罩层：颜色
     * @param maskingTextSize    有关遮罩层：文字大小
     * @param maskingTextColor   有关遮罩层：文字颜色
     * @param maskingTextContent 有关遮罩层：文字内容
     * @param addDrawable        添加的图片资源
     */
    public PhotoAdapter(Context context, GridLayoutManager gridLayoutManager, MaskProgressLayout maskProgressLayout,
                        ImageEngine imageEngine, Drawable placeholder, boolean isOperation, int maxMediaCount,
                        int maskingColor, int maskingTextSize, int maskingTextColor, String maskingTextContent,
                        int deleteColor, Drawable deleteImage, Drawable addDrawable) {
        this.mContext = context;
        this.mGridLayoutManage = gridLayoutManager;
        this.mInflater = LayoutInflater.from(context);

        this.maskProgressLayout = maskProgressLayout;
        this.imageEngine = imageEngine;
        this.placeholder = placeholder;
        this.isOperation = isOperation;
        this.maxMediaCount = maxMediaCount;

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

        if (mItemHeight == 0) {
            // 设置高度
            ViewGroup.LayoutParams params = photoViewHolder.itemView.getLayoutParams();
            // 动态计算，设置item的宽高一致，总宽度-左右margin-左右padding / 总列数-item左右margin-item左右padding
            mItemHeight =
                    mGridLayoutManage.getWidth() / mGridLayoutManage.getSpanCount() -
                            2 * photoViewHolder.itemView.getPaddingLeft() -
                            2 * ((ViewGroup.MarginLayoutParams) params).leftMargin;
            params.height = mItemHeight;
        } else {
            // 设置高度
            ViewGroup.LayoutParams params = photoViewHolder.itemView.getLayoutParams();
            params.height = mItemHeight;
        }


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
        // 设置图片
        if (isShowAddItem(position)) {
            // 加载➕图
            holder.mpvImage.setImageResource(R.drawable.selector_image_add);
            // 隐藏close
            holder.vClose.setVisibility(View.GONE);
            holder.vClose.setOnClickListener(null);
            holder.imgPlay.setVisibility(View.GONE);
            // 设置条目的点击事件
            holder.itemView.setOnClickListener(new OnMoreClickListener() {
                @Override
                public void onMoreClickListener(View v) {
                    mMultiMediaViewAdd.setMaskProgressView(holder.mpvImage);
                    mMultiMediaViewAdd.setItemView(holder.itemView);
                    // 点击加载➕图
                    if (listener != null) {
                        listener.onItemAdd(v, mMultiMediaViewAdd, mImageCount, mVideoCount, maskProgressLayout.audioList.size());
                    }
                }
            });
            holder.mpvImage.reset();
        } else {
            MultiMediaView multiMediaView = list.get(position);
            if (multiMediaView.getType() == MultimediaTypes.PICTURE || multiMediaView.getType() == MultimediaTypes.VIDEO) {
                multiMediaView.setMaskProgressView(holder.mpvImage);
                multiMediaView.setItemView(holder.itemView);
            }

            // 根据类型做相关设置
            if (multiMediaView.getType() == MultimediaTypes.VIDEO) {
                // 判断是否显示播放按钮
                holder.imgPlay.setVisibility(View.VISIBLE);
                // 视频处理
            } else if (multiMediaView.getType() == MultimediaTypes.PICTURE) {
                holder.imgPlay.setVisibility(View.GONE);
            }

            holder.loadImage(mContext, imageEngine, placeholder, multiMediaView, mItemHeight);

            // 显示close
            if (isOperation) {
                holder.vClose.setVisibility(View.VISIBLE);
                holder.vClose.setOnClickListener(v -> removePosition(multiMediaView));
            } else {
                holder.vClose.setVisibility(View.GONE);
            }

            // 设置条目的点击事件
            holder.itemView.setOnClickListener(new OnMoreClickListener() {
                @Override
                public void onMoreClickListener(View v) {
                    if (listener != null) {
                        // 点击
                        if (multiMediaView.getType() == MultimediaTypes.PICTURE) {
                            // 如果是图片，直接跳转详情
                            listener.onItemClick(v, multiMediaView);
                        } else {
                            // 如果是视频，判断是否已经下载好（有path就是已经下载好了）
                            if (TextUtils.isEmpty(multiMediaView.getPath()) && multiMediaView.getUri() == null) {
                                // 执行下载事件
                                boolean isContinue = listener.onItemVideoStartDownload(v, multiMediaView);
                                if (isContinue) {
                                    // 点击事件
                                    listener.onItemClick(v, multiMediaView);
                                }
                            } else {
                                // 点击事件
                                listener.onItemClick(v, multiMediaView);
                            }
                        }
                    }
                }
            });

            // 是否上传
            if (multiMediaView.isUploading()) {
                // 设置该对象已经上传请求过了
                multiMediaView.setUploading(false);
                if (listener != null) {
                    this.listener.onItemStartUploading(multiMediaView);
                }
            } else {
                // 取消上传动画
                holder.mpvImage.reset();
            }

        }
    }

    /**
     * 清空数据
     */
    public void clearAll() {
        list.clear();
        notifyDataSetChanged();
    }

    /**
     * @return 获取所有数据源
     */
    public ArrayList<MultiMediaView> getData() {
        return list;
    }

    /**
     * 获取图片的数据
     */
    public ArrayList<MultiMediaView> getImageData() {
        ArrayList<MultiMediaView> imageDatas = new ArrayList<>();
        for (MultiMediaView multiMediaView : list) {
            if (multiMediaView.getType() == MultimediaTypes.PICTURE) {
                imageDatas.add(multiMediaView);
            }
        }
        return imageDatas;
    }

    /**
     * 获取视频的数据
     */
    public ArrayList<MultiMediaView> getVideoData() {
        ArrayList<MultiMediaView> videoDatas = new ArrayList<>();
        for (MultiMediaView multiMediaView : list) {
            if (multiMediaView.getType() == MultimediaTypes.VIDEO) {
                videoDatas.add(multiMediaView);
            }
        }
        return videoDatas;
    }

    /**
     * 添加图片数据
     *
     * @param multiMediaViews 数据集合
     */
    public void addImageData(List<MultiMediaView> multiMediaViews) {
        Log.d(TAG + " Test", "addImageData");
        int position = getNeedAddPosition(MultimediaTypes.PICTURE);
        for (MultiMediaView item : multiMediaViews) {
            item.setId(mId++);
        }
        list.addAll(position, multiMediaViews);
        // 刷新ui
        notifyItemRangeInserted(position, multiMediaViews.size());
        notifyItemRangeChanged(position, multiMediaViews.size());
        isRemoveAdd();
    }

    /**
     * 赋值图片数据
     */
    public void setImageData(List<MultiMediaView> multiMediaViews) {
        Log.d(TAG + " Test", "setImageData");
        // 删除当前所有图片
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).getType() == MultimediaTypes.PICTURE) {
                list.remove(i);
                notifyItemRemoved(i);
            }
        }
        // 增加新的图片数据
        int position = list.size() - 1;
        for (MultiMediaView item : multiMediaViews) {
            item.setId(mId++);
        }
        list.addAll(multiMediaViews);
        notifyItemRangeInserted(position, multiMediaViews.size());
        notifyItemRangeChanged(position, multiMediaViews.size());
        isRemoveAdd();
    }

    /**
     * 添加视频数据
     *
     * @param multiMediaViews 数据集合
     */
    @SuppressLint("InflateParams")
    public void addVideoData(List<MultiMediaView> multiMediaViews) {
        Log.d(TAG + " Test", "addVideoData");
        int position = getNeedAddPosition(MultimediaTypes.VIDEO);
        for (MultiMediaView item : multiMediaViews) {
            item.setId(mId++);
        }
        list.addAll(position, multiMediaViews);
        // 刷新ui
        notifyItemRangeInserted(position, multiMediaViews.size());
        notifyItemRangeChanged(position, multiMediaViews.size());
        isRemoveAdd();
    }

    /**
     * 赋值视频数据
     */
    public void setVideoData(List<MultiMediaView> multiMediaViews) {
        Log.d(TAG + " Test", "setVideoData");
        // 删除当前所有视频
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).getType() == MultimediaTypes.VIDEO) {
                list.remove(i);
                notifyItemRemoved(i);
            }
        }

        // 增加新的视频数据
        for (MultiMediaView item : multiMediaViews) {
            item.setId(mId++);
        }
        list.addAll(0, multiMediaViews);
        notifyItemRangeInserted(0, multiMediaViews.size());
        notifyItemRangeChanged(0, multiMediaViews.size());
        isRemoveAdd();
    }

    /**
     * 删除某个数据
     *
     * @param multiMediaView 集合里面的某个对象
     */
    private void removePosition(MultiMediaView multiMediaView) {
        int position = list.indexOf(multiMediaView);
        removePosition(position);
    }

    /**
     * 删除某个数据
     *
     * @param position 索引
     */
    public void removePosition(int position) {
        MultiMediaView multiMediaView = list.get(position);
        if (listener != null) {
            listener.onItemClose(multiMediaView.getItemView(), multiMediaView);
        }
        list.remove(multiMediaView);
        multiMediaView.getMaskProgressView().reset();
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, 1);
    }

    @Override
    public int getItemCount() {
        // 计算图片和视频的数量
        mImageCount = 0;
        mVideoCount = 0;
        for (MultiMediaView item : list) {
            if (item.getType() == MultimediaTypes.PICTURE) {
                mImageCount++;
            } else if (item.getType() == MultimediaTypes.VIDEO) {
                mVideoCount++;
            }
        }
        // 数量如果小于最大值并且允许操作，才+1，这个+1是最后加个可操作的Add方框
        if ((list.size() + maskProgressLayout.audioList.size()) < maxMediaCount && isOperation) {
            return list.size() + 1;
        } else {
            return list.size();
        }
    }

    /**
     * 判断是否删除Add Item
     */
    private void isRemoveAdd() {
        // 判断是否等于最大数量,并且是可操作的才进行去掉add
        if ((list.size() + maskProgressLayout.audioList.size()) >= maxMediaCount
                && isOperation) {
            notifyItemRemoved(list.size());
            notifyItemRangeChanged(list.size(), 1);
        }
    }

    /**
     * 根据类型获取当前要添加的位置，新增的图片在最后一个，新增的视频在图片的前面
     *
     * @param type 数据类型
     * @return 索引
     */
    private int getNeedAddPosition(int type) {
        if (type == MultimediaTypes.PICTURE) {
            return Math.max(list.size(), 0);
        } else if (type == MultimediaTypes.VIDEO) {
            // 获取图片第一个索引
            return getImageFirstPosition();
        }
        return 0;
    }

    /**
     * 获取列表中第一个图片的索引
     *
     * @return 索引
     */
    private int getImageFirstPosition() {
        if (list.size() <= 0) {
            return 0;
        }
        for (MultiMediaView item : list) {
            if (item.getType() == MultimediaTypes.PICTURE) {
                return list.indexOf(item);
            }
        }
        return 0;
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

    static class PhotoViewHolder extends RecyclerView.ViewHolder {

        public MaskProgressView mpvImage;
        public ImageView imgPlay;
        public VectorMasterView vmvClose;
        public ImageView imgClose;
        public View vClose;
        public FrameLayout flMain;

        PhotoViewHolder(View itemView) {
            super(itemView);
            mpvImage = itemView.findViewById(R.id.mpvImage);
            imgPlay = itemView.findViewById(R.id.imgPlay);
            vmvClose = itemView.findViewById(R.id.vmvClose);
            imgClose = itemView.findViewById(R.id.imgClose);
            flMain = itemView.findViewById(R.id.flMain);
        }

        /**
         * 加载图片
         */
        private void loadImage(Context context, ImageEngine imageEngine,
                               Drawable placeholder, MultiMediaView multiMediaView, int height) {
            // 加载图片
            if (!TextUtils.isEmpty(multiMediaView.getPath())) {
                imageEngine.loadThumbnail(context, height, placeholder,
                        mpvImage, Uri.fromFile(new File(multiMediaView.getPath())));
            } else if (!TextUtils.isEmpty(multiMediaView.getUrl())) {
                imageEngine.loadUrlThumbnail(context, height, placeholder,
                        mpvImage, multiMediaView.getUrl());
            } else if (multiMediaView.getUri() != null) {
                imageEngine.loadThumbnail(context, height, placeholder,
                        mpvImage, multiMediaView.getUri());
            }
        }
    }

}
