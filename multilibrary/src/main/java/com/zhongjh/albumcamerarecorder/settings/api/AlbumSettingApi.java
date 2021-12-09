package com.zhongjh.albumcamerarecorder.settings.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zhongjh.albumcamerarecorder.album.filter.BaseFilter;
import com.zhongjh.albumcamerarecorder.album.listener.OnCheckedListener;
import com.zhongjh.albumcamerarecorder.album.listener.OnSelectedListener;
import com.zhongjh.albumcamerarecorder.settings.AlbumSetting;
import com.zhongjh.albumcamerarecorder.settings.AlbumSpec;
import com.zhongjh.albumcamerarecorder.settings.MultiMediaSetting;

import java.util.Set;

import com.zhongjh.common.enums.MimeType;

/**
 * 相册设置接口
 * @author zhongjh
 */
public interface AlbumSettingApi {

    /**
     * 销毁事件
     */
    void onDestroy();

    /**
     * 支持的类型：图片，视频
     * 这个优先于 {@link MultiMediaSetting#choose}
     * @param mimeTypes 类型
     * @return {@link AlbumSetting} this
     */
    AlbumSetting mimeTypeSet(@NonNull Set<MimeType> mimeTypes);

    /**
     * 如果选择的媒体仅为图像或视频，是否仅显示一种媒体类型。
     *
     * @param showSingleMediaType 是否只显示一种媒体类型，图像或视频。
     * @return {@link AlbumSetting} this
     * @see AlbumSpec#onlyShowImages()
     * @see AlbumSpec#onlyShowVideos()
     */
    AlbumSetting showSingleMediaType(boolean showSingleMediaType);

    /**
     * 用户选择媒体时显示自动增加的数字或复选标记。
     *
     * @param countable 如果是自动增加的数字，则为真；如果是复选标记，则为假。默认值为假。
     * @return {@link AlbumSetting} this
     */
    AlbumSetting countable(boolean countable);

    /**
     * 添加筛选器以筛选每个文件。
     *
     * @param baseFilter {@link BaseFilter}
     * @return {@link AlbumSetting} this
     */
    AlbumSetting addFilter(@NonNull BaseFilter baseFilter);

    /**
     * 显示原始照片检查选项。让用户在选择后决定是否使用原始照片
     *
     * @param enable 是否启用原始照片
     * @return {@link AlbumSetting} this
     */
    AlbumSetting originalEnable(boolean enable);

    /**
     * 最大原始大小，单位为MB。 仅当 {@link #originalEnable} 设置为真时才有用
     *
     * @param size 最大原始大小.默认值为 Integer.MAX_VALUE
     * @return {@link AlbumSetting} this
     */
    AlbumSetting maxOriginalSize(int size);

    /**
     * 设置媒体网格的固定跨度计数。不同屏幕方向相同。
     *
     * 设置时将忽略 {@link #gridExpectedSize(int)} 此项.
     *
     * @param spanCount 请求的范围计数
     * @return {@link AlbumSetting} this
     */
    AlbumSetting spanCount(int spanCount);

    /**
     * 设置媒体网格的预期大小以适应不同的屏幕大小。
     * 这不一定适用，因为媒体网格应该填充视图容器。测量的媒体网格大小将尽可能接近该值。
     *
     * @param size 预期的媒体网格大小（像素）.
     * @return {@link AlbumSetting} this
     */
    AlbumSetting gridExpectedSize(int size);

    /**
     * 照片缩略图的比例与视图的大小相比。它应该是(0.0,1.0] 中的浮点值.
     *
     * @param scale 缩略图的缩放比例（0.0，1.0）。默认值为0.5。
     * @return {@link AlbumSetting} this
     */
    AlbumSetting thumbnailScale(float scale);

    /**
     * 当用户选择或取消选择某个内容时，立即为回调设置侦听器。
     * <p>
     *
     * @param listener {@link OnSelectedListener}
     * @return {@link AlbumSetting} this
     */
    AlbumSetting setOnSelectedListener(@Nullable OnSelectedListener listener);

    /**
     * 当用户选中或取消选中“原始”时，立即为回调设置侦听器。
     *
     * @param listener {@link OnSelectedListener}
     * @return {@link AlbumSetting} this
     */
    AlbumSetting setOnCheckedListener(@Nullable OnCheckedListener listener);

}
