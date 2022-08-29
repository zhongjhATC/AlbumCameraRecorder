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

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.github.chrisbanes.photoview.PhotoView;
import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.album.utils.PhotoMetadataUtils;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.common.entity.MultiMedia;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhongjh
 */
public class PreviewPagerAdapter extends PagerAdapter {

    /**
     * 缓存view的最大数量
     */
    private static final int MAX_CACHE_SIZE = 20;
    private final Context mContext;
    private final Activity mActivity;
    private final ArrayList<MultiMedia> mItems = new ArrayList<>();
    /**
     * 要缓存的view
     */
    private SparseArray<View> mCacheView = new SparseArray<>();

    public PreviewPagerAdapter(Context context, Activity activity) {
        mContext = context;
        mActivity = activity;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NotNull
    @Override
    public Object instantiateItem(@NotNull ViewGroup container, int position) {
        View contentView = mCacheView.get(position);
        if (contentView == null) {
            contentView = LayoutInflater.from(container.getContext())
                    .inflate(R.layout.fragment_preview_item_zjh, container, false);
            mCacheView.put(position, contentView);
        }
        init(contentView, getMediaItem(position));
        (container).addView(contentView, 0);
        return contentView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NotNull Object object) {
        container.removeView((View) object);
        if (mCacheView.size() > MAX_CACHE_SIZE) {
            mCacheView.remove(position);
        }
    }

    /**
     * 清除
     */
    public void destroy() {
        mCacheView.clear();
        mCacheView = null;
    }

    public int getSize() {
        return mItems.size();
    }

    public MultiMedia getMediaItem(int position) {
        return getSize() > 0 && position < getSize() ? mItems.get(position) : null;
    }

    public void setMediaItem(int position, MultiMedia multiMedia) {
        mItems.set(position, multiMedia);
    }

    public void addAll(List<MultiMedia> items) {
        mItems.addAll(items);
    }

    public ArrayList<MultiMedia> getItems() {
        return mItems;
    }

    /**
     * 刷新当前
     *
     * @param currentItem 当前position
     */
    public void currentItemInit(int currentItem) {
        View view = mCacheView.get(currentItem);
        if (view != null) {
            init(view, mItems.get(currentItem));
        }
    }

    /**
     * 初始化，也可用于编辑图片后重新刷新当前界面
     */
    public void init(View view, MultiMedia item) {
        View videoPlayButton = view.findViewById(R.id.video_play_button);
        PhotoView image = view.findViewById(R.id.image_view);
        if (item.isVideo()) {
            videoPlayButton.setVisibility(View.VISIBLE);
            videoPlayButton.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                // 申请权限
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri uri = null;
                if (item.getUri() != null) {
                    uri = item.getUri();
                }
                // 如果uri为null并且url有值，那就用播放器播放网址
                if (uri == null && !TextUtils.isEmpty(item.getUrl())) {
                    uri = Uri.parse(item.getUrl());
                }
                intent.setDataAndType(uri, "video/*");
                try {
                    mActivity.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(mContext, R.string.z_multi_library_error_no_video_activity, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            videoPlayButton.setVisibility(View.GONE);
        }

        if (item.getUri() != null) {
            Point size = PhotoMetadataUtils.getBitmapSize(item.getUri(), mActivity);
            if (item.isGif()) {
                GlobalSpec.INSTANCE.getImageEngine().loadGifImage(mContext, size.x, size.y, image,
                        item.getUri());
            } else {
                GlobalSpec.INSTANCE.getImageEngine().loadImage(mContext, size.x, size.y, image,
                        item.getUri());

            }
        } else if (item.getUrl() != null) {
            GlobalSpec.INSTANCE.getImageEngine().loadUrlImage(mContext, image,
                    item.getUrl());
        } else if (item.getDrawableId() != -1) {
            GlobalSpec.INSTANCE.getImageEngine().loadDrawableImage(mContext, image,
                    item.getDrawableId());
        }
    }

}
