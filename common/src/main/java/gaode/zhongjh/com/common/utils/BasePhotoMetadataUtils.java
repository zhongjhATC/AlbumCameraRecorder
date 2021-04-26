package gaode.zhongjh.com.common.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * @author zhongjh
 */
public class BasePhotoMetadataUtils {

    private static final String SCHEME_CONTENT = "content";

    /**
     * 查询图片
     *
     * @param resolver ContentResolver共享数据库
     * @param uri      图片的uri
     * @return 图片路径
     */
    public static String getPath(ContentResolver resolver, Uri uri) {
        if (uri == null) {
            return null;
        }

        if (SCHEME_CONTENT.equals(uri.getScheme())) {
            try (Cursor cursor = resolver.query(uri, new String[]{MediaStore.Images.ImageColumns.DATA},
                    null, null, null)) {
                if (cursor == null || !cursor.moveToFirst()) {
                    return null;
                }
                return cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
            }
        }
        return uri.getPath();
    }

}
