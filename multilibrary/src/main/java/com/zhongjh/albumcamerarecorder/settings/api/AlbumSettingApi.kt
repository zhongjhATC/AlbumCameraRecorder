package com.zhongjh.albumcamerarecorder.settings.api

import com.zhongjh.albumcamerarecorder.album.filter.BaseFilter
import com.zhongjh.albumcamerarecorder.album.listener.OnCheckedListener
import com.zhongjh.albumcamerarecorder.album.listener.OnSelectedListener
import com.zhongjh.albumcamerarecorder.settings.AlbumSetting
import com.zhongjh.common.enums.MimeType

/**
 * 相册设置接口
 * @author zhongjh
 */
interface AlbumSettingApi {

    /**
     * 支持的类型：图片，视频
     * 这个优先于 [com.zhongjh.albumcamerarecorder.settings.MultiMediaSetting.choose]
     * @param mimeTypes 类型
     * @return [AlbumSetting] this
     */
    fun mimeTypeSet(mimeTypes: Set<MimeType>): AlbumSetting

    /**
     * 如果选择的媒体仅为图像或视频，是否仅显示一种媒体类型。
     *
     * @param showSingleMediaType 是否只显示一种媒体类型，图像或视频。
     * @return [AlbumSetting] this
     * @see com.zhongjh.albumcamerarecorder.settings.AlbumSpec.onlyShowImages
     * @see com.zhongjh.albumcamerarecorder.settings.AlbumSpec.onlyShowVideos
     */
    fun showSingleMediaType(showSingleMediaType: Boolean): AlbumSetting

    /**
     * 用户选择媒体时显示自动增加的数字或复选标记。
     *
     * @param countable 如果是自动增加的数字，则为真；如果是复选标记，则为假。默认值为假。
     * @return [AlbumSetting] this
     */
    fun countable(countable: Boolean): AlbumSetting

    /**
     * 添加筛选器以筛选每个文件。
     *
     * @param baseFilter [BaseFilter]
     * @return [AlbumSetting] this
     */
    fun addFilter(baseFilter: BaseFilter): AlbumSetting

    /**
     * 显示原始照片检查选项。让用户在选择后决定是否使用原始照片
     *
     * @param enable 是否启用原始照片
     * @return [AlbumSetting] this
     */
    fun originalEnable(enable: Boolean): AlbumSetting

    /**
     * 最大原始大小，单位为MB。 仅当 [.originalEnable] 设置为真时才有用
     *
     * @param size 最大原始大小.默认值为 Integer.MAX_VALUE
     * @return [AlbumSetting] this
     */
    fun maxOriginalSize(size: Int): AlbumSetting

    /**
     * 设置媒体网格的固定跨度计数。不同屏幕方向相同。
     *
     * 设置时将忽略 [.gridExpectedSize] 此项.
     *
     * @param spanCount 请求的范围计数
     * @return [AlbumSetting] this
     */
    fun spanCount(spanCount: Int): AlbumSetting

    /**
     * 设置媒体网格的预期大小以适应不同的屏幕大小。
     * 这不一定适用，因为媒体网格应该填充视图容器。测量的媒体网格大小将尽可能接近该值。
     *
     * @param size 预期的媒体网格大小（像素）.
     * @return [AlbumSetting] this
     */
    fun gridExpectedSize(size: Int): AlbumSetting

    /**
     * 照片缩略图的比例与视图的大小相比。它应该是(0.0,1.0] 中的浮点值.
     *
     * @param scale 缩略图的缩放比例（0.0，1.0）。默认值为0.5。
     * @return [AlbumSetting] this
     */
    fun thumbnailScale(scale: Float): AlbumSetting

    /**
     * 当用户选择或取消选择某个内容时，立即为回调设置侦听器。
     *
     *
     *
     * @param listener [OnSelectedListener]
     * @return [AlbumSetting] this
     */
    fun setOnSelectedListener(listener: OnSelectedListener): AlbumSetting

    /**
     * 当用户选中或取消选中“原始”时，立即为回调设置侦听器。
     *
     * @param listener [OnSelectedListener]
     * @return [AlbumSetting] this
     */
    fun setOnCheckedListener(listener: OnCheckedListener): AlbumSetting

    /**
     * 是否启动相册列表滑动隐藏顶部和底部控件，上滑隐藏、下滑显示
     * 默认关闭
     *
     * @param enable 是否启用该功能
     * @return [AlbumSetting] this
     */
    fun slidingHiddenEnable(enable: Boolean) : AlbumSetting

}