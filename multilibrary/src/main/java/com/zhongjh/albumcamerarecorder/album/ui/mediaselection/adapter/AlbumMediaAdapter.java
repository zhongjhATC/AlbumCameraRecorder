/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhongjh.albumcamerarecorder.album.ui.mediaselection.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.album.base.BaseRecyclerViewCursorAdapter;
import com.zhongjh.albumcamerarecorder.album.entity.Album;
import com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection;
import com.zhongjh.albumcamerarecorder.album.widget.CheckView;
import com.zhongjh.albumcamerarecorder.album.widget.MediaGrid;
import com.zhongjh.albumcamerarecorder.settings.AlbumSpec;
import com.zhongjh.common.entity.IncapableCause;
import com.zhongjh.common.entity.MultiMedia;

/**
 * 相册适配器
 * @author zhongjh
 */
public class AlbumMediaAdapter extends
        BaseRecyclerViewCursorAdapter<RecyclerView.ViewHolder> implements
        MediaGrid.OnMediaGridClickListener {

    private static final int VIEW_TYPE_MEDIA = 0x02;
    private final SelectedItemCollection mSelectedCollection;
    private final Drawable mPlaceholder;
    private final AlbumSpec mAlbumSpec;
    private CheckStateListener mCheckStateListener;
    private OnMediaClickListener mOnMediaClickListener;
    private final int mImageResize;

    public AlbumMediaAdapter(Context context, SelectedItemCollection selectedCollection, int imageResize) {
        super();
        mAlbumSpec = AlbumSpec.INSTANCE;
        mSelectedCollection = selectedCollection;
        Log.d("onSaveInstanceState",mSelectedCollection.asList().size() + " AlbumMediaAdapter");

        TypedArray ta = context.getTheme().obtainStyledAttributes(new int[]{R.attr.item_placeholder});
        mPlaceholder = ta.getDrawable(0);
        ta.recycle();

        mImageResize = imageResize;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 相片的item
        return new MediaViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.media_grid_item_zjh, parent, false));
    }


    @Override
    protected void onBindViewHolder(final RecyclerView.ViewHolder holder, Cursor cursor) {
        Log.d("onSaveInstanceState",mSelectedCollection.asList().size() + " onBindViewHolder");
        // 相片的item
        MediaViewHolder mediaViewHolder = (MediaViewHolder) holder;

        final MultiMedia item = MultiMedia.valueOf(cursor);
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

    /**
     * 设置当前选择状态
     *
     * @param item      数据
     * @param mediaGrid holder
     */
    private void setCheckStatus(MultiMedia item, MediaGrid mediaGrid) {
        Log.d("onSaveInstanceState",mSelectedCollection.asList().size() + " setCheckStatus");
        // 是否多选时,显示数字
        if (mAlbumSpec.getCountable()) {
            int checkedNum = mSelectedCollection.checkedNumOf(item);
            if (checkedNum > 0) {
                // 设置启用,设置数量
                mediaGrid.setCheckEnabled(true);
                mediaGrid.setCheckedNum(checkedNum);
            } else {
                // 判断当前数量 和 当前选择最大数量比较 是否相等，相等就设置为false，否则true
                if (mSelectedCollection.maxSelectableReached()) {
                    mediaGrid.setCheckEnabled(false);
                    mediaGrid.setCheckedNum(CheckView.UNCHECKED);
                } else {
                    mediaGrid.setCheckEnabled(true);
                    mediaGrid.setCheckedNum(checkedNum);
                }
            }
        } else {
            // 不显示字的情况
            boolean selected = mSelectedCollection.isSelected(item);
            // 如果被选中了，就设置选择
            if (selected) {
                mediaGrid.setCheckEnabled(true);
                mediaGrid.setChecked(true);
            } else {
                // 判断当前数量 和 当前选择最大数量比较 是否相等，相等就设置为false，否则true
                if (mSelectedCollection.maxSelectableReached()) {
                    // 设置为false
                    mediaGrid.setCheckEnabled(false);
                    mediaGrid.setChecked(false);
                } else {
                    // 设置为true
                    mediaGrid.setCheckEnabled(true);
                    mediaGrid.setChecked(false);
                }
            }
        }
    }


    @Override
    public void onThumbnailClicked(ImageView thumbnail, MultiMedia item, RecyclerView.ViewHolder holder) {
        if (mOnMediaClickListener != null) {
            mOnMediaClickListener.onMediaClick(null, item, holder.getBindingAdapterPosition());
        }
    }


    @Override
    public void onCheckViewClicked(CheckView checkView, MultiMedia item, RecyclerView.ViewHolder holder) {
        Log.d("onSaveInstanceState",mSelectedCollection.asList().size() + " onCheckViewClicked");
        // 是否多选模式,显示数字
        if (mAlbumSpec.getCountable()) {
            // 获取当前选择的第几个
            int checkedNum = mSelectedCollection.checkedNumOf(item);
            if (checkedNum == CheckView.UNCHECKED) {
                // 如果当前数据是未选状态
                if (assertAddSelection(holder.itemView.getContext(), item)) {
                    MediaViewHolder mediaViewHolder = (MediaViewHolder) holder;
                    mediaViewHolder.mMediaGrid.setSelectMaskVisibility(View.VISIBLE);
                    // 添加选择了当前数据
                    mSelectedCollection.add(item);
                    // 刷新数据源
                    notifyCheckStateChanged();
                }
            } else {
                MediaViewHolder mediaViewHolder = (MediaViewHolder) holder;
                mediaViewHolder.mMediaGrid.setSelectMaskVisibility(View.GONE);
                // 删除当前选择
                mSelectedCollection.remove(item);
                // 刷新数据
                notifyCheckStateChanged();
            }
        } else {
            // 不是多选模式
            if (mSelectedCollection.isSelected(item)) {
                // 如果当前已经被选中，再次选择就是取消了
                mSelectedCollection.remove(item);
                // 刷新数据源
                notifyCheckStateChanged();
            } else {
                if (assertAddSelection(holder.itemView.getContext(), item)) {
                    mSelectedCollection.add(item);
                    notifyCheckStateChanged();
                }
            }
        }
    }

    /**
     * 刷新数据
     */
    private void notifyCheckStateChanged() {
        notifyDataSetChanged();
        if (mCheckStateListener != null) {
            mCheckStateListener.onUpdate();
        }
    }

    /**
     * 返回类型
     *
     * @param position 索引
     * @param cursor   数据源
     */
    @Override
    public int getItemViewType(int position, Cursor cursor) {
        return VIEW_TYPE_MEDIA;
    }

    /**
     * 验证当前item是否满足可以被选中的条件
     *
     * @param context 上下文
     * @param item    数据源
     */
    private boolean assertAddSelection(Context context, MultiMedia item) {
        IncapableCause cause = mSelectedCollection.isAcceptable(item);
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
         * @param album 相册集合
         * @param item 选项
         * @param adapterPosition 索引
         */
        void onMediaClick(Album album, MultiMedia item, int adapterPosition);
    }

    private static class MediaViewHolder extends RecyclerView.ViewHolder {

        private final MediaGrid mMediaGrid;

        MediaViewHolder(View itemView) {
            super(itemView);
            mMediaGrid = (MediaGrid) itemView;
        }
    }

}
