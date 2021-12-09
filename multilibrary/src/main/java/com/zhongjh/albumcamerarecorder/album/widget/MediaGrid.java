package com.zhongjh.albumcamerarecorder.album.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;

import com.zhongjh.common.entity.MultiMedia;

/**
 * @author zhongjh
 */
public class MediaGrid extends SquareFrameLayout implements View.OnClickListener {

    private ImageView mThumbnail;
    /**
     * 选择控件
     */
    private CheckView mCheckView;
    /**
     * gif标志图片
     */
    private ImageView mGifTag;
    /**
     * 文本的时长（类似指视频的时长）
     */
    private TextView mVideoDuration;

    /**
     * 值
     */
    private MultiMedia mMedia;
    /**
     * 控件和一些别的变量
     */
    private PreBindInfo mPreBindInfo;
    /**
     * 事件
     */
    private OnMediaGridClickListener mListener;

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
        mCheckView = findViewById(R.id.checkView);
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
    public void bindMedia(MultiMedia item) {
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
     * 设置当前选择的第几个
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
            GlobalSpec.getInstance().imageEngine.loadGifThumbnail(getContext(), mPreBindInfo.mResize,
                    mPreBindInfo.mPlaceholder, mThumbnail, mMedia.getMediaUri());
        } else {
            GlobalSpec.getInstance().imageEngine.loadThumbnail(getContext(), mPreBindInfo.mResize,
                    mPreBindInfo.mPlaceholder, mThumbnail, mMedia.getMediaUri());
        }
    }

    /**
     * 设置文本的时长（类似指视频的时长）
     */
    private void setVideoDuration() {
        if (mMedia.isVideo()) {
            mVideoDuration.setVisibility(VISIBLE);
            mVideoDuration.setText(DateUtils.formatElapsedTime(mMedia.getDuration() / 1000));
        } else {
            mVideoDuration.setVisibility(GONE);
        }
    }

    public void setOnMediaGridClickListener(OnMediaGridClickListener listener) {
        mListener = listener;
    }

    public interface OnMediaGridClickListener {

        /**
         * 点击事件
         *
         * @param thumbnail 图片控件
         * @param item      数据
         * @param holder    控件
         */
        void onThumbnailClicked(ImageView thumbnail, MultiMedia item, RecyclerView.ViewHolder holder);

        /**
         * 选择事件
         *
         * @param checkView 选择控件
         * @param item      数据
         * @param holder    控件
         */
        void onCheckViewClicked(CheckView checkView, MultiMedia item, RecyclerView.ViewHolder holder);

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
