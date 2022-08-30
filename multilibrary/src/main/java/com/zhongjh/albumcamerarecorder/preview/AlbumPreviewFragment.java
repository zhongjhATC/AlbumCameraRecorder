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
import com.zhongjh.albumcamerarecorder.preview.base.BasePreviewFragment;
import com.zhongjh.common.entity.MultiMedia;

import java.util.ArrayList;
import java.util.List;

/**
 * 点击相册某个item 或者 点击九宫格进来
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
        if (getArguments() != null) {
            Album album = getArguments().getParcelable(EXTRA_ALBUM);
            ArrayList<MultiMedia> items = null;
            if (album != null) {
                mCollection.load(album);
            } else {
                Bundle bundle = getArguments().getBundle(EXTRA_DEFAULT_BUNDLE);
                items = bundle.getParcelableArrayList(SelectedItemCollection.STATE_SELECTION);
                initItems(items);
            }

            MultiMedia item = getArguments().getParcelable(EXTRA_ITEM);
            if (item != null) {
                if (mAlbumSpec.getCountable()) {
                    mViewHolder.checkView.setCheckedNum(mSelectedCollection.checkedNumOf(item));
                } else {
                    mViewHolder.checkView.setChecked(mSelectedCollection.isSelected(item));
                }
                updateUi(item);
            } else {
                if (items != null) {
                    if (mAlbumSpec.getCountable()) {
                        mViewHolder.checkView.setCheckedNum(1);
                    } else {
                        mViewHolder.checkView.setChecked(true);
                    }
                    mPreviousPos = 0;
                    updateUi(items.get(0));
                }
            }
        }
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
            if (getArguments() != null) {
                if (!mIsAlreadySetPosition) {
                    MultiMedia selected = getArguments().getParcelable(EXTRA_ITEM);
                    if (selected != null) {
                        // -1是爲了拿到索引
                        int selectedIndex = MultiMedia.checkedNumOf(items, selected) - 1;
                        mViewHolder.pager.setCurrentItem(selectedIndex, false);
                        mPreviousPos = selectedIndex;
                    }
                    // onAlbumMediaLoad is called many times..
                    mIsAlreadySetPosition = true;
                }
            }

        }
    }

    @Override
    public void onAlbumMediaReset() {

    }

}
