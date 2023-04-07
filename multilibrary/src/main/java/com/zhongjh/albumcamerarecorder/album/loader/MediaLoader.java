package com.zhongjh.albumcamerarecorder.album.loader;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.zhongjh.albumcamerarecorder.album.entity.Album;
import com.zhongjh.albumcamerarecorder.album.entity.Album2;
import com.zhongjh.albumcamerarecorder.album.listener.OnQueryDataListener;
import com.zhongjh.albumcamerarecorder.settings.AlbumSpec;
import com.zhongjh.albumcamerarecorder.settings.CameraSpec;
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
 *
 * @author zhongjh
 * @date 2022/9/9
 */
public class MediaLoader {

    /**
     * 来自于多媒体的数据源标记
     */
    private static final Uri QUERY_URI = MediaStore.Files.getContentUri("external");
    private static final String ORDER_BY = MediaStore.Files.FileColumns._ID + " DESC";
    private static final String GROUP_BY_BUCKET_ID = " GROUP BY (bucket_id";
    private static final String COLUMN_COUNT = "count";
    private static final String COLUMN_BUCKET_ID = "bucket_id";
    private static final String COLUMN_BUCKET_DISPLAY_NAME = "bucket_display_name";

    private final Context mContext;

    public MediaLoader(Context context) {
        this.mContext = context;
    }

    /**
     * 获取所有文件夹
     * 会通过 SdkVersionUtils.isQ 判断
     * SDK 29 以下的版本可以直接获取Data和
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
                        getCondition(),
                        // 配合上面的参数使用，上面的参数使用占位符"?"，那么这个参的数据会替换掉占位符"?"
                        getSelectionArgs(),
                        // 排序
                        ORDER_BY);
                if (data != null) {
                    int count = data.getCount();
                    int totalCount = 0;
                    List<Album2> albums = new ArrayList<>();
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
                        allMediaFolder.setFirstImagePath(SdkVersionUtils.isQ() ? getFirstUri(data) : getFirstUrl(data));
                        allMediaFolder.setFirstMimeType(getFirstCoverMimeType(data));
                    }
                }


                return null;
            }

            @Override
            public void onSuccess(ArrayList<Album2> result) {

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
    private String getCondition() {
        String fileSizeCondition = getFileSizeCondition();
        if (AlbumSpec.INSTANCE.onlyShowImages()) {
            // 只查询图片
            return getSelectionByImageCondition(fileSizeCondition);
        } else if (AlbumSpec.INSTANCE.onlyShowVideos()) {
            // 只查询视频
            return getSelectionByVideoCondition(fileSizeCondition);
        } else {
            // 查询所有
            return getSelectionByAllCondition(getDurationCondition(), fileSizeCondition);
        }
    }

    /**
     * 构造多媒体最大值查询条件的字符串
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
     * 构造视频的时长条件字符串
     *
     * @return 视频的时长条件字符串
     */
    private String getDurationCondition() {
        long maxS = CameraSpec.INSTANCE.getMaxDuration() == 0 ? Long.MAX_VALUE : CameraSpec.INSTANCE.getMaxDuration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return String.format(Locale.CHINA, "%d <%s " + MediaStore.MediaColumns.DURATION + " and " + MediaStore.MediaColumns.DURATION + " <= %d",
                    Math.max((long) 0, CameraSpec.INSTANCE.getMinDuration()),
                    Math.max((long) 0, CameraSpec.INSTANCE.getMinDuration()) == 0 ? "" : "=",
                    maxS);
        } else {
            return String.format(Locale.CHINA, "%d <%s duration and duration <= %d",
                    Math.max((long) 0, CameraSpec.INSTANCE.getMinDuration()),
                    Math.max((long) 0, CameraSpec.INSTANCE.getMinDuration()) == 0 ? "" : "=",
                    maxS);
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
        stringBuilder.append("(").append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?").append(" OR ")
                .append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=? AND ").append(durationCondition).append(") AND ").append(fileSizeCondition);
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
                Album2 album = new Album2(String.valueOf(bucketId), firstImagePath, bucketDisplayName, size);
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
            Album2 album = new Album2(String.valueOf(bucketId), url, bucketDisplayName, size);
            albums.add(album);
            totalCount += size;
        } while (data.moveToNext());
        return totalCount;
    }

}
