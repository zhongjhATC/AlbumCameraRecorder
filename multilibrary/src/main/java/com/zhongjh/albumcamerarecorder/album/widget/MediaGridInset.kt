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
package com.zhongjh.albumcamerarecorder.album.widget;

import android.graphics.Rect;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 * 线
 * @author zhongjh
 */
public class MediaGridInset extends RecyclerView.ItemDecoration {

    private final int mSpanCount;
    private final int mSpacing;
    private final boolean mIncludeEdge;

    public MediaGridInset(int spanCount, int spacing, boolean includeEdge) {
        this.mSpanCount = spanCount;
        this.mSpacing = spacing;
        this.mIncludeEdge = includeEdge;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, RecyclerView parent,
                               @NonNull RecyclerView.State state) {
        // item position
        int position = parent.getChildAdapterPosition(view);
        // item column
        int column = position % mSpanCount;

        if (mIncludeEdge) {
            // spacing - column * ((1f / spanCount) * spacing)
            outRect.left = mSpacing - column * mSpacing / mSpanCount;
            // (column + 1) * ((1f / spanCount) * spacing)
            outRect.right = (column + 1) * mSpacing / mSpanCount;

            // top edge
            if (position < mSpanCount) {
                outRect.top = mSpacing;
            }
            // item bottom
            outRect.bottom = mSpacing;
        } else {
            // column * ((1f / spanCount) * spacing)
            outRect.left = column * mSpacing / mSpanCount;
            // spacing - (column + 1) * ((1f / spanCount) * spacing)
            outRect.right = mSpacing - (column + 1) * mSpacing / mSpanCount;
            if (position >= mSpanCount) {
                // item top
                outRect.top = mSpacing;
            }
        }
    }
}
