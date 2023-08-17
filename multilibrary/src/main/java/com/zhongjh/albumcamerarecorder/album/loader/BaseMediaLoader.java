package com.zhongjh.albumcamerarecorder.album.loader;


import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;

import com.zhongjh.albumcamerarecorder.settings.AlbumSpec;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.common.utils.SdkVersionUtils;

import java.util.Locale;

/**
 * 父类的资源
 *
 * @author zhongjh
 */
public class BaseMediaLoader {

    /**
     * 来自于多媒体的数据源标记
     */
    protected static final Uri QUERY_URI = MediaStore.Files.getContentUri("external");
    protected static final String COLUMN_BUCKET_ID = "bucket_id";

    protected final Context mContext;
    protected final GlobalSpec globalSpec = GlobalSpec.INSTANCE;
    protected final AlbumSpec albumSpec = AlbumSpec.INSTANCE;

    public BaseMediaLoader(Context context) {
        this.mContext = context;
    }

    /**
     * 构造多媒体最大值查询条件的字符串
     *
     * @return 多媒体最大值查询条件的构造字符串
     */
    protected String getFileSizeCondition() {
        long maxS = albumSpec.getFilterMaxFileSize() == 0 ? Long.MAX_VALUE : albumSpec.getFilterMaxFileSize();
        return String.format(Locale.CHINA, MediaStore.MediaColumns.SIZE + ">%s %d and " + MediaStore.MediaColumns.SIZE + " <= %d",
                Math.max(0, albumSpec.getFilterMinFileSize()) == 0 ? "" : "=",
                Math.max(0, albumSpec.getFilterMinFileSize()),
                maxS);
    }

    /**
     * 构造视频的时长条件字符串
     *
     * @return 视频的时长条件字符串
     */
    protected String getDurationCondition() {
        long maxS = AlbumSpec.INSTANCE.getVideoMaxSecond() == 0 ? Long.MAX_VALUE : AlbumSpec.INSTANCE.getVideoMaxSecond();
        String duration;
        if (SdkVersionUtils.isQ()) {
            duration = MediaStore.MediaColumns.DURATION;
        } else {
            duration = "duration";
        }
        return String.format(Locale.CHINA, duration + ">%s %d and " + duration + " <= %d",
                Math.max((long) 0, AlbumSpec.INSTANCE.getVideoMinSecond()) == 0 ? "" : "=",
                Math.max((long) 0, AlbumSpec.INSTANCE.getVideoMinSecond()),
                maxS);
    }

}
