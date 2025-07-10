package com.zhongjh.demo.configuration;


import android.content.Context;
import android.graphics.Point;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.zhongjh.multimedia.album.filter.BaseFilter;
import com.zhongjh.multimedia.album.utils.PhotoMetadataUtils;
import com.zhongjh.common.entity.IncapableCause;
import com.zhongjh.common.entity.LocalMedia;
import com.zhongjh.common.enums.MimeType;
import com.zhongjh.demo.R;

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

    @NonNull
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
        Point size = PhotoMetadataUtils.getBitmapBound(context.getContentResolver(), Uri.parse(item.getPath()));
        if (size.x < mMinWidth || size.y < mMinHeight || item.getSize() > mMaxSize) {
            return new IncapableCause(IncapableCause.DIALOG, context.getString(R.string.error_gif, mMinWidth,
                    String.valueOf(PhotoMetadataUtils.getSizeInMb(mMaxSize))));
        }
        return null;
    }

}
