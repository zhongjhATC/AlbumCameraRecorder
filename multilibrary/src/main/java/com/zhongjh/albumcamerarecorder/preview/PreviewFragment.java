package com.zhongjh.albumcamerarecorder.preview;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zhongjh.albumcamerarecorder.MainActivity;
import com.zhongjh.albumcamerarecorder.album.entity.Album2;
import com.zhongjh.albumcamerarecorder.preview.adapter.PreviewPagerAdapter;
import com.zhongjh.albumcamerarecorder.preview.base.BasePreviewFragment;
import com.zhongjh.albumcamerarecorder.utils.LocalMediaUtils;
import com.zhongjh.common.entity.LocalMedia;

import java.util.ArrayList;

/**
 * 预览的Fragment
 *
 * @author zhongjh
 * @date 2022/7/26
 */
public class PreviewFragment extends BasePreviewFragment {

    private final String TAG = PreviewFragment.this.getClass().getSimpleName();

    public static final String EXTRA_ALBUM = "extra_album";
    public static final String EXTRA_ITEM = "extra_item";
    private boolean mIsAlreadySetPosition = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (getArguments() != null) {
            Album2 album = getArguments().getParcelable(EXTRA_ALBUM);
            ArrayList<LocalMedia> items = null;

            LocalMedia item = getArguments().getParcelable(EXTRA_ITEM);
            if (item != null) {
//                // 如果有当前数据，则跳转到当前数据索引
//                if (mAlbumSpec.getCountable()) {
//                    int selectedIndex = mMainModel.getSelectedData().checkedNumOf(item);
//                    // 索引需要减1
//                    mPreviousPos = selectedIndex - 1;
//                    mViewHolder.checkView.setCheckedNum(selectedIndex);
//                } else {
//                    mViewHolder.checkView.setChecked(mMainModel.getSelectedData().isSelected(item));
//                }
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

        initLocalMedias(mMainModel.getLocalMedias());
        return view;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        // 如果依附的Activity是MainActivity,就显示底部控件动画
        if (mActivity instanceof MainActivity) {
            ((MainActivity) mActivity).showHideTableLayoutAnimator(true);
        }
        super.onDestroy();
    }

    private void initLocalMedias(ArrayList<LocalMedia> localMediaArrayList) {
        PreviewPagerAdapter adapter = (PreviewPagerAdapter) mViewHolder.pager.getAdapter();
        if (adapter != null) {
            adapter.addAll(localMediaArrayList);
            adapter.notifyItemRangeInserted(0, localMediaArrayList.size() - 1);
            if (!mIsAlreadySetPosition) {
                // onAlbumMediaLoad is called many times..
                mIsAlreadySetPosition = true;
                if (getArguments() != null) {
                    LocalMedia selected = getArguments().getParcelable(EXTRA_ITEM);
                    if (selected != null) {
                        // 减1是爲了拿到索引
                        int selectedIndex = LocalMediaUtils.checkedNumOf(localMediaArrayList, selected) - 1;
                        mViewHolder.pager.setCurrentItem(selectedIndex, false);
                        mPreviousPos = selectedIndex;
                    }
                }
            }

        }
    }

    public void onAlbumMediaReset() {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");
    }

}
