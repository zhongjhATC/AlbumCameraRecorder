package com.zhongjh.albumcamerarecorder.preview;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zhongjh.albumcamerarecorder.album.entity.Album;
import com.zhongjh.albumcamerarecorder.album.model.AlbumMediaCollection;
import com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection;
import com.zhongjh.albumcamerarecorder.preview.adapter.PreviewPagerAdapter;
import com.zhongjh.common.entity.MultiMedia;

import java.util.ArrayList;
import java.util.List;

/**
 * 点击相册图片或者视频、九宫格处都可以进来
 * 标记是以点击的当前item为准而显示，这个为什么使用数据库 AlbumMediaCollection 查询，而不使用上个界面传递过来的数据
 * 是因为用户可能点击的是未选择的数据
 *
 * @author zhongjh
 * @date 2022/7/26
 */
public class AlbumPreviewFragment extends BasePreviewFragment implements
        AlbumMediaCollection.AlbumMediaCallbacks {

    public static final String EXTRA_ALBUM = "extra_album";
    public static final String EXTRA_ITEM = "extra_item";

    private final AlbumMediaCollection mCollection = new AlbumMediaCollection();

    private boolean mIsAlreadySetPosition;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mCollection.onCreate(mActivity, this);
        Album album;
        if (getArguments() != null) {
            album = getArguments().getParcelable(EXTRA_ALBUM);
            if (album != null) {
                mCollection.load(album);
            } else {
                Bundle bundle = getArguments().getBundle(EXTRA_DEFAULT_BUNDLE);
                ArrayList<MultiMedia> items = bundle.getParcelableArrayList(SelectedItemCollection.STATE_SELECTION);
                initItems(items);
            }

            MultiMedia item = getArguments().getParcelable(EXTRA_ITEM);
            if (mAlbumSpec.getCountable()) {
                mViewHolder.checkView.setCheckedNum(mSelectedCollection.checkedNumOf(item));
            } else {
                mViewHolder.checkView.setChecked(mSelectedCollection.isSelected(item));
            }
            updateUi(item);
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mCollection.onDestroy();
    }

    @Override
    public void onAlbumMediaLoad(Cursor cursor) {
        List<MultiMedia> items = new ArrayList<>();
        while (cursor.moveToNext()) {
            items.add(MultiMedia.valueOf(cursor));
        }

        if (items.isEmpty()) {
            return;
        }

        initItems(items);
    }

    private void initItems(List<MultiMedia> items) {
        PreviewPagerAdapter adapter = (PreviewPagerAdapter) mViewHolder.pager.getAdapter();
        if (adapter != null) {
            adapter.addAll(items);
            adapter.notifyDataSetChanged();
            if (!mIsAlreadySetPosition) {
                // onAlbumMediaLoad is called many times..
                mIsAlreadySetPosition = true;
                MultiMedia selected = null;
                if (getArguments() != null) {
                    selected = getArguments().getParcelable(EXTRA_ITEM);
                }
                // -1是爲了拿到索引
                int selectedIndex = 0;
                if (selected != null) {
                    selectedIndex = MultiMedia.checkedNumOf(items, selected) - 1;
                }
                mViewHolder.pager.setCurrentItem(selectedIndex, false);
                mPreviousPos = selectedIndex;
            }
        }
    }

    @Override
    public void onAlbumMediaReset() {

    }
}
