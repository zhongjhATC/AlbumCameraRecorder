package com.zhongjh.common.utils

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore

/**
 *
 * @author zhongjh
 * @date 2021/11/12
 */
open class BasePhotoMetadataUtils {

    companion object {

        private val SCHEME_CONTENT = "content"

        /**
         * 查询图片
         *
         * @param resolver ContentResolver共享数据库
         * @param uri      图片的uri
         * @return 图片路径
         */
        @JvmStatic
        fun getPath(resolver: ContentResolver, uri: Uri?): String? {
            if (uri == null) {
                return null
            }

            if (SCHEME_CONTENT == uri.scheme) {
                resolver.query(uri, arrayOf(MediaStore.Images.ImageColumns.DATA),
                        null,null,null).use {
                    cursor ->
                    return if (cursor == null || !cursor.moveToFirst()) {
                        null
                    } else {
                        val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                        if (index > -1) {
                            cursor.getString(index)
                        } else {
                            null
                        }
                    }
                }
            }
            return uri.path
        }
    }

}