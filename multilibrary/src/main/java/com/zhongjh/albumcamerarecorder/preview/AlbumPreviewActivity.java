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
package com.zhongjh.albumcamerarecorder.preview;

import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.Nullable;


import com.zhongjh.albumcamerarecorder.album.entity.Album;
import com.zhongjh.albumcamerarecorder.album.model.AlbumMediaCollection;
import com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection;
import com.zhongjh.albumcamerarecorder.preview.adapter.PreviewPagerAdapter;
import com.zhongjh.albumcamerarecorder.utils.MultiMediaUtils;

import java.util.ArrayList;
import java.util.List;

import com.zhongjh.common.entity.MultiMedia;

/**
 * 点击相册图片或者视频进来的
 * @author zhongjh
 */
public class AlbumPreviewActivity extends BasePreviewActivity implements
        AlbumMediaCollection.AlbumMediaCallbacks {

    public static final String EXTRA_ALBUM = "extra_album";
    public static final String EXTRA_ITEM = "extra_item";

    private final AlbumMediaCollection mCollection = new AlbumMediaCollection();

    private boolean mIsAlreadySetPosition;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCollection.onCreate(this, this);
        Album album = getIntent().getParcelableExtra(EXTRA_ALBUM);
        if (album != null) {
            mCollection.load(album);
        } else {
            Bundle bundle = getIntent().getBundleExtra(EXTRA_DEFAULT_BUNDLE);
            ArrayList<MultiMedia> items = bundle.getParcelableArrayList(SelectedItemCollection.STATE_SELECTION);
            initItems(items);
        }

        MultiMedia item = getIntent().getParcelableExtra(EXTRA_ITEM);
        if (mAlbumSpec.countable) {
            mViewHolder.checkView.setCheckedNum(mSelectedCollection.checkedNumOf(item));
        } else {
            mViewHolder.checkView.setChecked(mSelectedCollection.isSelected(item));
        }
        updateSize(item);
    }

    @Override
    protected void onDestroy() {
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
            if (!mIsAlreadySetPosition) {
                // onAlbumMediaLoad is called many times..
                mIsAlreadySetPosition = true;
                MultiMedia selected = getIntent().getParcelableExtra(EXTRA_ITEM);
                // -1是爲了拿到索引
                int selectedIndex = MultiMediaUtils.checkedNumOf(items, selected) - 1;
                mViewHolder.pager.setCurrentItem(selectedIndex, false);
                mPreviousPos = selectedIndex;
            }
        }
    }

    @Override
    public void onAlbumMediaReset() {

    }
}
