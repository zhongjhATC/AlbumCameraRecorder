//package com.zhongjh.albumcamerarecorder.album.loader;
//
//import android.annotation.SuppressLint;
//import android.content.ContentResolver;
//import android.content.ContentUris;
//import android.content.Context;
//import android.database.Cursor;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Bundle;
//import android.provider.MediaStore;
//import android.text.TextUtils;
//import android.util.Log;
//
//import com.zhongjh.albumcamerarecorder.album.entity.MediaData;
//import com.zhongjh.albumcamerarecorder.album.listener.OnLoadPageMediaDataListener;
//import com.zhongjh.albumcamerarecorder.constants.ModuleTypes;
//import com.zhongjh.albumcamerarecorder.settings.AlbumSpec;
//import com.zhongjh.common.entity.LocalMedia;
//import com.zhongjh.common.enums.MimeType;
//import com.zhongjh.common.utils.SdkVersionUtils;
//import com.zhongjh.common.utils.ThreadUtils;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * 用于查询分页的相册多媒体数据
// *
// * @author zhongjh
// * @date 2023/7/26
// */
//public class MediaPageLoader extends BaseMediaLoader {
//
//    private static final String TAG = "MediaPageLoader";
//
//    /**
//     * Media file database field
//     */
//    @SuppressLint("InlinedApi")
//    private static final String[] PROJECTION_PAGE = {
//            MediaStore.Files.FileColumns._ID,
//            MediaStore.MediaColumns.DATA,
//            MediaStore.MediaColumns.MIME_TYPE,
//            MediaStore.MediaColumns.WIDTH,
//            MediaStore.MediaColumns.HEIGHT,
//            MediaStore.MediaColumns.DURATION,
//            MediaStore.MediaColumns.ORIENTATION,
//            MediaStore.MediaColumns.SIZE,
//            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
//            MediaStore.MediaColumns.DISPLAY_NAME,
//            COLUMN_BUCKET_ID,
//            MediaStore.MediaColumns.DATE_ADDED};
//
//    public MediaPageLoader(Context context) {
//        super(context);
//    }
//
//    /**
//     * 查询指定目录(页)中的数据
//     *
//     * @param bucketId 专辑id
//     * @param page     当前页码
//     * @param limit    数据取多少个
//     * @param pageSize 每页多少个
//     * @param listener 回调事件
//     */
//    public void loadPageMediaData(long bucketId, int page, int limit, int pageSize,
//                                  OnLoadPageMediaDataListener listener) {
//        ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<MediaData>() {
//
//            @Override
//            public MediaData doInBackground() {
//                Cursor data = null;
//                try {
//                    if (SdkVersionUtils.isR()) {
//                        Bundle queryArgs = createQueryArgsBundle(getPageSelection(bucketId), getPageSelectionArgs(bucketId), limit, (page - 1) * pageSize);
//                        data = mContext.getContentResolver().query(QUERY_URI, PROJECTION_PAGE, queryArgs, null);
//                    } else {
//                        String orderBy = page == -1 ? MediaStore.Files.FileColumns._ID + " DESC" : MediaStore.Files.FileColumns._ID + " DESC limit " + limit + " offset " + (page - 1) * pageSize;
//                        data = mContext.getContentResolver().query(QUERY_URI, PROJECTION_PAGE, getPageSelection(bucketId), getPageSelectionArgs(bucketId), orderBy);
//                    }
//
//                    // 构造数据
//                    if (data != null) {
//                        Log.i(TAG, "dataCount: " + data.getCount());
//                        ArrayList<LocalMedia> result = getLocalMedias(data);
//                        return new MediaData(result, data.getCount() > 0);
//                    }
//                } catch (Exception exception) {
//                    exception.printStackTrace();
//                    Log.i(TAG, "loadMedia Page Data Error: " + exception.getMessage());
//                    return new MediaData(new ArrayList<>(), false);
//                } finally {
//                    if (data != null && !data.isClosed()) {
//                        data.close();
//                    }
//                }
//                return new MediaData(new ArrayList<>(), false);
//            }
//
//            @Override
//            public void onSuccess(MediaData result) {
//                if (listener != null) {
//                    listener.onLoadPageMediaDataComplete(result.getData(), page, result.isHasNextMore());
//                }
//            }
//
//            @Override
//            public void onCancel() {
//                super.onCancel();
//            }
//
//            @Override
//            public void onFail(Throwable t) {
//                super.onFail(t);
//            }
//
//        });
//    }
//
//    /**
//     * Android 版本R下的处理
//     * 创建Query ArgsBundle
//     *
//     * @param selection     查询语句
//     * @param selectionArgs 查询参数
//     * @param limitCount    几条开始
//     * @param offset        偏移
//     * @return bundle
//     */
//    public static Bundle createQueryArgsBundle(String selection, String[] selectionArgs, int limitCount, int offset) {
//        Bundle queryArgs = new Bundle();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection);
//            queryArgs.putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs);
//            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, MediaStore.Files.FileColumns._ID + " DESC");
//            Log.d(TAG, "selection: " + selection);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                queryArgs.putString(ContentResolver.QUERY_ARG_SQL_LIMIT, limitCount + " offset " + offset);
//                Log.d(TAG, "limitCount: " + limitCount + " offset " + offset);
//            }
//        }
//        return queryArgs;
//    }
//
//    /**
//     * 查询条件的sql语句
//     *
//     * @param bucketId 专辑ID
//     * @return 查询条件的sql语句
//     */
//    private String getPageSelection(long bucketId) {
//        String durationCondition = getDurationCondition();
//        String sizeCondition = getFileSizeCondition();
//        if (AlbumSpec.INSTANCE.onlyShowImages()) {
//            // 只查询图片
//            return getPageSelectionArgsForImageMediaCondition(bucketId, sizeCondition);
//        } else if (AlbumSpec.INSTANCE.onlyShowVideos()) {
//            // 只查询视频
//            return getPageSelectionArgsForVideoOrAudioMediaCondition(bucketId, durationCondition, sizeCondition);
//        } else {
//            // 查询所有
//            String sql = getPageSelectionArgsForAllMediaCondition(bucketId, durationCondition, sizeCondition);
//            Log.d(TAG, "sql: " + sql);
//            return sql;
//        }
//    }
//
//    /**
//     * 只查询图片
//     *
//     * @param bucketId      专辑ID
//     * @param sizeCondition 文件大小条件
//     * @return sql语句
//     */
//    private String getPageSelectionArgsForImageMediaCondition(long bucketId, String sizeCondition) {
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("(").append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?");
//        if (bucketId == -1) {
//            return stringBuilder.append(") AND ").append(sizeCondition).toString();
//        } else {
//            return stringBuilder.append(") AND ").append(COLUMN_BUCKET_ID).append("=? AND ").append(sizeCondition).toString();
//        }
//    }
//
//    /**
//     * 只查询视频
//     *
//     * @param bucketId          专辑ID
//     * @param durationCondition 视频文件的持续时间
//     * @param sizeCondition     文件大小条件
//     * @return sql语句
//     */
//    private String getPageSelectionArgsForVideoOrAudioMediaCondition(long bucketId, String durationCondition, String sizeCondition) {
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("(").append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?").append(" AND ").append(durationCondition).append(") AND ");
//        if (bucketId == -1) {
//            return stringBuilder.append(sizeCondition).toString();
//        } else {
//            return stringBuilder.append(COLUMN_BUCKET_ID).append("=? AND ").append(sizeCondition).toString();
//        }
//    }
//
//    /**
//     * 查询所有
//     * 查询条件： 图片类型 或 视频类型+帧 和 文件夹id 和 文件大小
//     * ((media_type=? OR media_type=? AND duration> 0 and duration <= 9223372036854775807))) AND bucket_id=? AND _size> 0 and _size <= 9223372036854775807
//     *
//     * @param bucketId          专辑ID
//     * @param durationCondition 视频文件的持续时间
//     * @param sizeCondition     文件大小条件
//     * @return sql语句
//     */
//    private static String getPageSelectionArgsForAllMediaCondition(long bucketId, String durationCondition, String sizeCondition) {
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("((").append(MediaStore.Files.FileColumns.MEDIA_TYPE)
//                .append("=?").append(" OR (").append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=? AND ").append(durationCondition).append("))) AND ");
//        if (bucketId == -1) {
//            return stringBuilder.append(sizeCondition).toString();
//        } else {
//            return stringBuilder.append(COLUMN_BUCKET_ID).append("=? AND ").append(sizeCondition).toString();
//        }
//    }
//
//    /**
//     * 设置参数
//     *
//     * @param bucketId 专辑ID
//     * @return 参数集合
//     */
//    private String[] getPageSelectionArgs(long bucketId) {
//        if (AlbumSpec.INSTANCE.onlyShowImages()) {
//            // 只查询图片
//            return getSelectionArgsForPageSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE, bucketId);
//        } else if (AlbumSpec.INSTANCE.onlyShowVideos()) {
//            // 只查询视频
//            return getSelectionArgsForPageSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO, bucketId);
//        } else {
//            // 查询所有
//            if (bucketId == -1) {
//                // 查询全部
//                return new String[]{
//                        String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
//                        String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
//                };
//            }
//            //  基于专辑ID查询全部
//            return new String[]{
//                    String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
//                    String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
//                    String.valueOf(bucketId)
//            };
//        }
//    }
//
//    /**
//     * 获取指定类型的文件
//     *
//     * @param mediaType 类型
//     * @return 参数数组
//     */
//    private static String[] getSelectionArgsForPageSingleMediaType(int mediaType, long bucketId) {
//        return bucketId == -1 ? new String[]{String.valueOf(mediaType)} : new String[]{String.valueOf(mediaType), String.valueOf(bucketId)};
//    }
//
//    /**
//     * 将Cursor data数据构造回LocalMedia返回
//     *
//     * @param data Cursor数据
//     * @return List<LocalMedia>
//     */
//    private ArrayList<LocalMedia> getLocalMedias(Cursor data) {
//        ArrayList<LocalMedia> result = new ArrayList<>();
//        if (data.getCount() > 0) {
//            int idColumn = data.getColumnIndexOrThrow(PROJECTION_PAGE[0]);
//            int dataColumn = data.getColumnIndexOrThrow(PROJECTION_PAGE[1]);
//            int mimeTypeColumn = data.getColumnIndexOrThrow(PROJECTION_PAGE[2]);
//            int widthColumn = data.getColumnIndexOrThrow(PROJECTION_PAGE[3]);
//            int heightColumn = data.getColumnIndexOrThrow(PROJECTION_PAGE[4]);
//            int durationColumn = data.getColumnIndexOrThrow(PROJECTION_PAGE[5]);
//            int orientationColumn = data.getColumnIndexOrThrow(PROJECTION_PAGE[6]);
//            int sizeColumn = data.getColumnIndexOrThrow(PROJECTION_PAGE[7]);
//            int folderNameColumn = data.getColumnIndexOrThrow(PROJECTION_PAGE[8]);
//            int fileNameColumn = data.getColumnIndexOrThrow(PROJECTION_PAGE[9]);
//            int bucketIdColumn = data.getColumnIndexOrThrow(PROJECTION_PAGE[10]);
//            int dateAddedColumn = data.getColumnIndexOrThrow(PROJECTION_PAGE[11]);
//            data.moveToFirst();
//            do {
//                long id = data.getLong(idColumn);
//                String mimeType = data.getString(mimeTypeColumn);
//                mimeType = TextUtils.isEmpty(mimeType) ? MimeType.JPEG.getMimeTypeName() : mimeType;
//                String absolutePath = data.getString(dataColumn);
//                String uri = SdkVersionUtils.isQ() ? getRealPathUri(id, mimeType) : absolutePath;
//                if (TextUtils.isEmpty(absolutePath)) {
//                    continue;
//                }
//                // 解决了部分获取mimeType后返回image/*格式的问题，比如小米8、9、10和其他型号会有该问题
//                if (mimeType.endsWith("image/*")) {
//                    if (MimeType.isContent(uri)) {
//                        mimeType = MimeType.getImageMimeType(absolutePath);
//                    } else {
//                        mimeType = MimeType.getImageMimeType(uri);
//                    }
//                    if (!albumSpec.isSupportGif()) {
//                        if (MimeType.isGif(mimeType)) {
//                            continue;
//                        }
//                    }
//                }
//                // 后面可以在这里增加筛选 image/webp image/bmp
//                if (!albumSpec.isSupportWebp()) {
//                    if (mimeType.startsWith(MimeType.WEBP.getMimeTypeName())) {
//                        continue;
//                    }
//                }
//                if (!albumSpec.isSupportBmp()) {
//                    if (mimeType.startsWith(MimeType.BMP.getMimeTypeName())) {
//                        continue;
//                    }
//                }
//                long duration = data.getLong(durationColumn);
//                int orientation = data.getInt(orientationColumn);
//                int width = data.getInt(widthColumn);
//                int height = data.getInt(heightColumn);
//                // 如果是横向值,修改属性
//                if (orientation == 90 || orientation == 270) {
//                    width = data.getInt(heightColumn);
//                    height = data.getInt(widthColumn);
//                }
//                long size = data.getLong(sizeColumn);
//                String folderName = data.getString(folderNameColumn);
//                String fileName = data.getString(fileNameColumn);
//                long bucketId = data.getLong(bucketIdColumn);
//
//                if (MimeType.isVideo(mimeType)) {
//                    if (albumSpec.getVideoMinSecond() > 0 && duration < albumSpec.getVideoMinSecond()) {
//                        // 如果设置了视频最小时长就判断最小时长值
//                        continue;
//                    }
//                    if (albumSpec.getVideoMaxSecond() > 0 && duration > albumSpec.getVideoMinSecond()) {
//                        // 如果设置了视频最大时长就判断最大时长值
//                        continue;
//                    }
//                    if (duration == 0) {
//                        // 如果长度为 0，则处理并过滤掉损坏的视频
//                        continue;
//                    }
//                    if (size <= 0) {
//                        // 视频大小为 0 过滤掉
//                        continue;
//                    }
//                }
//                LocalMedia image = LocalMedia.parseLocalMedia(id, uri, absolutePath, fileName, folderName, duration, orientation,
//                        mimeType, width, height, size, bucketId, data.getLong(dateAddedColumn));
//                result.add(image);
//            } while (data.moveToNext());
//        }
//        Log.d(TAG, "result.size(): " + result.size());
//        return result;
//    }
//
//    /**
//     * 根据 bucketId 和 mimeType 获取uri
//     *
//     * @param bucketId bucketId
//     * @param mimeType mimeType
//     * @return uri
//     */
//    public static String getRealPathUri(long bucketId, String mimeType) {
//        Uri contentUri;
//        if (MimeType.isImageOrGif(mimeType)) {
//            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//        } else if (MimeType.isVideo(mimeType)) {
//            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
//        } else if (MimeType.isAudio(mimeType)) {
//            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//        } else {
//            contentUri = MediaStore.Files.getContentUri("external");
//        }
//        return ContentUris.withAppendedId(contentUri, bucketId).toString();
//    }
//
//}
