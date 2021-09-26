package com.zhongjh.albumcamerarecorder;

import android.content.Context;

import com.zhongjh.albumcamerarecorder.camera.util.FileUtil;

import java.io.File;

/**
 * 开放的一些公共方法，主要是不依赖于GlobalSetting等设置
 * @author zhongjh
 * @date 2021/9/26
 */
public class AlbumCameraRecorderApi {

    /**
     * 获取缓存的文件大小
     * @param context 上下文
     * @return 以 （xxxx + 单位） 的字符串形式返回，例如13B,13KB,13MB,13GB
     */
    public static String getFileSize(Context context) {
        File file = new File(context.getExternalCacheDir().getPath());
        return FileUtil.getSize(file);
    }

    /**
     * 删除所有缓存文件
     * @param context 上下文
     */
    public static void deleteCacheDirFile(Context context) {
        FileUtil.deleteDir(context.getExternalCacheDir());
    }

}
