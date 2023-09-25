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
package com.zhongjh.cameraapp.configuration;

import android.content.Context;

import com.zhongjh.albumcamerarecorder.album.filter.BaseFilter;
import com.zhongjh.common.entity.IncapableCause;
import com.zhongjh.common.entity.LocalMedia;
import com.zhongjh.common.enums.MimeType;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class GifSizeFilter extends BaseFilter {

    private final int mMinWidth;
    private final int mMinHeight;
    private final int mMaxSize;

    public GifSizeFilter(int minWidth, int minHeight, int maxSizeInBytes) {
        mMinWidth = minWidth;
        mMinHeight = minHeight;
        mMaxSize = maxSizeInBytes;
    }

    @Override
    public Set<MimeType> constraintTypes() {
        return new HashSet<MimeType>() {{
            add(MimeType.GIF);
        }};
    }

    @Override
    public IncapableCause filter(@NotNull Context context, @NotNull LocalMedia item) {
        if (!needFiltering(item)) {
            return null;
        }
        
// TODO 这个重构代码需要恢复注释，修改实体MultiMedia 改成local
//        Point size = PhotoMetadataUtils.getBitmapBound(context.getContentResolver(), item.getPath());
//        if (size.x < mMinWidth || size.y < mMinHeight || item.getSize() > mMaxSize) {
//            return new IncapableCause(IncapableCause.DIALOG, context.getString(R.string.error_gif, mMinWidth,
//                    String.valueOf(PhotoMetadataUtils.getSizeInMb(mMaxSize))));
//        }
        return null;
    }

}
