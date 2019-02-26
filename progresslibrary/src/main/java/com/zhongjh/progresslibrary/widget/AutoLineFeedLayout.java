package com.zhongjh.progresslibrary.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.RecyclerView;
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
import com.zhongjh.progresslibrary.entity.MultiMedia;
import com.zhongjh.progresslibrary.listener.MaskProgressLayoutListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 自动换行的layout,只包含方框等等view
 * Created by zhongjh on 2019/1/29.
 */
public class AutoLineFeedLayout extends ViewGroup {

    private final static String TAG = "AutoLineFeedLayout";
    private MaskProgressLayout maskProgressLayout;


    // region 相关属性
    public List<MultiMedia> imageList = new ArrayList<>();     // 图片数据
    public List<MultiMedia> videoList = new ArrayList<>();     // 视频数据

    private int maxMediaCount;  // 设置最多显示多少个图片或者视频
    private ImageEngine imageEngine;   // 图片加载方式
    private Drawable placeholder; // 默认图片
    private int maskingColor; // 有关遮罩层
    private int maskingTextSize;// 有关遮罩层
    private int maskingTextColor;// 有关遮罩层
    private String maskingTextContent;// 有关遮罩层
    private int deleteColor = -1;// 删除图片的内圆颜色
    private Drawable deleteImage = null;// 删除图片的资源,优先权比deleteColor高
    private Drawable addDrawable = null;// 添加图片的资源
    private MaskProgressLayoutListener listener;   // 相关事件
    private int LEFT_RIGHT_SPACE = 10; //dip
    private int ROW_SPACE = 10;
    private final static String ADD = "ADD_ADD_ADD_ADD_ADD_ADD_ADD_ADD_ADD_ADD_ADD_图标";     // 用于判断最后一个添加符号标签图片
    private ViewHolder viewHolderAdd;

    public void setListener(MaskProgressLayoutListener listener) {
        this.listener = listener;
    }

    // endregion

    public AutoLineFeedLayout(Context context) {
        super(context);
        init();
    }

    public AutoLineFeedLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AutoLineFeedLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化赋值配置
     */
    public void initConfig(MaskProgressLayout maskProgressLayout, ImageEngine imageEngine, Drawable placeholder, int maxMediaCount,
                           int maskingColor, int maskingTextSize, int maskingTextColor, String maskingTextContent,
                           int deleteColor, Drawable deleteImage, Drawable addDrawable) {
        this.maskProgressLayout = maskProgressLayout;
        this.placeholder = placeholder;
        this.imageEngine = imageEngine;
        this.maxMediaCount = maxMediaCount;
        this.maskingColor = maskingColor;
        this.maskingTextSize = maskingTextSize;
        this.maskingTextColor = maskingTextColor;
        this.maskingTextContent = maskingTextContent;
        this.deleteColor = deleteColor;
        this.deleteImage = deleteImage;
        this.addDrawable = addDrawable;

        if (this.addDrawable != null)
            viewHolderAdd.mpvImage.setImageDrawable(this.addDrawable);
    }

    /**
     * 初始化
     */
    private void init() {
        // 默认➕号
        MultiMedia multiMedia = new MultiMedia(ADD, -1);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        viewHolderAdd = new ViewHolder(inflater.inflate(R.layout.list_item_image, null));
        viewHolderAdd.bind(multiMedia);
        addView(viewHolderAdd.itemView);
    }

    /**
     * 添加图片数据
     *
     * @param multiMedias 数据集合
     */
    public void addImageData(List<MultiMedia> multiMedias) {
        if (this.imageList == null) {
            this.imageList = new ArrayList<>();
        }
        // 记录数据的结尾,为了保证视频在第一位
        int endingPostion = imageList.size() + videoList.size();
        this.imageList.addAll(multiMedias);
        if (imageList != null && imageList.size() > 0) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            for (MultiMedia multiMedia : multiMedias) {
                ViewHolder viewHolder = new ViewHolder(inflater.inflate(R.layout.list_item_image, null));
                viewHolder.bind(multiMedia);
                addView(viewHolder.itemView, endingPostion);
                // 判断multiMedia有没有path,有path没有uri就执行上传
                if (TextUtils.isEmpty(multiMedia.getUrl()) && !TextUtils.isEmpty(multiMedia.getPath())) {
                    this.listener.onItemStartUploading(multiMedia);
                }
            }
        }
        checkLastImages();
    }

    /**
     * 添加视频数据
     *
     * @param multiMedias 数据集合
     */
    public void addVideoData(List<MultiMedia> multiMedias) {
        if (this.videoList == null) {
            this.videoList = new ArrayList<>();
        }
        this.videoList.addAll(multiMedias);
        if (videoList != null && videoList.size() > 0) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            for (MultiMedia multiMedia : multiMedias) {
                ViewHolder viewHolder = new ViewHolder(inflater.inflate(R.layout.list_item_image, null));
                viewHolder.bind(multiMedia);
                addView(viewHolder.itemView, 0);
                // 判断multiMedia有没有path,有path没有uri就执行上传
                if (TextUtils.isEmpty(multiMedia.getUrl()) && !TextUtils.isEmpty(multiMedia.getPath())) {
                    this.listener.onItemStartUploading(multiMedia);
                }
            }
        }
        checkLastImages();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //为所有的标签childView计算宽和高
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        //获取高的模式
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        //建议的高度
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        //布局的宽度采用建议宽度（match_parent或者size），如果设置wrap_content也是match_parent的效果
        int width = MeasureSpec.getSize(widthMeasureSpec);

        int height;
        if (heightMode == MeasureSpec.EXACTLY) {
            //如果高度模式为EXACTLY（match_perent或者size），则使用建议高度
            height = heightSize;
        } else {
            //其他情况下（AT_MOST、UNSPECIFIED）需要计算计算高度
            int childCount = getChildCount();
            if (childCount <= 0) {
                height = 0;   //没有标签时，高度为0
            } else {
                int row = 1;  // 标签行数
                int widthSpace = width;// 当前行右侧剩余的宽度
                for (int i = 0; i < childCount; i++) {
                    View view = getChildAt(i);
                    //获取标签宽度
                    int childW = view.getMeasuredWidth();
                    Log.v(TAG, "标签宽度:" + childW + " 行数：" + row + "  剩余宽度：" + widthSpace);
                    if (widthSpace >= childW) {
                        //如果剩余的宽度大于此标签的宽度，那就将此标签放到本行
                        widthSpace -= childW;
                    } else {
                        row++;    //增加一行
                        //如果剩余的宽度不能摆放此标签，那就将此标签放入一行
                        widthSpace = width - childW;
                    }
                    //减去标签左右间距
                    widthSpace -= LEFT_RIGHT_SPACE;
                }
                //由于每个标签的高度是相同的，所以直接获取第一个标签的高度即可
                int childH = getChildAt(0).getMeasuredHeight();
                //最终布局的高度=标签高度*行数+行距*(行数-1)
                height = (childH * row) + ROW_SPACE * (row - 1);

                Log.v(TAG, "总高度:" + height + " 行数：" + row + "  标签高度：" + childH);
            }
        }

        //设置测量宽度和测量高度
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int row = 0;
        int right = 0;   // 标签相对于布局的右侧位置
        int botom;       // 标签相对于布局的底部位置
        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            int childW = childView.getMeasuredWidth();
            int childH = childView.getMeasuredHeight();
            //右侧位置=本行已经占有的位置+当前标签的宽度
            right += childW;
            //底部位置=已经摆放的行数*（标签高度+行距）+当前标签高度
            botom = row * (childH + ROW_SPACE) + childH;
            // 如果右侧位置已经超出布局右边缘，跳到下一行
            // if it can't drawing on a same line , skip to next line
            if (right > (r - LEFT_RIGHT_SPACE)) {
                row++;
                right = childW;
                botom = row * (childH + ROW_SPACE) + childH;
            }
            Log.d(TAG, "left = " + (right - childW) + " top = " + (botom - childH) +
                    " right = " + right + " botom = " + botom);
            childView.layout(right - childW, botom - childH, right, botom);

            right += LEFT_RIGHT_SPACE;
        }
    }

    /**
     * 检查最后一个是否是添加
     */
    private void checkLastImages() {
        if ((imageList.size() + videoList.size()) < maxMediaCount) {
            viewHolderAdd.itemView.setVisibility(View.VISIBLE);
        } else {
            viewHolderAdd.itemView.setVisibility(View.GONE);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public MaskProgressView mpvImage;
        private ImageView imgPlay;
        private MultiMedia multiMedia;

        private View vClose;
        private VectorMasterView vmvClose;
        private ImageView imgClose;


        public ViewHolder(View itemView) {
            super(itemView);
            mpvImage = itemView.findViewById(R.id.mpvImage);
            vmvClose = itemView.findViewById(R.id.vmvClose);
            imgClose = itemView.findViewById(R.id.imgClose);
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

        public void bind(MultiMedia multiMedia) {
            this.multiMedia = multiMedia;
            if (multiMedia.getType() == 0 || multiMedia.getType() == 1) {
                this.multiMedia.setMaskProgressView(mpvImage);
            }
            //设置条目的点击事件
            itemView.setOnClickListener(this);
            //设置图片
            if (!TextUtils.isEmpty(multiMedia.getPath()) && multiMedia.getPath().equals(ADD)) {
                // 加载➕图
                mpvImage.setImageResource(R.drawable.selector_image_add);
                // 隐藏close
                vClose.setVisibility(View.GONE);
                vClose.setOnClickListener(null);
                imgPlay.setVisibility(View.GONE);
            } else {
                // 根据类型做相关设置
                if (multiMedia.getType() == 1) {
                    // 判断是否显示播放按钮
                    imgPlay.setVisibility(View.VISIBLE);
                    // 视频处理
                } else if (multiMedia.getType() == 0) {
                    imgPlay.setVisibility(View.GONE);
                }

                loadImage();

                // 显示close
                vClose.setVisibility(View.VISIBLE);
                vClose.setOnClickListener(v -> {
                    if (listener != null)
                        listener.onItemClose(v, multiMedia);
                    // 判断类型
                    if (multiMedia.getType() == 0) {
                        imageList.remove(multiMedia);
                    } else if (multiMedia.getType() == 1) {
                        videoList.remove(multiMedia);
                    }
                    ViewGroup parent = (ViewGroup) this.itemView.getParent();
                    parent.removeView(this.itemView);
                    checkLastImages();
                });

            }
        }

        /**
         * 加载图片
         */
        private void loadImage() {
            // 加载图片
            if (!TextUtils.isEmpty(multiMedia.getPath())) {
                imageEngine.loadThumbnail(getContext(), mpvImage.getWidth(), placeholder,
                        mpvImage, Uri.fromFile(new File(multiMedia.getPath())));
            } else if (!TextUtils.isEmpty(multiMedia.getUrl())) {
                imageEngine.loadUrlThumbnail(getContext(), mpvImage.getWidth(), placeholder,
                        mpvImage, multiMedia.getUrl());
            }
        }

        @Override
        public void onClick(View v) {
            if (listener != null)
                if (!TextUtils.isEmpty(multiMedia.getPath()) && multiMedia.getPath().equals(ADD)) {
                    // 加载➕图
                    listener.onItemAdd(v, multiMedia, imageList.size(), videoList.size(), maskProgressLayout.audioList.size());
                } else {
                    // 点击
                    if (multiMedia.getType() == 0){
                        // 如果是图片，直接跳转详情
                        listener.onItemImage(v, multiMedia);
                    }else{
                        // 如果是视频，判断是否已经下载好（有path就是已经下载好了）
                        if (TextUtils.isEmpty(multiMedia.getPath())){
                            // 执行下载事件
                            listener.onItemAudioStartDownload(multiMedia.getUrl());
                        }else{
                            // 点击事件
                            listener.onItemImage(v, multiMedia);
                        }
                    }
                }
        }

    }


}
