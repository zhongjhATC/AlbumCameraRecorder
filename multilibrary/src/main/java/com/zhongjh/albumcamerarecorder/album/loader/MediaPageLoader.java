package com.zhongjh.albumcamerarecorder.album.loader;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import com.zhongjh.albumcamerarecorder.album.entity.MediaData;
import com.zhongjh.albumcamerarecorder.album.listener.OnQueryDataPageListener;
import com.zhongjh.albumcamerarecorder.settings.AlbumSpec;
import com.zhongjh.common.utils.SdkVersionUtils;
import com.zhongjh.common.utils.ThreadUtils;

/**
 * 用于查询分页的相册多媒体数据
 *
 * @author zhongjh
 * @date 2023/7/26
 */
public class MediaPageLoader extends BaseMediaLoader {

    /**
     * 查询指定目录(页)中的数据
     */
    public void loadPageMediaData(long bucketId, int page, int limit, int pageSize,
                                  OnQueryDataPageListener<MediaData> listener) {
        ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<MediaData>() {

            @Override
            public MediaData doInBackground() throws Throwable {
                Cursor data = null;
                try {
                    if (SdkVersionUtils.isR()) {
                        Bundle queryArgs = createQueryArgsBundle(getPageSelection(bucketId), getPageSelectionArgs(bucketId), limit, (page - 1) * pageSize);
                        data = mContext.getContentResolver().query(QUERY_URI, PROJECTION_PAGE, queryArgs, null);
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                    Log.i(TAG, "loadMedia Page Data Error: " + exception.getMessage());
                    return new MediaData();
                }
                return null;
            }

            @Override
            public void onSuccess(MediaData result) {

            }
        });
    }

    /**
     * Android 版本R下的处理
     * 创建Query ArgsBundle
     *
     * @param selection     查询语句
     * @param selectionArgs 查询参数
     * @param limitCount
     * @param offset
     * @return
     */
    public static Bundle createQueryArgsBundle(String selection, String[] selectionArgs, int limitCount, int offset) {
        Bundle queryArgs = new Bundle();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection);
            queryArgs.putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs);
            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, MediaStore.Files.FileColumns._ID + " DESC");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                queryArgs.putString(ContentResolver.QUERY_ARG_SQL_LIMIT, limitCount + " offset " + offset);
            }
        }
        return queryArgs;
    }

    /**
     * 查询条件的sql语句
     *
     * @param bucketId 专辑ID
     * @return 查询条件的sql语句
     */
    private String getPageSelection(long bucketId) {
        String durationCondition = getDurationCondition();
        String sizeCondition = getFileSizeCondition();
        if (AlbumSpec.INSTANCE.onlyShowImages()) {
            // 只查询图片
            return getPageSelectionArgsForImageMediaCondition(bucketId, sizeCondition);
        } else if (AlbumSpec.INSTANCE.onlyShowVideos()) {
            // 只查询视频
            return getPageSelectionArgsForVideoOrAudioMediaCondition(bucketId, durationCondition, sizeCondition);
        } else {
            // 查询所有
            String sql = getPageSelectionArgsForAllMediaCondition(bucketId, durationCondition, sizeCondition);
            Log.d(TAG, "sql: " + sql);
            return sql;
        }
        return null;
    }

    /**
     * 只查询图片
     *
     * @param bucketId      专辑ID
     * @param sizeCondition 文件大小条件
     * @return sql语句
     */
    private String getPageSelectionArgsForImageMediaCondition(long bucketId, String sizeCondition) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(").append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?");
        if (bucketId == -1) {
            return stringBuilder.append(") AND ").append(sizeCondition).toString();
        } else {
            return stringBuilder.append(") AND ").append(COLUMN_BUCKET_ID).append("=? AND ").append(sizeCondition).toString();
        }
    }

    /**
     * 只查询视频
     *
     * @param bucketId          专辑ID
     * @param durationCondition 视频文件的持续时间
     * @param sizeCondition     文件大小条件
     * @return sql语句
     */
    private String getPageSelectionArgsForVideoOrAudioMediaCondition(long bucketId, String durationCondition, String sizeCondition) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(").append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?").append(" AND ").append(durationCondition).append(") AND ");
        if (bucketId == -1) {
            return stringBuilder.append(sizeCondition).toString();
        } else {
            return stringBuilder.append(COLUMN_BUCKET_ID).append("=? AND ").append(sizeCondition).toString();
        }
    }

    /**
     * 查询所有
     *
     * @param bucketId          专辑ID
     * @param durationCondition 视频文件的持续时间
     * @param sizeCondition     文件大小条件
     * @return sql语句
     */
    private static String getPageSelectionArgsForAllMediaCondition(long bucketId, String durationCondition, String sizeCondition) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(").append(MediaStore.Files.FileColumns.MEDIA_TYPE)
                .append("=?").append(" OR ").append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=? AND ").append(durationCondition).append(") AND ");
        if (bucketId == -1) {
            return stringBuilder.append(sizeCondition).toString();
        } else {
            return stringBuilder.append(COLUMN_BUCKET_ID).append("=? AND ").append(sizeCondition).toString();
        }
    }

    /**
     * 设置参数
     *
     * @param bucketId 专辑ID
     * @return 参数集合
     */
    private String[] getPageSelectionArgs(long bucketId) {
        switch (config.chooseMode) {
            case PictureConfig.TYPE_ALL:
                if (bucketId == -1) {
                    // ofAll
                    return new String[]{
                            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
                    };
                }
                //  Gets the specified album directory
                return new String[]{
                        String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                        String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
                        ValueOf.toString(bucketId)
                };
            case PictureConfig.TYPE_IMAGE:
                // Get photo
                return getSelectionArgsForPageSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE, bucketId);
            case PictureConfig.TYPE_VIDEO:
                // Get video
                return getSelectionArgsForPageSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO, bucketId);
            case PictureConfig.TYPE_AUDIO:
                // Get audio
                return getSelectionArgsForPageSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO, bucketId);
        }
        return null;
    }

    /**
     * 获取指定类型的文件
     *
     * @param mediaType 类型
     * @return 参数数组
     */
    private static String[] getSelectionArgsForPageSingleMediaType(int mediaType, long bucketId) {
        return bucketId == -1 ? new String[]{String.valueOf(mediaType)} : new String[]{String.valueOf(mediaType), String.valueOf(bucketId)};
    }

}
