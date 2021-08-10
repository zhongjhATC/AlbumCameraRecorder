package com.zhongjh.progresslibrary.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sdsmdg.harjot.vectormaster.VectorMasterView;
import com.sdsmdg.harjot.vectormaster.models.PathModel;
import com.zhongjh.progresslibrary.R;
import com.zhongjh.progresslibrary.engine.ImageEngine;
import com.zhongjh.progresslibrary.entity.MultiMediaView;
import com.zhongjh.progresslibrary.listener.MaskProgressLayoutListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import gaode.zhongjh.com.common.enums.MultimediaTypes;
import gaode.zhongjh.com.common.enums.MimeType;

/**
 * 自动换行的layout,只包含方框等等view
 *
 * @author zhongjh
 * @date 2019/1/29
 */
public class AutoLineFeedLayout extends ViewGroup {

    private final static String TAG = "AutoLineFeedLayout";
    private MaskProgressLayout maskProgressLayout;

    // region 相关属性

    /**
     * 该控件的整个宽度
     */
    private int mWidth = 0;
    /**
     * item的高度
     */
    private int mItemHeight = 0;
    /**
     * 添加图片的资源
     */
    private Drawable mAddDrawable;
    /**
     * 图片数据
     */
    public ArrayList<MultiMediaView> imageList = new ArrayList<>();
    /**
     * 视频数据
     */
    public ArrayList<MultiMediaView> videoList = new ArrayList<>();

    /**
     * 记录当前最后一个视频索引
     */
    private int mVideoPosition = 0;
    /**
     * 是否操作
     */
    private boolean isOperation;
    /**
     * 设置最多显示多少个图片/视频/语音
     */
    private int maxMediaCount;
    /**
     * 每行列数
     */
    private int columnNumber = 4;
    /**
     * 列与列之间的间隔
     */
    private int columnSpace = 10;
    /**
     * 图片加载方式
     */
    private ImageEngine imageEngine;
    /**
     * 默认图片
     */
    private Drawable placeholder;
    /**
     * 有关遮罩层
     */
    private int maskingColor;
    /**
     * 有关遮罩层
     */
    private int maskingTextSize;
    /**
     * 有关遮罩层
     */
    private int maskingTextColor;
    /**
     * 有关遮罩层
     */
    private String maskingTextContent;
    /**
     * 删除图片的内圆颜色
     */
    private int deleteColor = -1;
    /**
     * 删除图片的资源,优先权比deleteColor高
     */
    private Drawable deleteImage = null;
    /**
     * 相关事件
     */
    private MaskProgressLayoutListener listener;
    /**
     * 行间距
     */
    private final static int ROW_SPACE = 10;
    /**
     * 用于判断最后一个添加符号标签图片
     */
    private final static String ADD = "ADD_ADD_ADD_ADD_ADD_ADD_ADD_ADD_ADD_ADD_ADD_ADD";
    private ViewHolder viewHolderAdd;
    /**
     * 最后一次点击时间
     */
    private long mLastClickTime;
    /**
     * 1000毫秒只允许点击一次
     */
    private static final long CLICK_INTERVAL = 1000L;

    public void setListener(MaskProgressLayoutListener listener) {
        this.listener = listener;
    }

    public void removeListener() {
        this.listener = null;
    }

    /**
     * @return 最多显示多少个图片/视频/语音
     */
    public int getMaxMediaCount() {
        return maxMediaCount;
    }

    /**
     * 设置最多显示多少个图片/视频/语音
     */
    public void setMaxMediaCount(int maxMediaCount) {
        this.maxMediaCount = maxMediaCount;
    }

    // endregion

    public AutoLineFeedLayout(Context context) {
        super(context);
    }

    public AutoLineFeedLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoLineFeedLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean isOperation() {
        return isOperation;
    }

    public void setOperation(boolean operation) {
        isOperation = operation;
        if (!isOperation) {
            viewHolderAdd.itemView.setVisibility(View.GONE);
        }
    }

    /**
     * 初始化赋值配置
     */
    public void initConfig(MaskProgressLayout maskProgressLayout, ImageEngine imageEngine, boolean isOperation, Drawable placeholder, int maxMediaCount,
                           int maskingColor, int maskingTextSize, int maskingTextColor, String maskingTextContent,
                           int deleteColor, Drawable deleteImage, Drawable addDrawable,
                           int columnNumber, int columnSpace) {
        this.maskProgressLayout = maskProgressLayout;
        this.placeholder = placeholder;
        this.imageEngine = imageEngine;
        this.isOperation = isOperation;
        this.maxMediaCount = maxMediaCount;
        this.maskingColor = maskingColor;
        this.maskingTextSize = maskingTextSize;
        this.maskingTextColor = maskingTextColor;
        this.maskingTextContent = maskingTextContent;
        this.deleteColor = deleteColor;
        this.deleteImage = deleteImage;
        this.columnNumber = columnNumber;
        this.columnSpace = columnSpace;

        // 添加图片的资源
        if (addDrawable != null) {
            mAddDrawable = addDrawable;
        }

        // 默认➕号
        MultiMediaView multiMediaView = new MultiMediaView(MultimediaTypes.ADD);
        multiMediaView.setPath(ADD);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        viewHolderAdd = new ViewHolder(inflater.inflate(R.layout.list_item_image, null));
        viewHolderAdd.bind(multiMediaView);
    }


    /**
     * 初始化
     */
    @SuppressLint("InflateParams")
    public void init(int widthMeasureSpec) {
        if (mWidth == 0) {
            mWidth = MeasureSpec.getSize(widthMeasureSpec);
            addView(viewHolderAdd.itemView);
            initWidth(viewHolderAdd.itemView);

            if (mAddDrawable != null) {
                viewHolderAdd.mpvImage.setImageDrawable(mAddDrawable);
            }

            if (!isOperation) {
                Log.d(TAG, "viewHolderAdd.itemView.setVisibility(View.GONE)");
                viewHolderAdd.itemView.setVisibility(View.GONE);
            } else {
                Log.d(TAG, "viewHolderAdd.itemView.setVisibility(View.VISIBLE)");
                checkLastImages();
            }

            Log.d(TAG, "refreshView");
            refreshImageView(imageList);
            refreshVideoView(videoList);
        }
    }

    /**
     * 添加图片数据
     *
     * @param multiMediaViews 数据集合
     */
    @SuppressLint("InflateParams")
    public void addImageData(List<MultiMediaView> multiMediaViews, boolean isrefresh) {
        Log.d(TAG + " Test", "setImageData");
        if (this.imageList == null) {
            this.imageList = new ArrayList<>();
        }
        // 记录数据的结尾,为了保证视频在第一位
        this.imageList.addAll(multiMediaViews);
//        if (isrefresh) {
//            refreshImageView(imageList);
//        }
    }

    /**
     * 添加视频数据
     *
     * @param multiMediaViews 数据集合
     * @param isClean         添加前是否清空
     */
    @SuppressLint("InflateParams")
    public void addVideoData(List<MultiMediaView> multiMediaViews, boolean isClean, boolean isrefresh) {
        Log.d(TAG + " Test", "setVideoData");
        if (this.videoList == null) {
            this.videoList = new ArrayList<>();
        }
        if (isClean && this.videoList.size() > 0) {
            // 清空有关数据
            for (int i = 0; i < videoList.size(); i++) {
                removeViewAt(i);
            }
            this.videoList.clear();
            mVideoPosition = 0;
        }
        this.videoList.addAll(multiMediaViews);
//        if (isrefresh) {
//            refreshVideoView(imageList);
//        }
    }

    /**
     * 删除单个图片
     *
     * @param position 图片的索引，该索引列表不包含视频等
     */
    public void onRemoveItemImage(int position) {
        Log.d(TAG + " Test", "onRemoveItemImage");
        MultiMediaView multiMediaView = imageList.get(position);
        if (listener != null) {
            listener.onItemClose(multiMediaView.getMaskProgressView(), multiMediaView);
        }
        // 判断类型
        if (multiMediaView.getType() == MultimediaTypes.PICTURE) {
            imageList.remove(multiMediaView);
        } else if (multiMediaView.getType() == MultimediaTypes.VIDEO) {
            videoList.remove(multiMediaView);
            mVideoPosition--;
        }
        ViewGroup parent = (ViewGroup) multiMediaView.getItemView().getParent();
        parent.removeView(multiMediaView.getItemView());
        checkLastImages();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG, "onMeasure");
        // 为所有的标签childView计算宽和高
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        // 获取高的模式
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        // 建议的高度
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        // 布局的宽度采用建议宽度（match_parent或者size），如果设置wrap_content也是match_parent的效果
        init(widthMeasureSpec);

        int height;
        if (heightMode == MeasureSpec.EXACTLY) {
            // 如果高度模式为EXACTLY（match_perent或者size），则使用建议高度
            height = heightSize;
        } else {
            int childCount = 0;
            // 获取不处于隐藏的view的数量
            for (int i = 0; i < getChildCount(); i++) {
                View childView = getChildAt(i);
                if (childView.getVisibility() != GONE) {
                    childCount++;
                }
            }
            if (childCount <= 0) {
                // 没有标签时，高度为0
                height = 0;
            } else {
                // 标签行数
                int row = 1;
                // 当前行右侧剩余的宽度
                int widthSpace = mWidth;
                for (int i = 0; i < getChildCount(); i++) {
                    View view = getChildAt(i);
                    if (view.getVisibility() == GONE) {
                        continue;
                    }
                    // 获取标签宽度,包括右间距,如果是该行最后一个,则不加上间距。
                    int childW = view.getMeasuredWidth() + columnSpace;
                    Log.v(TAG, "标签宽度:" + childW + " 行数：" + row + "  剩余宽度：" + widthSpace);

                    // 如果剩余的宽度大于此标签的宽度，那就将此标签放到本行
                    if (widthSpace >= childW) {
                        widthSpace -= childW;
                    } else if (widthSpace >= view.getMeasuredWidth()) {
                        widthSpace -= view.getMeasuredWidth();
                    } else {
                        // 增加一行
                        row++;
                        // 如果剩余的宽度不能摆放此标签，那就将此标签放入一行
                        widthSpace = mWidth - childW;
                    }
                }
                // 最终布局的高度=标签高度*行数+行距*(行数-1)
                height = (mItemHeight * row) + ROW_SPACE * (row - 1);
                Log.v(TAG, "总高度:" + height + " 行数：" + row + "  标签高度：" + mItemHeight);
            }
        }

        // 设置测量宽度和测量高度
        setMeasuredDimension(mWidth, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int row = 0;
        // 标签相对于布局的右侧位置
        int right = 0;
        // 标签相对于布局的底部位置
        int botom;
        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            if (childView.getVisibility() != GONE) {
                int childW = childView.getMeasuredWidth();
                int childH = childView.getMeasuredHeight();
                // 右侧位置=本行已经占有的位置+当前标签的宽度
                right += childW;
                // 底部位置=已经摆放的行数*（标签高度+行距）+当前标签高度
                botom = row * (childH + ROW_SPACE) + childH;
                // 如果右侧位置已经超出布局右边缘，跳到下一行
                // if it can't drawing on a same line , skip to next line
                if (right > r) {
                    row++;
                    right = childW;
                    botom = row * (childH + ROW_SPACE) + childH;
                }
                Log.d(TAG, "left = " + (right - childW) + " top = " + (botom - childH) +
                        " right = " + right + " botom = " + botom);
                childView.layout(right - childW, botom - childH, right, botom);

                right += columnSpace;
            }
        }
    }

    /**
     * 检查最后一个是否是添加
     */
    public void checkLastImages() {
        Log.d(TAG + " Test", "checkLastImages");
        int mediaCount = (imageList.size() + videoList.size() + this.maskProgressLayout.audioList.size());
        if (mediaCount < maxMediaCount && isOperation) {
            Log.d(TAG, "viewHolderAdd.itemView.setVisibility(View.VISIBLE)");
            viewHolderAdd.itemView.setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG, "viewHolderAdd.itemView.setVisibility(View.GONE)");
            viewHolderAdd.itemView.setVisibility(View.GONE);
        }
        updatePosition();
    }

    /**
     * 更新索引
     */
    private void updatePosition() {
        Log.d(TAG + " Test", "updatePosition");
        for (int i = 0; i < imageList.size(); i++) {
            imageList.get(i).setPosition(i);
        }
        for (int i = 0; i < videoList.size(); i++) {
            videoList.get(i).setPosition(i);
        }
    }

    /**
     * 动态根据columnNumber设置宽度，高度跟宽度一样
     *
     * @param itemView view
     */
    private void initWidth(View itemView) {
        Log.d(TAG + " Test", "initWidth");
        // 设置动态宽度，先获取间隔宽度
        LayoutParams layoutParams = itemView.getLayoutParams();
        int sumColumnSpace = (columnNumber - 1) * columnSpace;
        // view的剩余宽度
        int viewWidth = (mWidth - sumColumnSpace) / columnNumber;
        layoutParams.width = viewWidth;
        layoutParams.height = viewWidth;
        mItemHeight = viewWidth;
        itemView.setLayoutParams(layoutParams);
    }

    /**
     * 刷新图片view
     *
     * @param imageListAdd 图片数据
     */
    @SuppressLint("InflateParams")
    public void refreshImageView(ArrayList<MultiMediaView> imageListAdd) {
        Log.d(TAG + " Test", "refreshImageView");
        if (imageList != null && imageList.size() > 0) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            for (MultiMediaView multiMediaView : imageListAdd) {
                ViewHolder viewHolder = new ViewHolder(inflater.inflate(R.layout.list_item_image, null));
                viewHolder.bind(multiMediaView);
                // 减1是因为多了一个add按钮控制
                int endingPosition = getChildCount() - 1;
                addView(viewHolder.itemView, endingPosition);
                initWidth(viewHolder.itemView);
            }
            // 为了multiMediaView的hashCode起到正确作用，这里才开始循环进行上传
            for (MultiMediaView multiMediaView : imageListAdd) {
                // 判断multiMedia有没有path,有path没有url就执行上传
                boolean pathNotNull = TextUtils.isEmpty(multiMediaView.getUrl()) &&
                        (!TextUtils.isEmpty(multiMediaView.getPath()) || multiMediaView.getUri() != null);
                if (pathNotNull) {
                    this.listener.onItemStartUploading(multiMediaView);
                }
            }
        }
        updatePosition();
    }

    /**
     * 刷新视频view
     *
     * @param videoListAdd 视频数据
     */
    @SuppressLint("InflateParams")
    public void refreshVideoView(ArrayList<MultiMediaView> videoListAdd) {
        Log.d(TAG + " Test", "refreshVideoView");
        // 记录视频的坐标点，视频默认加载在最前面
        if (videoList.size() > 0) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            for (MultiMediaView multiMediaView : videoListAdd) {
                // 标记音频，为了后面识别是视频进行播放
                multiMediaView.setMimeType(MimeType.MP4.toString());
                ViewHolder viewHolder = new ViewHolder(inflater.inflate(R.layout.list_item_image, null));
                viewHolder.bind(multiMediaView);
                addView(viewHolder.itemView, mVideoPosition);
                initWidth(viewHolder.itemView);
                mVideoPosition++;
            }
        }
        // 为了multiMediaView的hashCode起到正确作用，这里才开始循环进行上传
        for (MultiMediaView multiMediaView : videoListAdd) {
            if (multiMediaView.isUploading()) {
                // 判断multiMedia有没有path,有path没有uri就执行上传
                this.listener.onItemStartUploading(multiMediaView);
            }
        }
        updatePosition();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public MaskProgressView mpvImage;
        private final ImageView imgPlay;
        private MultiMediaView multiMediaView;

        private final View vClose;

        public ViewHolder(View itemView) {
            super(itemView);
            mpvImage = itemView.findViewById(R.id.mpvImage);
            VectorMasterView vmvClose = itemView.findViewById(R.id.vmvClose);
            ImageView imgClose = itemView.findViewById(R.id.imgClose);
            // 判断有没有自定义图片
            if (deleteImage != null) {
                // 使用自定义图片
                imgClose.setVisibility(View.VISIBLE);
                imgClose.setImageDrawable(deleteImage);
                vClose = imgClose;
            } else {
                // 使用自定义颜色
                vmvClose.setVisibility(View.VISIBLE);
                // find the correct path using name
                PathModel outline = vmvClose.getPathModelByName("close");
                // set the stroke color
                outline.setFillColor(deleteColor);
                vClose = vmvClose;
            }

            imgPlay = itemView.findViewById(R.id.imgPlay);
            mpvImage.setMaskingColor(maskingColor);
            mpvImage.setTextSize(maskingTextSize);
            mpvImage.setTextColor(maskingTextColor);
            mpvImage.setTextString(maskingTextContent);
        }

        public void bind(MultiMediaView multiMediaView) {
            this.multiMediaView = multiMediaView;

            if (multiMediaView.getType() == MultimediaTypes.PICTURE || multiMediaView.getType() == MultimediaTypes.VIDEO) {
                this.multiMediaView.setMaskProgressView(mpvImage);
                this.multiMediaView.setItemView(itemView);
            }
            // 设置条目的点击事件
            itemView.setOnClickListener(this);
            itemView.setTag(multiMediaView.getType());
            // 设置图片
            if (!TextUtils.isEmpty(multiMediaView.getPath()) && multiMediaView.getPath().equals(ADD)) {
                // 加载➕图
                mpvImage.setImageResource(R.drawable.selector_image_add);
                // 隐藏close
                vClose.setVisibility(View.GONE);
                vClose.setOnClickListener(null);
                imgPlay.setVisibility(View.GONE);
            } else {
                // 根据类型做相关设置
                if (multiMediaView.getType() == MultimediaTypes.VIDEO) {
                    // 判断是否显示播放按钮
                    imgPlay.setVisibility(View.VISIBLE);
                    // 视频处理
                } else if (multiMediaView.getType() == MultimediaTypes.PICTURE) {
                    imgPlay.setVisibility(View.GONE);
                }

                loadImage();

                // 显示close
                if (isOperation) {
                    vClose.setVisibility(View.VISIBLE);
                    vClose.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onItemClose(v, multiMediaView);
                        }
                        // 判断类型
                        if (multiMediaView.getType() == MultimediaTypes.PICTURE) {
                            imageList.remove(multiMediaView);
                        } else if (multiMediaView.getType() == MultimediaTypes.VIDEO) {
                            videoList.remove(multiMediaView);
                            mVideoPosition--;
                        }
                        ViewGroup parent = (ViewGroup) this.itemView.getParent();
                        parent.removeView(this.itemView);
                        checkLastImages();
                    });
                } else {
                    vClose.setVisibility(View.GONE);
                }
            }
        }

        /**
         * 加载图片
         */
        private void loadImage() {
            // 加载图片
            if (!TextUtils.isEmpty(multiMediaView.getPath())) {
                imageEngine.loadThumbnail(getContext(), mpvImage.getWidth(), placeholder,
                        mpvImage, Uri.fromFile(new File(multiMediaView.getPath())));
            } else if (!TextUtils.isEmpty(multiMediaView.getUrl())) {
                imageEngine.loadUrlThumbnail(getContext(), mpvImage.getWidth(), placeholder,
                        mpvImage, multiMediaView.getUrl());
            } else if (multiMediaView.getUri() != null) {
                imageEngine.loadThumbnail(getContext(), mpvImage.getWidth(), placeholder,
                        mpvImage, multiMediaView.getUri());
            }
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
                            listener.onItemImage(v, multiMediaView);
                        } else {
                            // 如果是视频，判断是否已经下载好（有path就是已经下载好了）
                            if (TextUtils.isEmpty(multiMediaView.getPath()) && multiMediaView.getUri() == null) {
                                // 执行下载事件
                                listener.onItemVideoStartDownload(multiMediaView.getUrl());
                            } else {
                                // 点击事件
                                listener.onItemImage(v, multiMediaView);
                            }
                        }
                    }
                    mLastClickTime = currentTime;
                }
            }
        }

    }

}
