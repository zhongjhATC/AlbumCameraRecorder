package com.zhongjh.albumcamerarecorder.album.loader;


import android.provider.MediaStore;

import com.zhongjh.albumcamerarecorder.settings.AlbumSpec;
import com.zhongjh.common.utils.SdkVersionUtils;

import java.util.Locale;

/**
 * 父类的资源
 *
 * @author zhongjh
 */
public class BaseMediaLoader {

    protected static final String COLUMN_BUCKET_ID = "bucket_id";

    /**
     * 构造多媒体最大值查询条件的字符串
     *
     * @return 多媒体最大值查询条件的构造字符串
     */
    protected String getFileSizeCondition() {
        // 获取文件最大值
        return String.format(Locale.CHINA, MediaStore.MediaColumns.SIZE + " > 0 and " + MediaStore.MediaColumns.SIZE + " <= %d",
                Long.MAX_VALUE);
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
        return String.format(Locale.CHINA, "%d <%s " + duration + " and " + duration + " <= %d",
                Math.max((long) 0, AlbumSpec.INSTANCE.getVideoMinSecond()),
                Math.max((long) 0, AlbumSpec.INSTANCE.getVideoMinSecond()) == 0 ? "" : "=",
                maxS);
    }

}
