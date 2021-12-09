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
package com.zhongjh.albumcamerarecorder.preview.previewitem;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;


import com.zhongjh.albumcamerarecorder.R;

import com.zhongjh.albumcamerarecorder.album.utils.PhotoMetadataUtils;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;

import com.zhongjh.common.entity.MultiMedia;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

/**
 * @author zhongjh
 */
public class PreviewItemFragment extends Fragment {

    private static final String ARGS_ITEM = "args_item";

    View videoPlayButton;
    ImageViewTouch image;

    public static PreviewItemFragment newInstance(MultiMedia item) {
        PreviewItemFragment fragment = new PreviewItemFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARGS_ITEM, item);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_preview_item_zjh, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        videoPlayButton = view.findViewById(R.id.video_play_button);
        image = view.findViewById(R.id.image_view);
        init();
    }

    /**
     * 初始化，也可用于编辑图片后重新刷新当前界面
     */
    public void init() {
        final MultiMedia item = getArguments().getParcelable(ARGS_ITEM);
        if (item == null) {
            return;
        }
        if (item.isVideo()) {
            videoPlayButton.setVisibility(View.VISIBLE);
            videoPlayButton.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                // 申请权限
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri uri = null;
                if (item.getMediaUri() != null) {
                    uri = item.getMediaUri();
                }else if(item.getUri() != null) {
                    uri = item.getUri();
                }
                // 如果uri为null并且url有值，那就用播放器播放网址
                if (uri == null && !TextUtils.isEmpty(item.getUrl())) {
                    uri = Uri.parse(item.getUrl());
                }
                intent.setDataAndType(uri, "video/*");
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getContext(), R.string.z_multi_library_error_no_video_activity, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            videoPlayButton.setVisibility(View.GONE);
        }

        image.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);

        if (item.getMediaUri() != null) {
            Point size = PhotoMetadataUtils.getBitmapSize(item.getMediaUri(), getActivity());
            if (item.isGif()) {
                GlobalSpec.getInstance().imageEngine.loadGifImage(getContext(), size.x, size.y, image,
                        item.getMediaUri());
            } else {
                GlobalSpec.getInstance().imageEngine.loadImage(getContext(), size.x, size.y, image,
                        item.getMediaUri());

            }
        } else if (item.getUri() != null) {
            GlobalSpec.getInstance().imageEngine.loadUriImage(getContext(), image,
                    item.getUri());
        } else if (item.getUrl() != null) {
            GlobalSpec.getInstance().imageEngine.loadUrlImage(getContext(), image,
                    item.getUrl());
        } else if (item.getDrawableId() != -1) {
            GlobalSpec.getInstance().imageEngine.loadDrawableImage(getContext(), image,
                    item.getDrawableId());
        }
    }

    public void resetView() {
        if (getView() != null) {
            ((ImageViewTouch) getView().findViewById(R.id.image_view)).resetMatrix();
        }
    }

}
