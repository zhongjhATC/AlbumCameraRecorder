package com.zhongjh.albumcamerarecorder.album.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.zhongjh.albumcamerarecorder.album.listener.OnQueryDataListener;
import com.zhongjh.albumcamerarecorder.settings.AlbumSpec;
import com.zhongjh.common.utils.ThreadUtils;

import java.util.ArrayList;
import java.util.Locale;

/**
 * @author zhongjh
 * @date 2022/9/9
 */
public class MediaLoader {

    /**
     * 来自于多媒体的数据源标记
     */
    private static final Uri QUERY_URI = MediaStore.Files.getContentUri("external");
    private static final String COLUMN_COUNT = "count";
    private static final String COLUMN_BUCKET_ID = "bucket_id";
    private static final String COLUMN_BUCKET_DISPLAY_NAME = "bucket_display_name";

    private final Context mContext;

    public MediaLoader(Context context) {
        this.mContext = context;
    }

    /**
     * 获取所有专辑
     *
     * @param listener
     */
    public void loadAllAlbum(OnQueryDataListener<Object> listener) {
        new ThreadUtils.SimpleTask<ArrayList<Object>>() {

            @Override
            public ArrayList<Object> doInBackground() {
                // 查询Android数据库
                Cursor data = mContext.getContentResolver().query(
                        // 查询数据来源的标记
                        QUERY_URI,
                        // 需要查询的列
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? PROJECTION_29 : PROJECTION,
                        // 查询条件
                        getCondition(),
                        // 配合上面的参数使用，上面的参数使用占位符"?"，那么这个参的数据会替换掉占位符"?"
                        getSelectionArgs(),
                        // 排序
                        ORDER_BY);
                return null;
            }

            @Override
            public void onSuccess(ArrayList<Object> result) {
                listener.onComplete(result);
            }
        };
    }

    /**
     * Android 29以上可查询的列
     */
    private static final String[] PROJECTION_29 = {
            MediaStore.Files.FileColumns._ID,
            COLUMN_BUCKET_ID,
            COLUMN_BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE};

    /**
     * Android 29以下可查询的列
     */
    private static final String[] PROJECTION = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DATA,
            COLUMN_BUCKET_ID,
            COLUMN_BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            "COUNT(*) AS " + COLUMN_COUNT};

    /**
     * 根据配置而定制的查询条件
     *
     * @return 查询条件字符串
     */
    private String getCondition() {
        String fileSizeCondition = getFileSizeCondition();
        String queryMimeCondition = getQueryMimeTypeCondition();
        switch (config.chooseMode) {
            case PictureConfig.TYPE_ALL:
                // Get all, not including audio
                return getSelectionArgsForAllMediaCondition(getDurationCondition(), fileSizeCondition, queryMimeCondition);
            case PictureConfig.TYPE_IMAGE:
                // Get Images
                return getSelectionArgsForImageMediaCondition(queryMimeCondition, fileSizeCondition);
            case PictureConfig.TYPE_VIDEO:
            case PictureConfig.TYPE_AUDIO:
                // Gets the specified album directory
                return getSelectionArgsForVideoOrAudioMediaCondition(queryMimeCondition, fileSizeCondition);
        }
        return null;
    }

    /**
     * 获取多媒体最大值查询条件的构造字符串
     *
     * @return 多媒体最大值查询条件的构造字符串
     */
    private String getFileSizeCondition() {
        // 获取文件最大值
        long maxFileSize = AlbumSpec.INSTANCE.getOriginalMaxSize() == 0 ? Long.MAX_VALUE : AlbumSpec.INSTANCE.getOriginalMaxSize();
        return String.format(Locale.CHINA, MediaStore.MediaColumns.SIZE + " > 0 and " + MediaStore.MediaColumns.SIZE + " <= %d",
                maxFileSize);
    }

    /**
     * 获取多媒体类型的构造字符串
     *
     * @return 多媒体类型的构造字符串
     */
    private String getQueryMimeTypeCondition() {

    }

}
