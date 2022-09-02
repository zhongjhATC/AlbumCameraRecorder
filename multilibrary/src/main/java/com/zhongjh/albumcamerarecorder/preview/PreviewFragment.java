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
 * 预览的Fragment
 *
 * @author zhongjh
 * @date 2022/7/26
 */
public class PreviewFragment extends BasePreviewFragment implements
        AlbumMediaCollection.AlbumMediaCallbacks {

    public static final String EXTRA_ALBUM = "extra_album";
    public static final String EXTRA_ITEM = "extra_item";
    private final AlbumMediaCollection mCollection = new AlbumMediaCollection();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mCollection.onCreate(mActivity, this);
        if (getArguments() != null) {
            Album album = getArguments().getParcelable(EXTRA_ALBUM);
            ArrayList<MultiMedia> items = null;
            if (album != null) {
                // 如果有专辑，则根据专辑加载数据
                mCollection.load(album);
            } else {
                // 如果没有专辑，就取决于来自与上个界面提供的数据
                Bundle bundle = getArguments().getBundle(EXTRA_DEFAULT_BUNDLE);
                items = bundle.getParcelableArrayList(SelectedItemCollection.STATE_SELECTION);
                initItems(items);
            }

            MultiMedia item = getArguments().getParcelable(EXTRA_ITEM);
            if (item != null) {
                if (mAlbumSpec.getCountable()) {
                    int selectedIndex = mSelectedCollection.checkedNumOf(item);
                    // 索引需要-1
                    mPreviousPos = selectedIndex - 1;
                    mViewHolder.pager.setCurrentItem(mPreviousPos, false);
                    mViewHolder.checkView.setCheckedNum(selectedIndex);
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
        }
    }

    @Override
    public void onAlbumMediaReset() {

    }

}
