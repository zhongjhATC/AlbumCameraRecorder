package com.zhongjh.albumcamerarecorder.album.loader;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.album.entity.Album2;
import com.zhongjh.albumcamerarecorder.album.entity.MediaData;
import com.zhongjh.albumcamerarecorder.album.listener.OnQueryDataListener;
import com.zhongjh.albumcamerarecorder.album.listener.OnQueryDataPageListener;
import com.zhongjh.albumcamerarecorder.album.utils.SortUtils;
import com.zhongjh.albumcamerarecorder.constants.ModuleTypes;
import com.zhongjh.albumcamerarecorder.settings.AlbumSpec;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.common.enums.MimeType;
import com.zhongjh.common.utils.SdkVersionUtils;
import com.zhongjh.common.utils.ThreadUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 调用Android系统自带方法查询图片数据库
 * 1. 每个查询条件会判断 Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q,如果低于Q版本则加入Group by bucket_id,理由是Q版本源码改变，已经无法Group by bucket_id了
 * 2. ContentResolver().query() 会生成 "WHERE (1 = 1) AND (xxxx)",所以 GROUP_BY_BUCKET_Id 要自带一个半括号"("
 * 3. 同时为了达到Group by效果，>= Q的版本会查询所有数据，然后进行数据分组
 * <p>
 * loadAllMedia 是获取专辑的
 * loadPageMediaData 这个是获取图片的
 * <p>
 * 参考pictureselector的master分支的LocalMediaPageLoader类
 * <p>
 * Q版本以下直接使用COUNT(*)会报错以下错误：
 * java.lang.IllegalArgumentException: Invalid column COUNT(*) AS count
 * 所以需要做相关兼容
 *
 * @author zhongjh
 * @date 2022/9/9
 */
public class MediaLoader extends BaseMediaLoader {

    private final String TAG = "MediaLoader";
    /**
     * 来自于多媒体的数据源标记
     */
    private static final Uri QUERY_URI = MediaStore.Files.getContentUri("external");
    private static final String ORDER_BY = MediaStore.Files.FileColumns._ID + " DESC";
    private static final String GROUP_BY_BUCKET_ID = " GROUP BY (bucket_id";
    private static final String COLUMN_COUNT = "count";
    private static final String COLUMN_BUCKET_DISPLAY_NAME = "bucket_display_name";

    private final Context mContext;
    private GlobalSpec globalSpec = GlobalSpec.INSTANCE;

    public MediaLoader(Context context) {
        this.mContext = context;
    }

    /**
     * 获取所有文件夹(专辑)
     * 会通过 SdkVersionUtils.isQ 判断
     * SDK 29 以下的版本可以直接通过sql语句获取Data和针对bucket_id分组
     *
     * @param listener 回调事件
     */
    public void loadAllMedia(OnQueryDataListener<Album2> listener) {
        ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<ArrayList<Album2>>() {
            @Override
            public ArrayList<Album2> doInBackground() {
                // 查询Android数据库
                Cursor data = mContext.getContentResolver().query(
                        // 查询数据来源的标记
                        QUERY_URI,
                        // 需要查询的列
                        SdkVersionUtils.isQ() ? PROJECTION_29 : PROJECTION,
                        // 查询条件，包括group by
                        getSelection(),
                        // 配合上面的参数使用，上面的参数使用占位符"?"，那么这个参的数据会替换掉占位符"?"
                        getSelectionArgs(),
                        // 排序
                        ORDER_BY);
                if (data != null) {
                    int count = data.getCount();
                    int totalCount = 0;
                    ArrayList<Album2> albums = new ArrayList<>();
                    if (count > 0) {
                        if (SdkVersionUtils.isQ()) {
                            // >= Q的版本会查询所有数据，需要针对bucket_id进行分组
                            totalCount = setGroupByBucketIdBy29(data, albums);
                        } else {
                            totalCount = setGroupByBucketId(data, albums);
                        }
                    }
                    // 添加一个所有相机胶卷专辑
                    Album2 allAlbum = new Album2();
                    if (data.moveToFirst()) {
                        allAlbum.setFirstImagePath(SdkVersionUtils.isQ() ? getFirstUri(data) : getFirstUrl(data));
                        allAlbum.setFirstMimeType(getFirstCoverMimeType(data));
                    }
                    SortUtils.sortFolder(albums);
                    // 多媒体总数
                    allAlbum.setCount(totalCount);
                    allAlbum.setChecked(true);
                    allAlbum.setId(-1);
                    String bucketDisplayName = mContext.getString(R.string.z_multi_library_album_name_all);
                    allAlbum.setName(bucketDisplayName);
                    allAlbum.setType(globalSpec.getMimeTypeSet(ModuleTypes.ALBUM));
                    albums.add(0, allAlbum);
                    return albums;
                }
                return new ArrayList<>();
            }

            @Override
            public void onSuccess(ArrayList<Album2> result) {
                if (listener != null) {
                    Log.d(TAG, "专辑数组长度" + result.size());
                    listener.onComplete(result);
                }
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
            }
        });
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
     * 构造 根据配置而定制的查询条件
     *
     * @return 查询条件字符串
     */
    private String getSelection() {
        String fileSizeCondition = getFileSizeCondition();
        if (AlbumSpec.INSTANCE.onlyShowImages()) {
            // 只查询图片
            return getSelectionByImageCondition(fileSizeCondition);
        } else if (AlbumSpec.INSTANCE.onlyShowVideos()) {
            // 只查询视频
            return getSelectionByVideoCondition(fileSizeCondition);
        } else {
            // 查询所有
            String sql = getSelectionByAllCondition(getDurationCondition(), fileSizeCondition);
            Log.d(TAG, "sql: " + sql);
            return sql;
        }
    }


    /**
     * 构造查询条件字符串 - 所有
     *
     * @param durationCondition 视频的时长条件字符串
     * @param fileSizeCondition 多媒体最大值查询条件字符串
     * @return 查询条件字符串 - 所有
     */
    private static String getSelectionByAllCondition(String durationCondition, String fileSizeCondition) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("((").append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?").append(" OR ")
                .append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?) AND ").append(durationCondition).append(") AND ").append(fileSizeCondition);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return stringBuilder.toString();
        } else {
            return stringBuilder.append(")").append(GROUP_BY_BUCKET_ID).toString();
        }
    }

    /**
     * 构造查询条件字符串 - 图片
     *
     * @param fileSizeCondition 多媒体最大值查询条件字符串
     * @return 条件字符串
     */
    private static String getSelectionByImageCondition(String fileSizeCondition) {
        StringBuilder stringBuilder = new StringBuilder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return stringBuilder.append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?").append(fileSizeCondition).toString();
        } else {
            return stringBuilder.append("(").append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?").append(") AND ").append(fileSizeCondition).append(")").append(GROUP_BY_BUCKET_ID).toString();
        }
    }

    /**
     * 构造查询条件字符串 - 视频
     *
     * @param fileSizeCondition 多媒体最大值查询条件字符串
     * @return 条件字符串
     */
    private static String getSelectionByVideoCondition(String fileSizeCondition) {
        StringBuilder stringBuilder = new StringBuilder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return stringBuilder.append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?").append(" AND ").append(fileSizeCondition).toString();
        } else {
            return stringBuilder.append("(").append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?").append(") AND ").append(fileSizeCondition).append(")").append(GROUP_BY_BUCKET_ID).toString();
        }
    }

    /**
     * 构造参数
     *
     * @return 参数
     */
    private String[] getSelectionArgs() {
        if (AlbumSpec.INSTANCE.onlyShowImages()) {
            // 获取有关查询图片的参数
            return getSelectionArgsBySingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE);
        } else if (AlbumSpec.INSTANCE.onlyShowVideos()) {
            // 获取有关查询视频的参数
            return getSelectionArgsBySingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO);
        } else {
            // 获取有关查询所有的参数
            return getSelectionArgsByAllMediaType();
        }
    }

    /**
     * 构造 图片、视频类型 的参数
     *
     * @return 参数数组
     */
    private static String[] getSelectionArgsByAllMediaType() {
        return new String[]{String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE), String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)};
    }

    /**
     * 构造指定类型的参数
     *
     * @param mediaType 类型
     * @return 参数数组
     */
    private static String[] getSelectionArgsBySingleMediaType(int mediaType) {
        return new String[]{String.valueOf(mediaType)};
    }

    /**
     * 根据 bucketId 和 mimeType 获取uri
     *
     * @param bucketId bucketId
     * @param mimeType mimeType
     * @return uri
     */
    private static String getRealPathUri(long bucketId, String mimeType) {
        Uri contentUri;
        if (MimeType.isImageOrGif(mimeType)) {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if (MimeType.isVideo(mimeType)) {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if (MimeType.isAudio(mimeType)) {
            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        } else {
            contentUri = MediaStore.Files.getContentUri("external");
        }
        return ContentUris.withAppendedId(contentUri, bucketId).toString();
    }

    /**
     * >= Q的版本会查询所有数据，需要代码针对bucket_id进行分组
     *
     * @param data   查询出来数据源，针对该数据源进行分组
     * @param albums 最后整理成专辑添加入该列表
     * @return totalCount 所有照片总数，用于后面添加一个"所有"专辑计算数量
     */
    private int setGroupByBucketIdBy29(Cursor data, List<Album2> albums) {
        int totalCount = 0;
        Map<Long, Long> countMap = new HashMap<>();
        // data 循环移动到下一位执行逻辑 添加到countMap，如果有同个bucket_id，则value+1
        while (data.moveToNext()) {
            long bucketId = data.getLong(data.getColumnIndexOrThrow(COLUMN_BUCKET_ID));
            Long newCount = countMap.get(bucketId);
            if (newCount == null) {
                newCount = 1L;
            } else {
                newCount++;
            }
            countMap.put(bucketId, newCount);
        }

        // 上一轮已经跑到最后一个索引，现在重新回到第一个索引
        if (data.moveToFirst()) {
            Set<Long> hashSet = new HashSet<>();
            // 先执行逻辑，data再移动到下一位执行逻辑
            do {
                long bucketId = data.getLong(data.getColumnIndexOrThrow(COLUMN_BUCKET_ID));
                if (hashSet.contains(bucketId)) {
                    continue;
                }
                String bucketDisplayName = data.getString(data.getColumnIndexOrThrow(COLUMN_BUCKET_DISPLAY_NAME));
                String mimeType = data.getString(data.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE));
                Long size = countMap.get(bucketId);
                size = size == null ? 0 : size;
                long id = data.getLong(data.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID));
                String firstImagePath = getRealPathUri(id, mimeType);
                Album2 album = new Album2();
                album.setId(bucketId);
                album.setFirstImagePath(firstImagePath);
                album.setName(bucketDisplayName);
                album.setCount(Integer.parseInt(String.valueOf(size)));
                albums.add(album);
                hashSet.add(bucketId);
                totalCount += size;
            } while (data.moveToNext());
        }
        return totalCount;
    }

    /**
     * < Q的版本会查询所有数据，数据库查询已经针对bucket_id分组
     *
     * @param data   查询出来数据源，进行相关处理
     * @param albums 最后整理成专辑添加入该列表
     * @return totalCount 所有照片总数，用于后面添加一个"所有"专辑计算数量
     */
    private int setGroupByBucketId(Cursor data, List<Album2> albums) {
        int totalCount = 0;
        data.moveToFirst();
        do {
            long bucketId = data.getLong(data.getColumnIndexOrThrow(COLUMN_BUCKET_ID));
            String bucketDisplayName = data.getString(data.getColumnIndexOrThrow(COLUMN_BUCKET_DISPLAY_NAME));
            int size = data.getInt(data.getColumnIndexOrThrow(COLUMN_COUNT));
            String url = data.getString(data.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
            Album2 album = new Album2();
            album.setId(bucketId);
            album.setFirstImagePath(url);
            album.setName(bucketDisplayName);
            album.setCount(size);
            albums.add(album);
            totalCount += size;
        } while (data.moveToNext());
        return totalCount;
    }

    /**
     * 根据游标获取uri
     *
     * @param cursor 游标
     * @return uri
     */
    private static String getFirstUri(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID));
        String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE));
        return getRealPathUri(id, mimeType);
    }

    /**
     * 根据游标获取mimeType
     *
     * @param cursor 游标
     * @return mimeType
     */
    private static String getFirstCoverMimeType(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE));
    }

    /**
     * 根据游标获取path
     *
     * @param cursor 游标
     * @return url path
     */
    private static String getFirstUrl(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA));
    }

}
