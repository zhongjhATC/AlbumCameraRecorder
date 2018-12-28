package com.zhongjh.cameraviewsoundrecorder.settings;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.zhongjh.cameraviewsoundrecorder.album.engine.ImageEngine;
import com.zhongjh.cameraviewsoundrecorder.album.engine.impl.GlideEngine;
import com.zhongjh.cameraviewsoundrecorder.album.engine.impl.PicassoEngine;
import com.zhongjh.cameraviewsoundrecorder.album.enums.MimeType;
import com.zhongjh.cameraviewsoundrecorder.album.filter.Filter;
import com.zhongjh.cameraviewsoundrecorder.album.listener.OnCheckedListener;
import com.zhongjh.cameraviewsoundrecorder.album.listener.OnSelectedListener;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by zhongjh on 2018/12/27.
 */
public class AlbumSetting {

    private final AlbumSpec mAlbumSpec;
    private final GlobalSpec mGlobalSpec;

    /**
     *
     * @param mimeTypes
     * @param mediaTypeExclusive 是否可以同时选择不同的资源类型 true表示不可以 false表示可以
     */
    public AlbumSetting(@NonNull Set<MimeType> mimeTypes, boolean mediaTypeExclusive) {
        mAlbumSpec = AlbumSpec.getInstance();
        mGlobalSpec = GlobalSpec.getInstance();
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
        mGlobalSpec.maxSelectable = -1;
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

    /**
     * Capture strategy provided for the location to save photos including internal and external
     * storage and also a authority for {@link android.support.v4.content.FileProvider}.
     *
     * @param captureStrategy {@link CaptureStrategy}, needed only when capturing is enabled.
     * @return {@link GlobalSetting} for fluent API.
     */
    public AlbumSetting captureStrategy(CaptureStrategy captureStrategy) {
        mAlbumSpec.captureStrategy = captureStrategy;
        return this;
    }

    /**
     * Set a fixed span count for the media grid. Same for different screen orientations.
     * <p>
     * This will be ignored when {@link #gridExpectedSize(int)} is set.
     *
     * @param spanCount Requested span count.
     * @return {@link GlobalSetting} for fluent API.
     */
    public AlbumSetting spanCount(int spanCount) {
        if (spanCount < 1) throw new IllegalArgumentException("spanCount cannot be less than 1");
        mAlbumSpec.spanCount = spanCount;
        return this;
    }

    /**
     * Set expected size for media grid to adapt to different screen sizes. This won't necessarily
     * be applied cause the media grid should fill the view container. The measured media grid's
     * size will be as close to this value as possible.
     *
     * @param size Expected media grid size in pixel.
     * @return {@link GlobalSetting} for fluent API.
     */
    public AlbumSetting gridExpectedSize(int size) {
        mAlbumSpec.gridExpectedSize = size;
        return this;
    }

    /**
     * Photo thumbnail's scale compared to the View's size. It should be a float value in (0.0,
     * 1.0].
     *
     * @param scale Thumbnail's scale in (0.0, 1.0]. Default value is 0.5.
     * @return {@link GlobalSetting} for fluent API.
     */
    public AlbumSetting thumbnailScale(float scale) {
        if (scale <= 0f || scale > 1f)
            throw new IllegalArgumentException("Thumbnail scale must be between (0.0, 1.0]");
        mAlbumSpec.thumbnailScale = scale;
        return this;
    }



    /**
     * Set listener for callback immediately when user select or unselect something.
     * <p>
     * It's a redundant API with {@link MultiMedia#obtainResult(Intent)},
     * we only suggest you to use this API when you need to do something immediately.
     *
     * @param listener {@link OnSelectedListener}
     * @return {@link GlobalSetting} for fluent API.
     */
    @NonNull
    public AlbumSetting setOnSelectedListener(@Nullable OnSelectedListener listener) {
        mAlbumSpec.onSelectedListener = listener;
        return this;
    }

    /**
     * Set listener for callback immediately when user check or uncheck original.
     *
     * @param listener {@link OnSelectedListener}
     * @return {@link GlobalSetting} for fluent API.
     */
    public AlbumSetting setOnCheckedListener(@Nullable OnCheckedListener listener) {
        mAlbumSpec.onCheckedListener = listener;
        return this;
    }

}
