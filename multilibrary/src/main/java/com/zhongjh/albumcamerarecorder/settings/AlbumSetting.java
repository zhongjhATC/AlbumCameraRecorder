package com.zhongjh.albumcamerarecorder.settings;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.zhongjh.albumcamerarecorder.album.enums.MimeType;
import com.zhongjh.albumcamerarecorder.album.filter.Filter;
import com.zhongjh.albumcamerarecorder.album.listener.OnCheckedListener;
import com.zhongjh.albumcamerarecorder.album.listener.OnSelectedListener;

import java.util.ArrayList;
import java.util.Set;

/**
 * 相册设置
 * Created by zhongjh on 2018/12/27.
 */
public class AlbumSetting {

    private final AlbumSpec mAlbumSpec;

    /**
     *
     * @param mediaTypeExclusive 是否可以同时选择不同的资源类型 true表示不可以 false表示可以
     */
    public AlbumSetting(boolean mediaTypeExclusive) {
        mAlbumSpec = AlbumSpec.getInstance();

        mAlbumSpec.mediaTypeExclusive = mediaTypeExclusive;
    }

    /**
     * 支持的类型：图片，视频
     * 这个优先于 {@link MultiMediaSetting#choose}
     * @param mimeTypes 类型
     * @return {@link AlbumSetting} this
     */
    public AlbumSetting mimeTypeSet(@NonNull Set<MimeType> mimeTypes) {
        mAlbumSpec.mimeTypeSet = mimeTypes;
        return this;
    }

    /**
     * 如果选择的媒体仅为图像或视频，是否仅显示一种媒体类型。
     *
     * @param showSingleMediaType 是否只显示一种媒体类型，图像或视频。
     * @return {@link AlbumSetting} this
     * @see AlbumSpec#onlyShowImages()
     * @see AlbumSpec#onlyShowVideos()
     */
    public AlbumSetting showSingleMediaType(boolean showSingleMediaType) {
        mAlbumSpec.showSingleMediaType = showSingleMediaType;
        return this;
    }

    /**
     * 用户选择媒体时显示自动增加的数字或复选标记。
     *
     * @param countable 如果是自动增加的数字，则为真；如果是复选标记，则为假。默认值为假。
     * @return {@link AlbumSetting} this
     */
    public AlbumSetting countable(boolean countable) {
        mAlbumSpec.countable = countable;
        return this;
    }

    /**
     * 添加筛选器以筛选每个文件。
     *
     * @param filter {@link Filter}
     * @return {@link AlbumSetting} this
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
     * 显示原始照片检查选项。让用户在选择后决定是否使用原始照片
     *
     * @param enable 是否启用原始照片
     * @return {@link AlbumSetting} this
     */
    public AlbumSetting originalEnable(boolean enable) {
        mAlbumSpec.originalable = enable;
        return this;
    }

    /**
     * 最大原始大小，单位为MB。 仅当 {@link #originalEnable} 设置为真时才有用
     *
     * @param size 最大原始大小.默认值为 Integer.MAX_VALUE
     * @return {@link AlbumSetting} this
     */
    public AlbumSetting maxOriginalSize(int size) {
        mAlbumSpec.originalMaxSize = size;
        return this;
    }

    /**
     * 提供保存公有或者私有的文件路径
     * 文件路径存储于 {@link android.support.v4.content.FileProvider}.
     * 这个优先于 {@link GlobalSetting#captureStrategy}
     *
     * @param captureStrategy {@link CaptureStrategy},仅仅启用时才需要
     * @return {@link AlbumSetting} this
     */
    public AlbumSetting captureStrategy(CaptureStrategy captureStrategy) {
        mAlbumSpec.captureStrategy = captureStrategy;
        return this;
    }

    /**
     * 设置媒体网格的固定跨度计数。不同屏幕方向相同。
     *
     * 设置时将忽略 {@link #gridExpectedSize(int)} 此项.
     *
     * @param spanCount 请求的范围计数
     * @return {@link AlbumSetting} this
     */
    public AlbumSetting spanCount(int spanCount) {
        if (spanCount < 1) throw new IllegalArgumentException("spanCount cannot be less than 1");
        mAlbumSpec.spanCount = spanCount;
        return this;
    }

    /**
     * 设置媒体网格的预期大小以适应不同的屏幕大小。
     * 这不一定适用，因为媒体网格应该填充视图容器。测量的媒体网格大小将尽可能接近该值。
     *
     * @param size 预期的媒体网格大小（像素）.
     * @return {@link AlbumSetting} this
     */
    public AlbumSetting gridExpectedSize(int size) {
        mAlbumSpec.gridExpectedSize = size;
        return this;
    }

    /**
     * 照片缩略图的比例与视图的大小相比。它应该是(0.0,1.0] 中的浮点值.
     *
     * @param scale 缩略图的缩放比例（0.0，1.0）。默认值为0.5。
     * @return {@link AlbumSetting} this
     */
    public AlbumSetting thumbnailScale(float scale) {
        if (scale <= 0f || scale > 1f)
            throw new IllegalArgumentException("缩略图比例必须介于(0.0, 1.0]之间");
        mAlbumSpec.thumbnailScale = scale;
        return this;
    }

    /**
     * 当用户选择或取消选择某个内容时，立即为回调设置侦听器。
     * <p>
     *
     * @param listener {@link OnSelectedListener}
     * @return {@link AlbumSetting} this
     */
    @NonNull
    public AlbumSetting setOnSelectedListener(@Nullable OnSelectedListener listener) {
        mAlbumSpec.onSelectedListener = listener;
        return this;
    }

    /**
     * 当用户选中或取消选中“原始”时，立即为回调设置侦听器。
     *
     * @param listener {@link OnSelectedListener}
     * @return {@link AlbumSetting} this
     */
    public AlbumSetting setOnCheckedListener(@Nullable OnCheckedListener listener) {
        mAlbumSpec.onCheckedListener = listener;
        return this;
    }

}
