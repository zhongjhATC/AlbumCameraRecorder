package com.zhongjh.albumcamerarecorder.settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zhongjh.common.enums.MimeType;

import com.zhongjh.albumcamerarecorder.album.filter.BaseFilter;
import com.zhongjh.albumcamerarecorder.album.listener.OnCheckedListener;
import com.zhongjh.albumcamerarecorder.album.listener.OnSelectedListener;
import com.zhongjh.albumcamerarecorder.settings.api.AlbumSettingApi;

import java.util.ArrayList;
import java.util.Set;

/**
 * 相册设置
 *
 * @author zhongjh
 * @date 2018/12/27
 */
public class AlbumSetting implements AlbumSettingApi {

    private final AlbumSpec mAlbumSpec;

    private final static float SCALE_ZERO = 0f;
    private final static float SCALE_ONE = 1f;

    /**
     *
     * @param mediaTypeExclusive 是否可以同时选择不同的资源类型 true表示不可以 false表示可以
     */
    public AlbumSetting(boolean mediaTypeExclusive) {
        mAlbumSpec = AlbumSpec.getCleanInstance();

        mAlbumSpec.mediaTypeExclusive = mediaTypeExclusive;
    }

    @Override
    public void onDestroy() {
        mAlbumSpec.onSelectedListener = null;
        mAlbumSpec.onCheckedListener = null;
    }

    @Override
    public AlbumSetting mimeTypeSet(@NonNull Set<MimeType> mimeTypes) {
        mAlbumSpec.mimeTypeSet = mimeTypes;
        return this;
    }

    @Override
    public AlbumSetting showSingleMediaType(boolean showSingleMediaType) {
        mAlbumSpec.showSingleMediaType = showSingleMediaType;
        return this;
    }

    @Override
    public AlbumSetting countable(boolean countable) {
        mAlbumSpec.countable = countable;
        return this;
    }

    @Override
    public AlbumSetting addFilter(@NonNull BaseFilter baseFilter) {
        if (mAlbumSpec.baseFilters == null) {
            mAlbumSpec.baseFilters = new ArrayList<>();
        }
        mAlbumSpec.baseFilters.add(baseFilter);
        return this;
    }

    @Override
    public AlbumSetting originalEnable(boolean enable) {
        mAlbumSpec.originalable = enable;
        return this;
    }

    @Override
    public AlbumSetting maxOriginalSize(int size) {
        mAlbumSpec.originalMaxSize = size;
        return this;
    }

    @Override
    public AlbumSetting spanCount(int spanCount) {
        if (spanCount < 1) {
            throw new IllegalArgumentException("spanCount cannot be less than 1");
        }
        mAlbumSpec.spanCount = spanCount;
        return this;
    }

    @Override
    public AlbumSetting gridExpectedSize(int size) {
        mAlbumSpec.gridExpectedSize = size;
        return this;
    }

    @Override
    public AlbumSetting thumbnailScale(float scale) {
        if (scale <= SCALE_ZERO || scale > SCALE_ONE) {
            throw new IllegalArgumentException("Thumbnail scale must be between (0.0, 1.0)");
        }
        mAlbumSpec.thumbnailScale = scale;
        return this;
    }

    @NonNull  @Override
    public AlbumSetting setOnSelectedListener(@Nullable OnSelectedListener listener) {
        mAlbumSpec.onSelectedListener = listener;
        return this;
    }

    @Override
    public AlbumSetting setOnCheckedListener(@Nullable OnCheckedListener listener) {
        mAlbumSpec.onCheckedListener = listener;
        return this;
    }


}
