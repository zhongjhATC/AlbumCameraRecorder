package com.zhongjh.albumcamerarecorder.preview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection;
import com.zhongjh.common.entity.MultiMedia;

import java.util.List;

/**
 * 点击相册的预览按钮进入的界面
 *
 * @author zhongjh
 * @date 2022/7/26
 */
public class SelectedPreviewFragment extends BasePreviewFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        Bundle bundle;
        if (getArguments() != null) {
            bundle = getArguments().getBundle(EXTRA_DEFAULT_BUNDLE);
            List<MultiMedia> selected = bundle.getParcelableArrayList(SelectedItemCollection.STATE_SELECTION);
            mAdapter.addAll(selected);
            mAdapter.notifyDataSetChanged();
            updateUi(selected.get(0));
        }
        if (mAlbumSpec.getCountable()) {
            mViewHolder.checkView.setCheckedNum(1);
        } else {
            mViewHolder.checkView.setChecked(true);
        }
        mPreviousPos = 0;
        return view;
    }
}
