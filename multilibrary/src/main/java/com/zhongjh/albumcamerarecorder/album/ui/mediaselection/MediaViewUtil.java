package com.zhongjh.albumcamerarecorder.album.ui.mediaselection;

import android.database.Cursor;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.album.entity.Album;
import com.zhongjh.albumcamerarecorder.album.model.AlbumMediaCollection;
import com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection;
import com.zhongjh.albumcamerarecorder.album.ui.mediaselection.adapter.AlbumMediaAdapter;
import com.zhongjh.albumcamerarecorder.album.utils.UiUtils;
import com.zhongjh.albumcamerarecorder.album.widget.MediaGridInset;
import com.zhongjh.albumcamerarecorder.settings.AlbumSpec;
import com.zhongjh.common.entity.MultiMedia;

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
                         RecyclerView recyclerView,
                         SelectionProvider selectionProvider,
                         AlbumMediaAdapter.CheckStateListener checkStateListener,
                         AlbumMediaAdapter.OnMediaClickListener onMediaClickListener) {
        mActivity = activity;
        mRecyclerView = recyclerView;
        mSelectionProvider = selectionProvider;
        mCheckStateListener = checkStateListener;
        mOnMediaClickListener = onMediaClickListener;
        init();
    }

    private final FragmentActivity mActivity;
    private final AlbumMediaCollection mAlbumMediaCollection = new AlbumMediaCollection();
    private final RecyclerView mRecyclerView;
    private AlbumMediaAdapter mAdapter;
    private Album mAlbum;
    /**
     * 选择接口事件
     */
    private final SelectionProvider mSelectionProvider;
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
        AlbumSpec albumSpec = AlbumSpec.INSTANCE;
        if (albumSpec.getGridExpectedSize() > 0) {
            spanCount = UiUtils.spanCount(mActivity, albumSpec.getGridExpectedSize());
        } else {
            spanCount = albumSpec.getSpanCount();
        }
        // 删除动画
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity.getApplicationContext(), spanCount));
        // 需要先设置布局获取确定的spanCount，才能设置adapter
        mAdapter = new AlbumMediaAdapter(mActivity,
                mSelectionProvider.provideSelectedItemCollection(), getImageResize());
        Log.d("onSaveInstanceState"," mAdapter");
        mAdapter.registerCheckStateListener(this);
        mAdapter.registerOnMediaClickListener(this);
        mAdapter.setHasStableIds(true);
        mRecyclerView.setHasFixedSize(true);

        // 加载线，recyclerView加载数据
        int spacing = mActivity.getResources().getDimensionPixelSize(R.dimen.z_media_grid_spacing);
        mRecyclerView.addItemDecoration(new MediaGridInset(spanCount, spacing, false));
        mRecyclerView.setAdapter(mAdapter);


        mAlbumMediaCollection.onCreate(mActivity, new AlbumMediaCollection.AlbumMediaCallbacks() {

            /**
             * 加载数据完毕
             *
             * @param cursor 光标数据
             */
            @Override
            public void onAlbumMediaLoad(Cursor cursor) {
                mAdapter.swapCursor(cursor);
            }

            /**
             * 当一个已创建的加载器被重置从而使其数据无效时，此方法被调用
             */
            @Override
            public void onAlbumMediaReset() {
                // 此处是用于上面的onLoadFinished()的游标将被关闭时执行，我们需确保我们不再使用它
                mAdapter.swapCursor(null);
            }
        });
    }

    public void onDestroyView() {
        mAdapter.unregisterCheckStateListener();
        mAdapter.unregisterOnMediaClickListener();
        mCheckStateListener = null;
        mOnMediaClickListener = null;
        mAdapter = null;
        onDestroyData();
    }

    public void onDestroyData() {
        mAlbumMediaCollection.onDestroy();
    }

    /**
     * 每次筛选后，重新查询数据
     *
     * @param album 专辑
     */
    public void load(Album album) {
        mAlbum = album;
        mAlbumMediaCollection.restartLoader(mAlbum);
    }

    /**
     * 刷新数据源
     */
    public void refreshMediaGrid() {
        mAdapter.notifyItemRangeChanged(0, mAdapter.getItemCount());
    }

    /**
     * 重新获取数据源
     */
    public void restartLoaderMediaGrid() {
        mAlbumMediaCollection.restartLoader(mAlbum);
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
    public void onMediaClick(Album album, MultiMedia item, int adapterPosition) {
        if (mOnMediaClickListener != null) {
            mOnMediaClickListener.onMediaClick(mAlbum,
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
