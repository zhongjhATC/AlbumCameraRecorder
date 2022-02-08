package com.zhongjh.common.utils;

import android.text.TextUtils;

/**
 * 类型工具类
 *
 * @author zhongjh
 * @date 2022/2/8
 */
public class MimeTypeUtils {

    /**
     * is content://
     *
     * @param uri uri
     * @return 判断uri是否content类型
     */
    public static boolean isContent(String uri) {
        if (TextUtils.isEmpty(uri)) {
            return false;
        }
        return uri.startsWith("content://");
    }

}
