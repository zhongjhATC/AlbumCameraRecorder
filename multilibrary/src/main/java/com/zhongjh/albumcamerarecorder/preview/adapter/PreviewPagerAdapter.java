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
package com.zhongjh.albumcamerarecorder.preview.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import android.util.SparseArray;
import android.view.ViewGroup;


import com.zhongjh.albumcamerarecorder.preview.previewitem.PreviewItemFragment;

import java.util.ArrayList;
import java.util.List;

import gaode.zhongjh.com.common.entity.MultiMedia;

/**
 * @author zhongjh
 */
public class PreviewPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<MultiMedia> mItems = new ArrayList<>();
    private SparseArray<Fragment> fragments;
    private OnPrimaryItemSetListener mListener;

    public PreviewPagerAdapter(@NonNull FragmentManager fm, int behavior, OnPrimaryItemSetListener listener) {
        super(fm, behavior);
        mListener = listener;
        fragments = new SparseArray<>(getCount());
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        fragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        fragments.remove(position);
    }

    public Fragment getFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public Fragment getItem(int position) {
        return PreviewItemFragment.newInstance(mItems.get(position));
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        if (mListener != null) {
            mListener.onPrimaryItemSet(position);
        }
    }

    public MultiMedia getMediaItem(int position) {
        return mItems.get(position);
    }

    public void setMediaItem(int position, MultiMedia multiMedia) {
        mItems.set(position, multiMedia);
    }

    public void addAll(List<MultiMedia> items) {
        mItems.addAll(items);
    }

    public ArrayList<MultiMedia> getmItems() {
        return mItems;
    }

    interface OnPrimaryItemSetListener {

        void onPrimaryItemSet(int position);
    }

}
