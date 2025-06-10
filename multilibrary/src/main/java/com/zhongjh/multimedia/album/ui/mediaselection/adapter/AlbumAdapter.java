package com.zhongjh.multimedia.album.ui.mediaselection.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zhongjh.multimedia.R;
import com.zhongjh.multimedia.album.entity.Album2;
import com.zhongjh.multimedia.album.ui.mediaselection.adapter.widget.MediaGrid;
import com.zhongjh.multimedia.album.widget.CheckView;
import com.zhongjh.multimedia.model.SelectedModel;
import com.zhongjh.multimedia.settings.AlbumSpec;
import com.zhongjh.common.entity.IncapableCause;
import com.zhongjh.common.entity.LocalMedia;

import java.util.ArrayList;
import java.util.List;

/**
 * 相册适配器
 *
 * @author zhongjh
 */
public class AlbumAdapter extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> implements
        MediaGrid.OnMediaGridClickListener {

    private final String TAG = AlbumAdapter.this.getClass().getSimpleName();
    private static final int VIEW_TYPE_MEDIA = 0x01;

    private final SelectedModel mSelectedModel;
    private final Drawable mPlaceholder;
    private final AlbumSpec mAlbumSpec;
    private final List<LocalMedia> data = new ArrayList<>();
    private CheckStateListener mCheckStateListener;
    private OnMediaClickListener mOnMediaClickListener;
    private final int mImageResize;

    public AlbumAdapter(Context context, SelectedModel selectedModel, int imageResize) {
        super();
        mAlbumSpec = AlbumSpec.INSTANCE;
        mSelectedModel = selectedModel;
        Log.d("onSaveInstanceState", mSelectedModel.getSelectedData().getLocalMedias().size() + " AlbumMediaAdapter");

        TypedArray ta = context.getTheme().obtainStyledAttributes(new int[]{R.attr.item_placeholder});
        mPlaceholder = ta.getDrawable(0);
        ta.recycle();

        mImageResize = imageResize;
    }

    public List<LocalMedia> getData() {
        return data;
    }

    /**
     * 重新赋值数据
     *
     * @param data 数据源
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<LocalMedia> data) {
        Log.d(TAG, "setData size:" + data.size());
        this.data.clear();
        this.data.addAll(data);
        notifyDataSetChanged();
    }

    /**
     * 添加数据
     *
     * @param data 数据源
     */
    public void addData(List<LocalMedia> data) {
        Log.d(TAG, "addData size:" + data.size());
        int positionStart = this.data.size();
        this.data.addAll(data);
        notifyItemInserted(positionStart);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 相片的item
        return new MediaViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.media_grid_item_zjh, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Log.d("onSaveInstanceState", mSelectedModel.getSelectedData().getLocalMedias().size() + " onBindViewHolder");
        // 相片的item
        MediaViewHolder mediaViewHolder = (MediaViewHolder) holder;

        LocalMedia item = this.data.get(position);
        Log.d(TAG, "position: " + position);
        if (position == 0) {
            Log.d(TAG, "path: " + item.getPath());
        }
        // 传递相关的值
        mediaViewHolder.mMediaGrid.preBindMedia(new MediaGrid.PreBindInfo(
                mImageResize,
                mPlaceholder,
                mAlbumSpec.getCountable(),
                holder
        ));

        mediaViewHolder.mMediaGrid.bindMedia(item);
        mediaViewHolder.mMediaGrid.setOnMediaGridClickListener(this);
        setCheckStatus(item, mediaViewHolder.mMediaGrid);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public long getItemId(int position) {
        // 需要返回id，否则不会重复调用onBindViewHolder，因为设置了mAdapter.setHasStableIds(true)
        return this.data.get(position).getFileId();
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "data.size(): " + data.size());
        return this.data.size();
    }

    /**
     * 设置当前选择状态
     *
     * @param item      数据
     * @param mediaGrid holder
     */
    private void setCheckStatus(LocalMedia item, MediaGrid mediaGrid) {
        Log.d("onSaveInstanceState", mSelectedModel.getSelectedData().getLocalMedias().size() + " setCheckStatus");
        // 是否多选时,显示数字
        if (mAlbumSpec.getCountable()) {
            // 显示数字
            int checkedNum = mSelectedModel.getSelectedData().checkedNumOf(item);
            if (checkedNum > 0) {
                // 设置启用,设置数量
                mediaGrid.setCheckEnabled(true);
                mediaGrid.setCheckedNum(checkedNum);
            } else {
                // 判断当前数量 和 当前选择最大数量比较 是否相等，相等就设置为false，否则true
                if (mSelectedModel.getSelectedData().maxSelectableReached()) {
                    mediaGrid.setCheckEnabled(false);
                    mediaGrid.setCheckedNum(CheckView.UNCHECKED);
                } else {
                    mediaGrid.setCheckEnabled(true);
                    mediaGrid.setCheckedNum(checkedNum);
                }
            }
        } else {
            // 不显示数字
            boolean selected = mSelectedModel.getSelectedData().isSelected(item);
            // 如果被选中了，就设置选择
            if (selected) {
                mediaGrid.setCheckEnabled(true);
                mediaGrid.setChecked(true);
            } else {
                // 判断当前数量 和 当前选择最大数量比较 是否相等，相等就设置为false，否则true
                mediaGrid.setCheckEnabled(!mSelectedModel.getSelectedData().maxSelectableReached());
                mediaGrid.setChecked(false);
            }
        }
    }

    @Override
    public void onThumbnailClicked(@NonNull ImageView imageView, @NonNull LocalMedia item, @NonNull RecyclerView.ViewHolder holder) {
        if (mOnMediaClickListener != null) {
            mOnMediaClickListener.onMediaClick(null, imageView, item, holder.getBindingAdapterPosition());
        }
    }

    @Override
    public void onCheckViewClicked(@NonNull CheckView checkView, @NonNull LocalMedia item, @NonNull RecyclerView.ViewHolder holder) {
        Log.d("onSaveInstanceState", mSelectedModel.getSelectedData().getLocalMedias().size() + " onCheckViewClicked");
        // 是否多选模式,显示数字
        if (mAlbumSpec.getCountable()) {
            // 获取当前选择的第几个
            int checkedNum = mSelectedModel.getSelectedData().checkedNumOf(item);
            if (checkedNum == CheckView.UNCHECKED) {
                // 如果当前数据是未选状态
                if (assertAddSelection(holder.itemView.getContext(), item)) {
                    // 添加选择了当前数据
                    mSelectedModel.addSelectedData(item);
                    // 刷新数据源
                    notifyCheckStateChanged();
                }
            } else {
                // 删除当前选择
                mSelectedModel.removeSelectedData(item);
                // 刷新数据
                notifyCheckStateChanged();
            }
        } else {
            // 不是多选模式
            if (mSelectedModel.getSelectedData().isSelected(item)) {
                // 如果当前已经被选中，再次选择就是取消了
                mSelectedModel.removeSelectedData(item);
                // 刷新数据源
                notifyCheckStateChanged();
            } else {
                if (assertAddSelection(holder.itemView.getContext(), item)) {
                    mSelectedModel.addSelectedData(item);
                    notifyCheckStateChanged();
                }
            }
        }
    }

    /**
     * 刷新数据
     */
    @SuppressLint("NotifyDataSetChanged")
    public void notifyCheckStateChanged() {
        notifyDataSetChanged();
        if (mCheckStateListener != null) {
            mCheckStateListener.onUpdate();
        }
    }

    /**
     * 验证当前item是否满足可以被选中的条件
     *
     * @param context 上下文
     * @param item    数据源
     */
    private boolean assertAddSelection(Context context, LocalMedia item) {
        IncapableCause cause = mSelectedModel.getSelectedData().isAcceptable(item);
        IncapableCause.handleCause(context, cause);
        return cause == null;
    }

    /**
     * 注册选择事件
     *
     * @param listener 事件
     */
    public void registerCheckStateListener(CheckStateListener listener) {
        mCheckStateListener = listener;
    }

    /**
     * 注销选择事件
     */
    public void unregisterCheckStateListener() {
        mCheckStateListener = null;
    }

    /**
     * 注册图片点击事件
     *
     * @param listener 事件
     */
    public void registerOnMediaClickListener(OnMediaClickListener listener) {
        mOnMediaClickListener = listener;
    }

    /**
     * 注销图片点击事件
     */
    public void unregisterOnMediaClickListener() {
        mOnMediaClickListener = null;
    }

    public interface CheckStateListener {
        /**
         * 选择选项后更新事件
         */
        void onUpdate();
    }

    public interface OnMediaClickListener {
        /**
         * 点击事件
         *
         * @param album           相册集合
         * @param imageView       图片View
         * @param item            选项
         * @param adapterPosition 索引
         */
        void onMediaClick(Album2 album, ImageView imageView, LocalMedia item, int adapterPosition);
    }

    private static class MediaViewHolder extends RecyclerView.ViewHolder {

        private final MediaGrid mMediaGrid;

        MediaViewHolder(View itemView) {
            super(itemView);
            mMediaGrid = (MediaGrid) itemView;
        }
    }

}
