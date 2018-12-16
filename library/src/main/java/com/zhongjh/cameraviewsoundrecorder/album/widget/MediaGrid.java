package com.zhongjh.cameraviewsoundrecorder.album.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhongjh.cameraviewsoundrecorder.R;
import com.zhongjh.cameraviewsoundrecorder.album.entity.Item;
import com.zhongjh.cameraviewsoundrecorder.settings.SelectionSpec;

public class MediaGrid extends SquareFrameLayout implements View.OnClickListener {

    private ImageView mThumbnail;
    private CheckView mCheckView; // 选择控件
    private ImageView mGifTag;// gif标志图片
    private TextView mVideoDuration; // 文本的时长（类似指视频的时长）

    private Item mMedia;// 值
    private PreBindInfo mPreBindInfo; // 控件和一些别的变量
    private OnMediaGridClickListener mListener; // 事件
    private boolean checked;

    public MediaGrid(Context context) {
        super(context);
        init(context);
    }

    public MediaGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.media_grid_content, this, true);

        mThumbnail = findViewById(R.id.media_thumbnail);
        mCheckView = findViewById(R.id.check_view);
        mGifTag = findViewById(R.id.gif);
        mVideoDuration = findViewById(R.id.video_duration);

        mThumbnail.setOnClickListener(this);
        mCheckView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (mListener != null) {
            if (view == mThumbnail) {
                // 图片的点击事件
                mListener.onThumbnailClicked(mThumbnail, mMedia, mPreBindInfo.mViewHolder);
            } else if (view == mCheckView) {
                // 勾选的点击事件
                mListener.onCheckViewClicked(mCheckView, mMedia, mPreBindInfo.mViewHolder);
            }
        }
    }

    public void preBindMedia(PreBindInfo info) {
        mPreBindInfo = info;
    }

    /**
     * 绑定值
     * @param item 值
     */
    public void bindMedia(Item item) {
        mMedia = item;
        setGifTag();
        initCheckView();
        setImage();
        setVideoDuration();
    }

    /**
     * 根据gif判断是否显示gif标志
     */
    private void setGifTag() {
        mGifTag.setVisibility(mMedia.isGif() ? View.VISIBLE : View.GONE);
    }

    /**
     * 设置是否多选
     */
    private void initCheckView() {
        mCheckView.setCountable(mPreBindInfo.mCheckViewCountable);
    }

    /**
     * 设置是否启用
     * @param enabled 启用
     */
    public void setCheckEnabled(boolean enabled) {
            mCheckView.setEnabled(enabled);
    }

    /**
     * 设置当前选择的第级个
     * @param checkedNum 数量
     */
    public void setCheckedNum(int checkedNum) {
        mCheckView.setCheckedNum(checkedNum);
    }

    /**
     * 设置当前的单选框为选择
     * @param checked 是否选择
     */
    public void setChecked(boolean checked) {
        mCheckView.setChecked(checked);
    }


    /**
     * 设置图片或者gif图片
     */
    private void setImage() {
        if (mMedia.isGif()) {
            SelectionSpec.getInstance().imageEngine.loadGifThumbnail(getContext(), mPreBindInfo.mResize,
                    mPreBindInfo.mPlaceholder, mThumbnail, mMedia.getContentUri());
        } else {
            SelectionSpec.getInstance().imageEngine.loadThumbnail(getContext(), mPreBindInfo.mResize,
                    mPreBindInfo.mPlaceholder, mThumbnail, mMedia.getContentUri());
        }
    }

    /**
     * 设置文本的时长（类似指视频的时长）
     */
    private void setVideoDuration() {
        if (mMedia.isVideo()) {
            mVideoDuration.setVisibility(VISIBLE);
            mVideoDuration.setText(DateUtils.formatElapsedTime(mMedia.duration / 1000));
        } else {
            mVideoDuration.setVisibility(GONE);
        }
    }

    public void setOnMediaGridClickListener(OnMediaGridClickListener listener) {
        mListener = listener;
    }




    public interface OnMediaGridClickListener {

        void onThumbnailClicked(ImageView thumbnail, Item item, RecyclerView.ViewHolder holder);

        void onCheckViewClicked(CheckView checkView, Item item, RecyclerView.ViewHolder holder);

    }

    public static class PreBindInfo {
        int mResize;
        Drawable mPlaceholder;
        boolean mCheckViewCountable;
        RecyclerView.ViewHolder mViewHolder;

        public PreBindInfo(int resize, Drawable placeholder, boolean checkViewCountable,
                           RecyclerView.ViewHolder viewHolder) {
            mResize = resize;
            mPlaceholder = placeholder;
            mCheckViewCountable = checkViewCountable;
            mViewHolder = viewHolder;
        }
    }

}
