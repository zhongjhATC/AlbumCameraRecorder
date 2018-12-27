package com.zhongjh.cameraviewsoundrecorder.settings;

import com.zhongjh.cameraviewsoundrecorder.album.engine.ImageEngine;
import com.zhongjh.cameraviewsoundrecorder.album.engine.impl.GlideEngine;
import com.zhongjh.cameraviewsoundrecorder.album.enums.MimeType;
import com.zhongjh.cameraviewsoundrecorder.album.filter.Filter;
import com.zhongjh.cameraviewsoundrecorder.album.listener.OnCheckedListener;
import com.zhongjh.cameraviewsoundrecorder.album.listener.OnSelectedListener;

import java.util.List;
import java.util.Set;

/**
 * 相册的设置
 * Created by zhongjh on 2018/12/27.
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
        maxSelectable = 1;
        maxImageSelectable = 0;
        maxVideoSelectable = 0;
        filters = null;
        captureStrategy = null;
        spanCount = 3;
        thumbnailScale = 0.5f;
        imageEngine = new GlideEngine();
        originalable = false;
        originalMaxSize = Integer.MAX_VALUE;
    }

    // region

    public Set<MimeType> mimeTypeSet; // 选择 mime 的类型，MimeType.allOf()
    public boolean mediaTypeExclusive; // 是否可以同时选择不同的资源类型 true表示不可以 false表示可以
    public boolean showSingleMediaType; // 仅仅显示一个多媒体类型
    public boolean countable;   // 是否显示多选图片的数字
    public int maxSelectable;   // 最大选择数量
    public int maxImageSelectable;  // 最大图片选择数量
    public int maxVideoSelectable;  // 最大视频选择数量
    public CaptureStrategy captureStrategy; // 参数1 true表示拍照存储在共有目录，false表示存储在私有目录；参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
    public int spanCount;           // 如果设置了item宽度的具体数值则计算获得列表的列数，否则使用设置的列数。如果你想要固定的跨度计数，请使用 spanCount(int spanCount)，当方向更改时，范围计数将保持不变。
    public int gridExpectedSize;    // 设置列宽
    public float thumbnailScale;      // 图片缩放比例
    public ImageEngine imageEngine;
    public OnSelectedListener onSelectedListener; // 触发选择的事件，不管是列表界面还是显示大图的列表界面
    public boolean originalable;    // 是否原图
    public int originalMaxSize;     // 最大原图size,仅当originalEnable为true的时候才有效
    public OnCheckedListener onCheckedListener;
    public List<Filter> filters;

    // endregion

    /**
     * 是否不显示多选数字和是否单选
     * @return 是否
     */
    public boolean singleSelectionModeEnabled() {
        return !countable && (maxSelectable == 1 || (maxImageSelectable == 1 && maxVideoSelectable == 1));
    }

    /**
     * 仅显示图片
     */
    public boolean onlyShowImages() {
        return showSingleMediaType && MimeType.ofImage().containsAll(mimeTypeSet);
    }

    /**
     * 仅显示视频
     */
    public boolean onlyShowVideos() {
        return showSingleMediaType && MimeType.ofVideo().containsAll(mimeTypeSet);
    }

    private static final class InstanceHolder {
        private static final AlbumSpec INSTANCE = new AlbumSpec();
    }

}
