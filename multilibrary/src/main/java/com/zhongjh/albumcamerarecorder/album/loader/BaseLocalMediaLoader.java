package com.zhongjh.albumcamerarecorder.album.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.zhongjh.albumcamerarecorder.album.entity.Album;
import com.zhongjh.albumcamerarecorder.album.entity.LocalMedia;
import com.zhongjh.albumcamerarecorder.album.listener.OnQueryAlbumListener;
import com.zhongjh.albumcamerarecorder.album.listener.OnQueryAllAlbumListener;
import com.zhongjh.albumcamerarecorder.album.listener.OnQueryDataResultListener;
import com.zhongjh.albumcamerarecorder.settings.CameraSetting;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * 加载图像和视频的接口，区分全部和分页
 *
 * @author zhongjh
 */
public abstract class BaseLocalMediaLoader {

    protected static final String COLUMN_COUNT = "count";
    protected static final String COLUMN_BUCKET_ID = "bucket_id";
    protected static final String COLUMN_DURATION = "duration";
    protected static final String COLUMN_BUCKET_DISPLAY_NAME = "bucket_display_name";
    protected static final String COLUMN_ORIENTATION = "orientation";

    private final Context mContext;

    public BaseLocalMediaLoader(Context context) {
        this.mContext = context;
    }

    /**
     * 返回哪些列 - 进行分页
     */
    protected static final String[] PAGE_PROJECTION = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            COLUMN_DURATION,
            MediaStore.MediaColumns.SIZE,
            COLUMN_BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DISPLAY_NAME,
            COLUMN_BUCKET_ID,
            MediaStore.MediaColumns.DATE_ADDED,
            COLUMN_ORIENTATION};

    /**
     * 返回那些列 - 不分页
     */
    protected static final String[] ALL_PROJECTION = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            COLUMN_DURATION,
            MediaStore.MediaColumns.SIZE,
            COLUMN_BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DISPLAY_NAME,
            COLUMN_BUCKET_ID,
            MediaStore.MediaColumns.DATE_ADDED,
            COLUMN_ORIENTATION,
            "COUNT(*) AS " + COLUMN_COUNT};

    /**
     * 查询专辑封面
     *
     * @param bucketId 专辑id
     */
    public abstract String getAlbumFirstCover(long bucketId);

    /**
     * query album list
     */
    public abstract void loadAllAlbum(OnQueryAllAlbumListener<Album> query);

    /**
     * 页面查询指定内容
     *
     * @param bucketId 专辑id
     * @param page 第几页
     * @param pageSize 每页多少条
     */
    public abstract void loadPageMediaData(long bucketId, int page, int pageSize, OnQueryDataResultListener<LocalMedia> query);

    /**
     * 页面查询指定内容
     */
    public abstract void loadOnlyInAppDirAllMedia(OnQueryAlbumListener<Album> query);

    /**
     * 一个过滤器声明要返回哪些行
     * 格式为SQL WHERE子句(不包括WHERE本身)
     * 传递null将返回给定URI的所有行
     */
    protected abstract String getSelection();

    /**
     * 你可以在selection中包含?s，它将被selectionArgs中的值替换
     * 按它们在选项中出现的顺序。这些值将被绑定为字符串。
     */
    protected abstract String[] getSelectionArgs();

    /**
     * 如何对格式化为SQL order BY子句的行进行排序(不包括order BY本身)。
     * 传递null将使用默认排序顺序，这可能是无序的。
     */
    protected abstract String getSortOrder();

    /**
     * 解析LocalMedia
     *
     * @param data      游标
     * @param isUsePool 对象池
     */
    protected abstract LocalMedia parseLocalMedia(Cursor data, boolean isUsePool);

    /**
     * 获取视频(最长或最短时间)
     *
     * @return String
     */
    protected String getDurationCondition() {
        long maxS = mCameraSpec.maxDuration.filterVideoMaxSecond == 0 ? Long.MAX_VALUE : getConfig().filterVideoMaxSecond;
        return String.format(Locale.CHINA, "%d <%s " + COLUMN_DURATION + " and " + COLUMN_DURATION + " <= %d",
                Math.max((long) 0, getConfig().filterVideoMinSecond), "=", maxS);
    }

    /**
     * 获取媒体大小(maxFileSize或miniFileSize)
     *
     * @return String
     */
    protected String getFileSizeCondition() {
        long maxS = getConfig().filterMaxFileSize == 0 ? Long.MAX_VALUE : getConfig().filterMaxFileSize;
        return String.format(Locale.CHINA, "%d <%s " + MediaStore.MediaColumns.SIZE + " and " + MediaStore.MediaColumns.SIZE + " <= %d",
                Math.max(0, getConfig().filterMinFileSize), "=", maxS);
    }

    /**
     * 查询Mime条件
     *
     * @return String
     */
    protected String getQueryMimeCondition() {
        List<String> filters = getConfig().queryOnlyList;
        HashSet<String> filterSet = new HashSet<>(filters);
        Iterator<String> iterator = filterSet.iterator();
        StringBuilder stringBuilder = new StringBuilder();
        int index = -1;
        while (iterator.hasNext()) {
            String value = iterator.next();
            if (TextUtils.isEmpty(value)) {
                continue;
            }
            if (getConfig().chooseMode == SelectMimeType.ofVideo()) {
                if (value.startsWith(PictureMimeType.MIME_TYPE_PREFIX_IMAGE) || value.startsWith(PictureMimeType.MIME_TYPE_PREFIX_AUDIO)) {
                    continue;
                }
            } else if (getConfig().chooseMode == SelectMimeType.ofImage()) {
                if (value.startsWith(PictureMimeType.MIME_TYPE_PREFIX_AUDIO) || value.startsWith(PictureMimeType.MIME_TYPE_PREFIX_VIDEO)) {
                    continue;
                }
            } else if (getConfig().chooseMode == SelectMimeType.ofAudio()) {
                if (value.startsWith(PictureMimeType.MIME_TYPE_PREFIX_VIDEO) || value.startsWith(PictureMimeType.MIME_TYPE_PREFIX_IMAGE)) {
                    continue;
                }
            }
            index++;
            stringBuilder.append(index == 0 ? " AND " : " OR ").append(MediaStore.MediaColumns.MIME_TYPE).append("='").append(value).append("'");
        }
        if (getConfig().chooseMode != SelectMimeType.ofVideo()) {
            if (!getConfig().isGif && !filterSet.contains(PictureMimeType.ofGIF())) {
                stringBuilder.append(NOT_GIF);
            }
        }
        return stringBuilder.toString();
    }

}
