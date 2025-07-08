package com.zhongjh.multimedia.settings

import com.zhongjh.multimedia.album.filter.BaseFilter
import com.zhongjh.multimedia.album.listener.OnCheckedListener
import com.zhongjh.multimedia.album.listener.OnSelectedListener
import com.zhongjh.multimedia.constants.ModuleTypes
import com.zhongjh.multimedia.utils.SelectableUtils.singleImageVideo
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.enums.MimeType
import com.zhongjh.common.enums.MimeType.Companion.ofImage
import com.zhongjh.common.enums.MimeType.Companion.ofVideo

/**
 * 相册的设置
 *
 * @author zhongjh
 * @date 2018/12/27
 */
object AlbumSpec {

    // region start 属性
    /**
     * 选择 mime 的类型，MimeType.allOf()
     */
    var mimeTypeSet: Set<MimeType>? = null

    /**
     * 相册已经选好的数据
     */
    var SelectedData = ArrayList<LocalMedia>()

    /**
     * 是否支持gif,默认为true
     * 当设置为false的时候，会在相册数据源剔除掉gif文件
     */
    var isSupportGif = true

    /**
     * 是否支持webp,默认为true
     * 当设置为false的时候，会在相册数据源剔除掉webp文件
     */
    var isSupportWebp = true

    /**
     * 是否支持bmp,默认为true
     * 当设置为false的时候，会在相册数据源剔除掉bmp文件
     */
    var isSupportBmp = true

    /**
     * 是否支持heic,默认为true
     * 当设置为false的时候，会在相册数据源剔除掉heic文件
     */
    var isSupportHeic = true

    /**
     * 是否可以同时选择不同的资源类型 true表示不可以 false表示可以
     */
    var mediaTypeExclusive = false

    /**
     * 每页多少个文件
     */
    var pageSize: Int = 60

    /**
     * 仅仅显示一个多媒体类型
     */
    var showSingleMediaType = false

    /**
     * 是否显示多选图片的数字
     */
    var countable = true

    /**
     * 如果设置了item宽度的具体数值则计算获得列表的列数，否则使用设置的列数。如果你想要固定的跨度计数，请使用 spanCount(int spanCount)，当方向更改时，范围计数将保持不变。
     */
    var spanCount = 0

    /**
     * 设置列宽
     */
    var gridExpectedSize = 0

    /**
     * 图片缩放比例
     */
    var thumbnailScale = 0f

    /**
     * 触发选择的事件，不管是列表界面还是显示大图的列表界面
     */
    var onSelectedListener: OnSelectedListener? = null

    /**
     * 是否启用原图
     */
    var originalEnable = false

    /**
     * 是否启动相册列表滑动隐藏顶部和底部控件，上滑隐藏、下滑显示
     */
    var slidingHiddenEnable = false

    /**
     * 最大原图size,仅当originalEnable为true的时候才有效
     */
    var originalMaxSize = Int.MAX_VALUE
    var onCheckedListener: OnCheckedListener? = null
    var baseFilters: ArrayList<BaseFilter>? = null

    /**
     * 用于筛选视频最长时长,秒作为单位
     */
    var videoMaxSecond = 0

    /**
     * 用于筛选视频最短时长,秒作为单位
     */
    var videoMinSecond = 0

    /**
     * 用于过滤文件大小的最大值
     */
    var filterMaxFileSize = 0L

    /**
     * 用于过滤文件大小的最小值
     */
    var filterMinFileSize = 1024L

    // endregion end 属性

    val cleanInstance = AlbumSpec
        get() {
            val albumSpec: AlbumSpec = field
            albumSpec.reset()
            return albumSpec
        }

    /**
     * 重置
     */
    private fun reset() {
        mimeTypeSet = null
        SelectedData.clear()
        isSupportGif = true
        isSupportWebp = true
        isSupportBmp = true
        mediaTypeExclusive = false
        pageSize = 60
        showSingleMediaType = false
        countable = true
        baseFilters = null
        spanCount = 3
        thumbnailScale = 0.5f
        originalEnable = false
        originalMaxSize = Int.MAX_VALUE
        // 筛选最长的播放时间
        videoMaxSecond = 0
        // 筛选最短的播放时间
        videoMinSecond = 0
        filterMaxFileSize = 0L
        filterMinFileSize = 0L
    }

    /**
     * 是否不显示多选数字和是否单选
     *
     * @return 是否
     */
    fun singleSelectionModeEnabled(): Boolean {
        return !countable && singleImageVideo
    }

    /**
     * 仅显示图片 或者 视频可选为0个
     */
    fun onlyShowImages(): Boolean {
        return (showSingleMediaType &&
                ofImage().containsAll(GlobalSpec.getMimeTypeSet(ModuleTypes.ALBUM))
                || GlobalSpec.maxVideoSelectable != null && GlobalSpec.maxVideoSelectable == 0)
    }

    /**
     * 仅显示视频 或者 图片可选为0个
     */
    fun onlyShowVideos(): Boolean {
        return (showSingleMediaType &&
                ofVideo().containsAll(GlobalSpec.getMimeTypeSet(ModuleTypes.ALBUM))
                || GlobalSpec.maxImageSelectable != null && GlobalSpec.maxImageSelectable == 0)
    }

}