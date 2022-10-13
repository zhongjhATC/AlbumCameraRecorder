package com.zhongjh.albumcamerarecorder.album.loader;

import android.content.Context;
import android.provider.MediaStore;

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
     * query album cover
     *
     * @param bucketId
     */
    public abstract String getAlbumFirstCover(long bucketId);

}
