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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.chrisbanes.photoview.PhotoView;
import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.album.utils.PhotoMetadataUtils;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.common.entity.LocalMedia;
import com.zhongjh.common.entity.MultiMedia;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhongjh
 */
public class PreviewPagerAdapter extends RecyclerView.Adapter<PreviewPagerAdapter.PreviewViewHolder> {

    private final Context mContext;
    private final Activity mActivity;
    private final ArrayList<LocalMedia> mItems = new ArrayList<>();

    public PreviewPagerAdapter(Context context, Activity activity) {
        mContext = context;
        mActivity = activity;
    }

    @NonNull
    @Override
    public PreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PreviewViewHolder(LayoutInflater.from(mActivity).inflate(R.layout.fragment_preview_item_zjh, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PreviewViewHolder holder, int position) {
        LocalMedia item = mItems.get(position);
        if (item.isVideo()) {
            holder.videoPlayButton.setVisibility(View.VISIBLE);
            holder.videoPlayButton.setOnClickListener(v -> {
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                // 申请权限
//                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                Uri uri = null;
//                if (item.getUri() != null) {
//                    uri = item.getUri();
//                }
//                // 如果uri为null并且url有值，那就用播放器播放网址
//                if (uri == null && !TextUtils.isEmpty(item.getUrl())) {
//                    uri = Uri.parse(item.getUrl());
//                }
//                intent.setDataAndType(uri, "video/*");
//                try {
//                    mActivity.startActivity(intent);
//                } catch (ActivityNotFoundException e) {
//                    Toast.makeText(mContext, R.string.z_multi_library_error_no_video_activity, Toast.LENGTH_SHORT).show();
//                }
            });
        } else {
            holder.videoPlayButton.setVisibility(View.GONE);
        }


        GlobalSpec.INSTANCE.getImageEngine().loadUrlImage(mContext, holder.imageView,
                item.getPath());

//        if (item.getUri() != null) {
//            Point size = PhotoMetadataUtils.getBitmapSize(item.getUri(), mActivity);
//            if (item.isGif()) {
//                GlobalSpec.INSTANCE.getImageEngine().loadGifImage(mContext, size.x, size.y, holder.imageView,
//                        item.getUri());
//            } else {
//                GlobalSpec.INSTANCE.getImageEngine().loadImage(mContext, size.x, size.y, holder.imageView,
//                        item.getUri());
//
//            }
//        } else if (item.getUrl() != null) {
//            GlobalSpec.INSTANCE.getImageEngine().loadUrlImage(mContext, holder.imageView,
//                    item.getUrl());
//        } else if (item.getDrawableId() != -1) {
//            GlobalSpec.INSTANCE.getImageEngine().loadDrawableImage(mContext, holder.imageView,
//                    item.getDrawableId());
//        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public int getSize() {
        return mItems.size();
    }

    public LocalMedia getLocalMedia(int position) {
        return getSize() > 0 && position < getSize() ? mItems.get(position) : null;
    }

    public void setMediaItem(int position, LocalMedia localMedia) {
        mItems.set(position, localMedia);
    }

    public void addAll(List<LocalMedia> items) {
        mItems.addAll(items);
    }

    public ArrayList<LocalMedia> getItems() {
        return mItems;
    }

    static class PreviewViewHolder extends RecyclerView.ViewHolder {

        View videoPlayButton;
        PhotoView imageView;

        PreviewViewHolder(View itemView) {
            super(itemView);
            videoPlayButton = itemView.findViewById(R.id.video_play_button);
            imageView = itemView.findViewById(R.id.image_view);
        }
    }

}
