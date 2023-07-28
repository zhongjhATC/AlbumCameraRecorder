package com.zhongjh.albumcamerarecorder.album.loader;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.zhongjh.albumcamerarecorder.album.entity.LocalMedia;
import com.zhongjh.albumcamerarecorder.album.entity.MediaData;
import com.zhongjh.albumcamerarecorder.album.listener.OnQueryDataPageListener;
import com.zhongjh.albumcamerarecorder.settings.AlbumSpec;
import com.zhongjh.common.enums.MimeType;
import com.zhongjh.common.utils.MimeTypeUtils;
import com.zhongjh.common.utils.SdkVersionUtils;
import com.zhongjh.common.utils.ThreadUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于查询分页的相册多媒体数据
 *
 * @author zhongjh
 * @date 2023/7/26
 */
public class MediaPageLoader extends BaseMediaLoader {

    private final String TAG = "MediaPageLoader";

    /**
     * Media file database field
     */
    @SuppressLint("InlinedApi")
    private static final String[] PROJECTION_PAGE = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.MediaColumns.DURATION,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DISPLAY_NAME,
            COLUMN_BUCKET_ID,
            MediaStore.MediaColumns.DATE_ADDED};

    public MediaPageLoader(Context context) {
        super(context);
    }

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
                    } else {
                        String orderBy = page == -1 ? MediaStore.Files.FileColumns._ID + " DESC" : MediaStore.Files.FileColumns._ID + " DESC limit " + limit + " offset " + (page - 1) * pageSize;
                        data = mContext.getContentResolver().query(QUERY_URI, PROJECTION_PAGE, getPageSelection(bucketId), getPageSelectionArgs(bucketId), orderBy);
                    }
                    // 构造数据
                    if (data != null) {

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
        if (AlbumSpec.INSTANCE.onlyShowImages()) {
            // 只查询图片
            return getSelectionArgsForPageSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE, bucketId);
        } else if (AlbumSpec.INSTANCE.onlyShowVideos()) {
            // 只查询视频
            return getSelectionArgsForPageSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO, bucketId);
        } else {
            // 查询所有
            if (bucketId == -1) {
                // 查询全部
                return new String[]{
                        String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                        String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
                };
            }
            //  基于专辑ID查询全部
            return new String[]{
                    String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                    String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
                    String.valueOf(bucketId)
            };
        }
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

    /**
     * 将Cursor data数据构造回LocalMedia返回
     *
     * @param data Cursor数据
     * @return List<LocalMedia>
     */
    private List<LocalMedia> getLocalMedias(Cursor data) {
        List<LocalMedia> result = new ArrayList<>();
        if (data.getCount() > 0) {
            int idColumn = data.getColumnIndexOrThrow(PROJECTION_PAGE[0]);
            int dataColumn = data.getColumnIndexOrThrow(PROJECTION_PAGE[1]);
            int mimeTypeColumn = data.getColumnIndexOrThrow(PROJECTION_PAGE[2]);
            int widthColumn = data.getColumnIndexOrThrow(PROJECTION_PAGE[3]);
            int heightColumn = data.getColumnIndexOrThrow(PROJECTION_PAGE[4]);
            int durationColumn = data.getColumnIndexOrThrow(PROJECTION_PAGE[5]);
            int sizeColumn = data.getColumnIndexOrThrow(PROJECTION_PAGE[6]);
            int folderNameColumn = data.getColumnIndexOrThrow(PROJECTION_PAGE[7]);
            int fileNameColumn = data.getColumnIndexOrThrow(PROJECTION_PAGE[8]);
            int bucketIdColumn = data.getColumnIndexOrThrow(PROJECTION_PAGE[9]);
            int dateAddedColumn = data.getColumnIndexOrThrow(PROJECTION_PAGE[10]);
            data.moveToFirst();
            do {
                long id = data.getLong(idColumn);
                String mimeType = data.getString(mimeTypeColumn);
                mimeType = TextUtils.isEmpty(mimeType) ? MimeType.JPEG.getMimeTypeName() : mimeType;
                String absolutePath = data.getString(dataColumn);
                String url = SdkVersionUtils.isQ() ? MediaLoader.getRealPathUri(id, mimeType) : absolutePath;
                if (TextUtils.isEmpty(absolutePath) || new File(absolutePath).exists()) {
                    continue;
                }
                // 解决了部分获取mimeType后返回image/*格式的问题，比如小米8、9、10和其他型号会有该问题
                if (mimeType.endsWith("image/*")) {
                    if (MimeTypeUtils.isContent(url)) {
                        mimeType = MimeTypeUtils.getImageMimeType(absolutePath);
                    } else {
                        mimeType = MimeTypeUtils.getImageMimeType(url);
                    }
                    if (!albumSpec.isSupportGif()) {
                        if (MimeTypeUtils.isGif(mimeType)) {
                            continue;
                        }
                    }
                }
                // 后面可以在这里增加筛选 image/webp image/bmp
                int width = data.getInt(widthColumn);
                int height = data.getInt(heightColumn);
                long duration = data.getLong(durationColumn);
                long size = data.getLong(sizeColumn);
                String folderName = data.getString(folderNameColumn);
                String fileName = data.getString(fileNameColumn);
                long bucketId = data.getLong(bucketIdColumn);
                // 后面可以在这里增加筛选文件比配置大的continue


            }
        }
    }

}
