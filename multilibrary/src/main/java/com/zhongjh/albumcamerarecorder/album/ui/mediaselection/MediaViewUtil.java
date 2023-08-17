package com.zhongjh.albumcamerarecorder.album.ui.mediaselection;

import android.util.Log;
import android.widget.ImageView;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.album.entity.Album2;
import com.zhongjh.common.entity.LocalMedia;
import com.zhongjh.albumcamerarecorder.album.listener.OnLoadPageMediaDataListener;
import com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection;
import com.zhongjh.albumcamerarecorder.album.ui.main.MainModel;
import com.zhongjh.albumcamerarecorder.album.ui.mediaselection.adapter.AlbumMediaAdapter;
import com.zhongjh.albumcamerarecorder.album.utils.UiUtils;
import com.zhongjh.albumcamerarecorder.album.ui.mediaselection.adapter.widget.MediaGridInset;
import com.zhongjh.albumcamerarecorder.settings.AlbumSpec;
import com.zhongjh.common.entity.MultiMedia;

import java.util.List;

/**
 * 以前是MediaSelectionFragment,现在为了能滑动影响上下布局，放弃Fragment布局，直接使用RecyclerView
 * Fragment嵌套RecyclerView的话，会在配置小的机器下产生性能卡顿问题
 *
 * @author zhongjh
 * @date 2022/9/19
 */
public class MediaViewUtil implements
        AlbumMediaAdapter.CheckStateListener, AlbumMediaAdapter.OnMediaClickListener {

    public MediaViewUtil(FragmentActivity activity,
                         MainModel mainModel,
                         RecyclerView recyclerView,
                         AlbumMediaAdapter.CheckStateListener checkStateListener,
                         AlbumMediaAdapter.OnMediaClickListener onMediaClickListener) {
        mActivity = activity;
        mMainModel = mainModel;
        mRecyclerView = recyclerView;
        mCheckStateListener = checkStateListener;
        mOnMediaClickListener = onMediaClickListener;
        init();
    }

    private final FragmentActivity mActivity;
    private final MainModel mMainModel;
    private final RecyclerView mRecyclerView;
    private AlbumMediaAdapter mAdapter;
    private Album2 mAlbum;
    private AlbumSpec mAlbumSpec;
    /**
     * 分页相册的当前页码
     */
    private int mPage = 0;
    /**
     * 单选事件
     */
    private AlbumMediaAdapter.CheckStateListener mCheckStateListener;
    /**
     * 点击事件
     */
    private AlbumMediaAdapter.OnMediaClickListener mOnMediaClickListener;

    private void init() {
        // 先设置recyclerView的布局
        int spanCount;
        mAlbumSpec = AlbumSpec.INSTANCE;
        if (mAlbumSpec.getGridExpectedSize() > 0) {
            spanCount = UiUtils.spanCount(mActivity.getApplicationContext(), mAlbumSpec.getGridExpectedSize());
        } else {
            spanCount = mAlbumSpec.getSpanCount();
        }
        // 删除动画
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity.getApplicationContext(), spanCount));
        // 需要先设置布局获取确定的spanCount，才能设置adapter
        mAdapter = new AlbumMediaAdapter(mActivity.getApplicationContext(),mMainModel, getImageResize());
        Log.d("onSaveInstanceState", " mAdapter");
        mAdapter.registerCheckStateListener(this);
        mAdapter.registerOnMediaClickListener(this);
        mAdapter.setHasStableIds(true);
        mRecyclerView.setHasFixedSize(true);

        // 加载线，recyclerView加载数据
        int spacing = mActivity.getResources().getDimensionPixelSize(R.dimen.z_media_grid_spacing);
        mRecyclerView.addItemDecoration(new MediaGridInset(spanCount, spacing, false));
        mRecyclerView.setAdapter(mAdapter);

        // 监听到新的相册数据
        mMainModel.getLocalMedias().observe(mActivity, new Observer<List<LocalMedia>>() {
            @Override
            public void onChanged(List<LocalMedia> localMedia) {
                mAdapter.setData(localMedia);
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
    public void restartLoaderMediaGrid() {
        mMainModel.loadPageMediaData(mAlbum.getId(), mPage, mAlbumSpec.getPageSize());
    }

    /**
     * 每次筛选后，重新查询数据
     *
     * @param album 专辑
     */
    public void load(Album2 album) {
        mAlbum = album;
        restartLoaderMediaGrid();
    }

    /**
     * 刷新数据源
     */
    public void refreshMediaGrid() {
        mAdapter.notifyItemRangeChanged(0, mAdapter.getItemCount());
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
        int screenWidth = mActivity.getApplicationContext().getResources().getDisplayMetrics().widthPixels;
        int availableWidth = screenWidth - mActivity.getApplicationContext().getResources().getDimensionPixelSize(
                R.dimen.z_media_grid_spacing) * (spanCount - 1);
        // 图片调整后的大小：获取列表的每个格子的宽度
        imageResize = availableWidth / spanCount;
        // 图片调整后的大小 * 缩放比例
        imageResize = (int) (imageResize * AlbumSpec.INSTANCE.getThumbnailScale());
        return imageResize;
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
     * 点击接口
     */
    public interface SelectionProvider {
        /**
         * 用于获取当前选择的数据
         *
         * @return 当前选择的数据
         */
        SelectedItemCollection provideSelectedItemCollection();
    }
}
