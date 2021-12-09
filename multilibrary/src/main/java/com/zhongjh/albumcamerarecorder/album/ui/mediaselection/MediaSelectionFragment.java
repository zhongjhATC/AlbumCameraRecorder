package com.zhongjh.albumcamerarecorder.album.ui.mediaselection;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.album.MatissFragment;

import com.zhongjh.albumcamerarecorder.album.entity.Album;
import com.zhongjh.albumcamerarecorder.album.utils.UiUtils;
import com.zhongjh.albumcamerarecorder.settings.AlbumSpec;
import com.zhongjh.albumcamerarecorder.album.model.AlbumMediaCollection;
import com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection;
import com.zhongjh.albumcamerarecorder.album.ui.mediaselection.adapter.AlbumMediaAdapter;
import com.zhongjh.albumcamerarecorder.album.widget.MediaGridInset;
import com.zhongjh.common.utils.DisplayMetricsUtils;

import com.zhongjh.common.entity.MultiMedia;

/**
 * 相册 界面
 *
 * @author zhongjh
 * @date 2018/8/30
 */
public class MediaSelectionFragment extends Fragment implements
        AlbumMediaAdapter.CheckStateListener, AlbumMediaAdapter.OnMediaClickListener {

    /**
     * 专辑数据
     */
    private static final String EXTRA_ALBUM = "extra_album";

    private final AlbumMediaCollection mAlbumMediaCollection = new AlbumMediaCollection();
    private RecyclerView mRecyclerView;
    private FrameLayout mFlMain;
    private AlbumMediaAdapter mAdapter;
    /**
     * 选择接口事件
     */
    private SelectionProvider mSelectionProvider;
    /**
     * 单选事件
     */
    private AlbumMediaAdapter.CheckStateListener mCheckStateListener;
    /**
     * 点击事件
     */
    private AlbumMediaAdapter.OnMediaClickListener mOnMediaClickListener;

    /**
     * 实例化
     *
     * @param album 专辑
     */
    public static MediaSelectionFragment newInstance(Album album, int marginBottom) {
        MediaSelectionFragment fragment = new MediaSelectionFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_ALBUM, album);
        args.putInt(MatissFragment.ARGUMENTS_MARGIN_BOTTOM, marginBottom);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * 生命周期 onAttach() - onCreate() - onCreateView() - onActivityCreated()
     *
     * @param context 上下文
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        // 旧版的知乎是用Activity，而这边则使用Fragments的获取到MatissFragment
        Fragment matissFragment = null;
        if (getFragmentManager() != null) {
            for (Fragment fragment : getFragmentManager().getFragments()) {
                if (fragment instanceof MatissFragment) {
                    matissFragment = fragment;
                }
            }
        }
        if (matissFragment == null) {
            throw new IllegalStateException("matissFragment Cannot be null");
        }
        mSelectionProvider = (SelectionProvider) matissFragment;
        mCheckStateListener = (AlbumMediaAdapter.CheckStateListener) matissFragment;
        mOnMediaClickListener = (AlbumMediaAdapter.OnMediaClickListener) matissFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_media_selection_zjh, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = view.findViewById(R.id.recyclerview);
        mFlMain = view.findViewById(R.id.flMain);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Album album = null;
        if (getArguments() != null) {
            album = getArguments().getParcelable(EXTRA_ALBUM);
        }

        // 初始化底部间距
        int marginBottom = 0;
        if (getArguments() != null) {
            marginBottom = getArguments().getInt(MatissFragment.ARGUMENTS_MARGIN_BOTTOM);
        }
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mFlMain.getLayoutParams();
        layoutParams.bottomMargin = DisplayMetricsUtils.dip2px(marginBottom);

        // 实例化适配器并且传递数据源
        if (getContext() == null || getActivity() == null) {
            return;
        }
        mAdapter = new AlbumMediaAdapter(getContext(),
                mSelectionProvider.provideSelectedItemCollection(), mRecyclerView);
        mAdapter.registerCheckStateListener(this);
        mAdapter.registerOnMediaClickListener(this);
        mRecyclerView.setHasFixedSize(true);

        // 设置recyclerView的布局
        int spanCount;
        AlbumSpec albumSpec = AlbumSpec.getInstance();
        if (albumSpec.gridExpectedSize > 0) {
            spanCount = UiUtils.spanCount(getContext(), albumSpec.gridExpectedSize);
        } else {
            spanCount = albumSpec.spanCount;
        }
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount));

        // 加载线，recyclerView加载数据
        int spacing = getResources().getDimensionPixelSize(R.dimen.media_grid_spacing);
        mRecyclerView.addItemDecoration(new MediaGridInset(spanCount, spacing, false));
        mRecyclerView.setAdapter(mAdapter);
        mAlbumMediaCollection.onCreate(getActivity(), new AlbumMediaCollection.AlbumMediaCallbacks() {

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
        mAlbumMediaCollection.load(album);
    }

    public void onDestroyData() {
        mAlbumMediaCollection.onDestroy();
    }

    /**
     * 刷新数据源
     */
    public void refreshMediaGrid() {
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 重新获取数据源
     */
    public void restartLoaderMediaGrid() {
        Album album = null;
        if (getArguments() != null) {
            album = getArguments().getParcelable(EXTRA_ALBUM);
        }
        mAlbumMediaCollection.restartLoader(album);
    }

    /**
     * 刷新所能看到的选择
     */
    public void refreshSelection() {
        mAdapter.refreshSelection();
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
            if (getArguments() != null) {
                mOnMediaClickListener.onMediaClick(getArguments().getParcelable(EXTRA_ALBUM),
                        item, adapterPosition);
            }
        }
    }

    /**
     * 点击接口
     */
    public interface SelectionProvider {
        /**
         * 用于获取当前选择的数据
         * @return 当前选择的数据
         */
        SelectedItemCollection provideSelectedItemCollection();
    }

}
