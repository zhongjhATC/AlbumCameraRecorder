package com.zhongjh.albumcamerarecorder.album.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.zhongjh.albumcamerarecorder.album.listener.OnQueryDataListener;
import com.zhongjh.albumcamerarecorder.settings.AlbumSpec;
import com.zhongjh.albumcamerarecorder.settings.CameraSpec;
import com.zhongjh.common.utils.ThreadUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 调用Android系统自带方法查询图片数据库
 * 1. 每个查询条件会判断 Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q,如果低于Q版本则加入Group by bucket_id
 * 2. ContentResolver().query() 会生成 "WHERE (1 = 1) AND (xxxx)",所以 GROUP_BY_BUCKET_Id 要自带一个半括号"("
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
                if (data != null) {
                    int count = data.getCount();
                    int totalCount = 0;
                    List<LocalMediaFolder> mediaFolders = new ArrayList<>();
                }
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
     * @return
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

}
