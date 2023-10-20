package com.zhongjh.albumcamerarecorder.album.ui.mediaselection;

import android.util.Log;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.model.SelectedModel;
import com.zhongjh.albumcamerarecorder.album.entity.Album2;
import com.zhongjh.albumcamerarecorder.album.widget.recyclerview.RecyclerLoadMoreView;
import com.zhongjh.common.entity.LocalMedia;
import com.zhongjh.albumcamerarecorder.model.MainModel;
import com.zhongjh.albumcamerarecorder.album.ui.mediaselection.adapter.AlbumAdapter;
import com.zhongjh.albumcamerarecorder.album.utils.UiUtils;
import com.zhongjh.albumcamerarecorder.album.ui.mediaselection.adapter.widget.MediaGridInset;
import com.zhongjh.albumcamerarecorder.settings.AlbumSpec;

/**
 * 以前是MediaSelectionFragment,现在为了能滑动影响上下布局，放弃Fragment布局，直接使用RecyclerView
 * Fragment嵌套RecyclerView的话，会在配置小的机器下产生性能卡顿问题
 *
 * @author zhongjh
 * @date 2022/9/19
 */
public class MediaViewUtil implements
        AlbumAdapter.CheckStateListener, AlbumAdapter.OnMediaClickListener {

    public MediaViewUtil(FragmentActivity activity,
                         Fragment fragment,
                         MainModel mainModel,
                         SelectedModel selectedModel,
                         RecyclerLoadMoreView recyclerView,
                         AlbumAdapter.CheckStateListener checkStateListener,
                         AlbumAdapter.OnMediaClickListener onMediaClickListener) {
        mActivity = activity;
        mFragment = fragment;
        mMainModel = mainModel;
        mSelectedModel = selectedModel;
        mRecyclerView = recyclerView;
        mCheckStateListener = checkStateListener;
        mOnMediaClickListener = onMediaClickListener;
        init();
    }

    private final FragmentActivity mActivity;
    private final Fragment mFragment;
    private final MainModel mMainModel;
    private final SelectedModel mSelectedModel;
    private final RecyclerLoadMoreView mRecyclerView;
    private AlbumAdapter mAdapter;
    private Album2 mAlbum;
    private AlbumSpec mAlbumSpec;
    /**
     * 单选事件
     */
    private AlbumAdapter.CheckStateListener mCheckStateListener;
    /**
     * 点击事件
     */
    private AlbumAdapter.OnMediaClickListener mOnMediaClickListener;

    private void init() {
        // 先设置recyclerView的布局
        int spanCount;
        mAlbumSpec = AlbumSpec.INSTANCE;
        if (mAlbumSpec.getGridExpectedSize() > 0) {
            spanCount = UiUtils.spanCount(mActivity, mAlbumSpec.getGridExpectedSize());
        } else {
            spanCount = mAlbumSpec.getSpanCount();
        }
        // 删除动画
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, spanCount));
        // 需要先设置布局获取确定的spanCount，才能设置adapter
        mAdapter = new AlbumAdapter(mActivity, mSelectedModel, getImageResize());
        Log.d("onSaveInstanceState", " mAdapter");
        mAdapter.registerCheckStateListener(this);
        mAdapter.registerOnMediaClickListener(this);
        mAdapter.setHasStableIds(true);
        mRecyclerView.setHasFixedSize(true);

        // 加载线，recyclerView加载数据
        int spacing = mActivity.getResources().getDimensionPixelSize(R.dimen.z_media_grid_spacing);
        mRecyclerView.addItemDecoration(new MediaGridInset(spanCount, spacing, false));
        mRecyclerView.setAdapter(mAdapter);

        // 加载更多事件
        mRecyclerView.setOnRecyclerViewLoadMoreListener(() -> mMainModel.addAllPageMediaData(mAlbum.getId(), mAlbumSpec.getPageSize()));

        // 监听到新的相册数据
        mMainModel.getLocalMediaPages().observe(mFragment.getViewLifecycleOwner(), mediaData -> {
            // 如果没有数据，则关闭下拉加载
            mRecyclerView.setEnabledLoadMore(!mediaData.getData().isEmpty());
            if (mMainModel.getPage() == 1) {
                mAdapter.setData(mediaData.getData());
                mRecyclerView.scrollToPosition(0);
            } else {
                mAdapter.addData(mediaData.getData());
            }
        });
    }

    public void onDestroyView() {
        mAdapter.unregisterCheckStateListener();
        mAdapter.unregisterOnMediaClickListener();
        mCheckStateListener = null;
        mOnMediaClickListener = null;
        mAdapter = null;
    }

    /**
     * 重新获取数据源
     */
    public void reloadPageMediaData() {
        mMainModel.reloadPageMediaData(mAlbum.getId(), mAlbumSpec.getPageSize());
    }

    /**
     * 每次筛选后，重新查询数据
     *
     * @param album 专辑
     */
    public void load(Album2 album) {
        mAlbum = album;
        reloadPageMediaData();
    }

    /**
     * 刷新数据源
     */
    public void refreshMediaGrid() {
        mAdapter.notifyItemRangeChanged(0, mAdapter.getItemCount());
    }

    /**
     * 根据localMedia刷新列表数据
     *
     * @param localMedia 单个实体
     */
    public void notifyItemByLocalMedia(LocalMedia localMedia) {
        // 更新列表数据
        int position = mAdapter.getData().indexOf(localMedia);
        boolean isExistBySelected = mSelectedModel.getSelectedData().getLocalMedias().contains(localMedia);
        if (!isNotifyAll(isExistBySelected)) {
            mAdapter.notifyItemChanged(position);
        }
        // 更新除了列表其他地方
        if (mCheckStateListener != null) {
            mCheckStateListener.onUpdate();
        }
    }

    @Override
    public void onUpdate() {
        // 通知外部活动检查状态改变
        if (mCheckStateListener != null) {
            mCheckStateListener.onUpdate();
        }
    }

    @Override
    public void onMediaClick(Album2 album, ImageView imageView, LocalMedia item, int adapterPosition) {
        if (mOnMediaClickListener != null) {
            mOnMediaClickListener.onMediaClick(mAlbum, imageView,
                    item, adapterPosition);
        }
    }

    /**
     * 返回图片调整大小
     *
     * @return 列表的每个格子的宽度 * 缩放比例
     */
    private int getImageResize() {
        int imageResize;
        RecyclerView.LayoutManager lm = mRecyclerView.getLayoutManager();
        int spanCount = 0;
        if (lm != null) {
            spanCount = ((GridLayoutManager) lm).getSpanCount();
        }
        int screenWidth = mActivity.getResources().getDisplayMetrics().widthPixels;
        int availableWidth = screenWidth - mActivity.getResources().getDimensionPixelSize(
                R.dimen.z_media_grid_spacing) * (spanCount - 1);
        // 图片调整后的大小：获取列表的每个格子的宽度
        imageResize = availableWidth / spanCount;
        // 图片调整后的大小 * 缩放比例
        imageResize = (int) (imageResize * AlbumSpec.INSTANCE.getThumbnailScale());
        return imageResize;
    }

    /**
     * 检查通知策略
     *
     * @param isExistBySelected 选择的数据中是否存在
     * @return 是否通知所有
     */
    private boolean isNotifyAll(boolean isExistBySelected) {
        // 用于后续添加配置：如果选择满了判断配置是否使用蒙层
        return false;
    }
}
