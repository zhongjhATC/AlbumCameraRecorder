package com.zhongjh.albumcamerarecorder.settings;

import com.zhongjh.common.enums.MimeType;

import com.zhongjh.albumcamerarecorder.album.filter.BaseFilter;
import com.zhongjh.albumcamerarecorder.album.listener.OnCheckedListener;
import com.zhongjh.albumcamerarecorder.album.listener.OnSelectedListener;
import com.zhongjh.albumcamerarecorder.constants.ModuleTypes;
import com.zhongjh.albumcamerarecorder.utils.SelectableUtils;

import java.util.List;
import java.util.Set;

/**
 * 相册的设置
 *
 * @author zhongjh
 * @date 2018/12/27
 */
public class AlbumSpec {

    private AlbumSpec() {
    }

    public static AlbumSpec getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public static AlbumSpec getCleanInstance() {
        AlbumSpec albumSpec = getInstance();
        albumSpec.reset();
        return albumSpec;
    }

    /**
     * 重置
     */
    private void reset() {
        mimeTypeSet = null;
        mediaTypeExclusive = true;
        showSingleMediaType = false;
        countable = false;
        baseFilters = null;
        spanCount = 3;
        thumbnailScale = 0.5f;
        originalable = false;
        originalMaxSize = Integer.MAX_VALUE;
    }

    // region

    /**
     * 选择 mime 的类型，MimeType.allOf()
     */
    public Set<MimeType> mimeTypeSet;
    /**
     * 是否可以同时选择不同的资源类型 true表示不可以 false表示可以
     */
    public boolean mediaTypeExclusive;
    /**
     * 仅仅显示一个多媒体类型
     */
    public boolean showSingleMediaType;
    /**
     * 是否显示多选图片的数字
     */
    public boolean countable;
    /**
     * 如果设置了item宽度的具体数值则计算获得列表的列数，否则使用设置的列数。如果你想要固定的跨度计数，请使用 spanCount(int spanCount)，当方向更改时，范围计数将保持不变。
     */
    public int spanCount;
    /**
     * 设置列宽
     */
    public int gridExpectedSize;
    /**
     * 图片缩放比例
     */
    public float thumbnailScale;
    /**
     * 触发选择的事件，不管是列表界面还是显示大图的列表界面
     */
    public OnSelectedListener onSelectedListener;
    /**
     * 是否原图
     */
    public boolean originalable;
    /**
     * 最大原图size,仅当originalEnable为true的时候才有效
     */
    public int originalMaxSize;
    public OnCheckedListener onCheckedListener;
    public List<BaseFilter> baseFilters;

    // endregion

    /**
     * 是否不显示多选数字和是否单选
     *
     * @return 是否
     */
    public boolean singleSelectionModeEnabled() {
        return !countable && SelectableUtils.getSingleImageVideo();
    }

    /**
     * 仅显示图片 或者 视频可选为0个
     */
    public boolean onlyShowImages() {
        return (showSingleMediaType &&
                MimeType.ofImage().containsAll(GlobalSpec.getInstance().getMimeTypeSet(ModuleTypes.ALBUM)))
                || (GlobalSpec.getInstance().maxVideoSelectable != null && GlobalSpec.getInstance().maxVideoSelectable == 0);
    }

    /**
     * 仅显示视频 或者 图片可选为0个
     */
    public boolean onlyShowVideos() {
        return (showSingleMediaType &&
                MimeType.ofVideo().containsAll(GlobalSpec.getInstance().getMimeTypeSet(ModuleTypes.ALBUM)))
                || (GlobalSpec.getInstance().maxImageSelectable != null && GlobalSpec.getInstance().maxImageSelectable == 0);
    }

    private static final class InstanceHolder {
        private static final AlbumSpec INSTANCE = new AlbumSpec();
    }

}
