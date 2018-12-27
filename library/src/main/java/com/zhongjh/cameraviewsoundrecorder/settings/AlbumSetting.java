package com.zhongjh.cameraviewsoundrecorder.settings;

import android.support.annotation.NonNull;

import com.zhongjh.cameraviewsoundrecorder.album.enums.MimeType;
import com.zhongjh.cameraviewsoundrecorder.album.filter.Filter;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by zhongjh on 2018/12/27.
 */

public class AlbumSetting {

    private final AlbumSpec mAlbumSpec;

    /**
     *
     * @param mimeTypes
     * @param mediaTypeExclusive 是否可以同时选择不同的资源类型 true表示不可以 false表示可以
     */
    public AlbumSetting(@NonNull Set<MimeType> mimeTypes, boolean mediaTypeExclusive) {
        mAlbumSpec = AlbumSpec.getInstance();
        mAlbumSpec.mimeTypeSet = mimeTypes;
        mAlbumSpec.mediaTypeExclusive = mediaTypeExclusive;
    }

    /**
     * Whether to show only one media type if choosing medias are only images or videos.
     *
     * @param showSingleMediaType whether to show only one media type, either images or videos.
     * @return {@link GlobalSetting} for fluent API.
     * @see AlbumSpec#onlyShowImages()
     * @see AlbumSpec#onlyShowVideos()
     */
    public AlbumSetting showSingleMediaType(boolean showSingleMediaType) {
        mAlbumSpec.showSingleMediaType = showSingleMediaType;
        return this;
    }

    /**
     * Show a auto-increased number or a check mark when user select media.
     *
     * @param countable true for a auto-increased number from 1, false for a check mark. Default
     *                  value is false.
     * @return {@link GlobalSetting} for fluent API.
     */
    public AlbumSetting countable(boolean countable) {
        mAlbumSpec.countable = countable;
        return this;
    }

    /**
     * Maximum selectable count.
     *
     * @param maxSelectable Maximum selectable count. Default value is 1.
     * @return {@link GlobalSetting} for fluent API.
     */
    public AlbumSetting maxSelectable(int maxSelectable) {
        if (maxSelectable < 1)
            throw new IllegalArgumentException("maxSelectable must be greater than or equal to one");
        if (mAlbumSpec.maxImageSelectable > 0 || mAlbumSpec.maxVideoSelectable > 0)
            throw new IllegalStateException("already set maxImageSelectable and maxVideoSelectable");
        mAlbumSpec.maxSelectable = maxSelectable;
        return this;
    }

    /**
     * Only useful when {@link AlbumSpec#mediaTypeExclusive} set true and you want to set different maximum
     * selectable files for image and video media types.
     *
     * @param maxImageSelectable Maximum selectable count for image.
     * @param maxVideoSelectable Maximum selectable count for video.
     * @return
     */
    public AlbumSetting maxSelectablePerMediaType(int maxImageSelectable, int maxVideoSelectable) {
        if (maxImageSelectable < 1 || maxVideoSelectable < 1)
            throw new IllegalArgumentException(("max selectable must be greater than or equal to one"));
        mAlbumSpec.maxSelectable = -1;
        mAlbumSpec.maxImageSelectable = maxImageSelectable;
        mAlbumSpec.maxVideoSelectable = maxVideoSelectable;
        return this;
    }

    /**
     * Add filter to filter each selecting item.
     *
     * @param filter {@link Filter}
     * @return {@link GlobalSetting} for fluent API.
     */
    public AlbumSetting addFilter(@NonNull Filter filter) {
        if (mAlbumSpec.filters == null) {
            mAlbumSpec.filters = new ArrayList<>();
        }
        if (filter == null) throw new IllegalArgumentException("filter cannot be null");
        mAlbumSpec.filters.add(filter);
        return this;
    }

    /**
     * Show a original photo check options.Let users decide whether use original photo after select
     *
     * @param enable Whether to enable original photo or not
     * @return {@link GlobalSetting} for fluent API.
     */
    public AlbumSetting originalEnable(boolean enable) {
        mAlbumSpec.originalable = enable;
        return this;
    }

    /**
     * Maximum original size,the unit is MB. Only useful when {link@originalEnable} set true
     *
     * @param size Maximum original size. Default value is Integer.MAX_VALUE
     * @return {@link GlobalSetting} for fluent API.
     */
    public AlbumSetting maxOriginalSize(int size) {
        mAlbumSpec.originalMaxSize = size;
        return this;
    }



}
