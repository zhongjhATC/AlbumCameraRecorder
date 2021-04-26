package com.zhongjh.albumcamerarecorder.camera.util;

import java.io.File;

/**
 * 文件工具类
 * @author zhongjh
 */
public class FileUtil {

    public static boolean deleteFile(String url) {
        boolean result = false;
        File file = new File(url);
        if (file.exists()) {
            result = file.delete();
        }
        return result;
    }

    public static boolean deleteFile(File file) {
        boolean result = false;
        if (file.exists()) {
            result = file.delete();
        }
        return result;
    }
}
