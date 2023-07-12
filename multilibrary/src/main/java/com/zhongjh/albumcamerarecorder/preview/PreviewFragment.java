package com.zhongjh.albumcamerarecorder.preview;

import static com.zhongjh.albumcamerarecorder.album.model.AlbumMediaCollection.LOADER_PREVIEW_ID;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zhongjh.albumcamerarecorder.MainActivity;
import com.zhongjh.albumcamerarecorder.album.entity.Album;
import com.zhongjh.albumcamerarecorder.album.entity.Album2;
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
    private boolean mIsAlreadySetPosition = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mCollection.onCreate(mActivity, this);
        if (getArguments() != null) {
            Album2 album = getArguments().getParcelable(EXTRA_ALBUM);
            ArrayList<MultiMedia> items = null;
            if (album != null) {
                // 如果有专辑，则根据专辑加载数据
                mCollection.load(album, LOADER_PREVIEW_ID);
            } else {
                // 如果没有专辑，就取决于来自与上个界面提供的数据
                Bundle bundle = getArguments().getBundle(EXTRA_DEFAULT_BUNDLE);
                items = bundle.getParcelableArrayList(SelectedItemCollection.STATE_SELECTION);
                initItems(items);
            }

            MultiMedia item = getArguments().getParcelable(EXTRA_ITEM);
            if (item != null) {
                // 如果有当前数据，则跳转到当前数据索引
                if (mAlbumSpec.getCountable()) {
                    int selectedIndex = mSelectedCollection.checkedNumOf(item);
                    // 索引需要减1
                    mPreviousPos = selectedIndex - 1;
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
        // 如果依附的Activity是MainActivity,就显示底部控件动画
        if (mActivity instanceof MainActivity) {
            ((MainActivity) mActivity).showHideTableLayoutAnimator(true);
        }
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
            adapter.notifyItemRangeInserted(0, items.size() - 1);
            if (!mIsAlreadySetPosition) {
                // onAlbumMediaLoad is called many times..
                mIsAlreadySetPosition = true;
                if (getArguments() != null) {
                    MultiMedia selected = getArguments().getParcelable(EXTRA_ITEM);
                    if (selected != null) {
                        // 减1是爲了拿到索引
                        int selectedIndex = MultiMedia.checkedNumOf(items, selected) - 1;
                        mViewHolder.pager.setCurrentItem(selectedIndex, false);
                        mPreviousPos = selectedIndex;
                    }
                }
            }

        }
    }

    @Override
    public void onAlbumMediaReset() {

    }

}
