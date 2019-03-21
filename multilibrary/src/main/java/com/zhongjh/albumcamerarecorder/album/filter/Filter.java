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
package com.zhongjh.albumcamerarecorder.album.filter;

import android.content.Context;

import gaode.zhongjh.com.common.entity.IncapableCause;
import gaode.zhongjh.com.common.entity.MultiMedia;
import gaode.zhongjh.com.common.enums.MimeType;

import java.util.Set;

/**
 * Filter for choosing a {@link Item}. You can add multiple Filters through
 */
@SuppressWarnings("unused")
public abstract class Filter {
    /**
     * Convenient constant for a minimum value.
     */
    public static final int MIN = 0;
    /**
     * Convenient constant for a maximum value.
     */
    public static final int MAX = Integer.MAX_VALUE;
    /**
     * Convenient constant for 1024.
     */
    public static final int K = 1024;

    /**
     * Against what mime types this filter applies.
     */
    protected abstract Set<MimeType> constraintTypes();

    /**
     * Invoked for filtering each item.
     *
     * @return null if selectable, {@link IncapableCause} if not selectable.
     * 调用以过滤每个项。
     */
    public abstract IncapableCause filter(Context context, MultiMedia item);

    /**
     * Whether an {@link MultiMedia} need filtering.
     */
    protected boolean needFiltering(Context context, MultiMedia item) {
        for (MimeType type : constraintTypes()) {
            if (type.checkType(context.getContentResolver(), item.getMediaUri())) {
                return true;
            }
        }
        return false;
    }
}
