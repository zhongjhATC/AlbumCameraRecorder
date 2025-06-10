package com.zhongjh.multimedia.album.utils;

import android.content.ContentResolver;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.util.Log;

import com.zhongjh.common.utils.BasePhotoMetadataUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * @author zhongjh
 */
public final class PhotoMetadataUtils extends BasePhotoMetadataUtils {
    private static final String TAG = PhotoMetadataUtils.class.getSimpleName();


    private PhotoMetadataUtils() {
        throw new AssertionError("oops! the utility class is about to be instantiated...");
    }

    /**
     * 获取长度和宽度
     *
     * @param resolver ContentResolver共享数据库
     * @param uri      图片uri
     * @return xy
     */
    public static Point getBitmapBound(ContentResolver resolver, Uri uri) {
        InputStream is = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            is = resolver.openInputStream(uri);
            BitmapFactory.decodeStream(is, null, options);
            int width = options.outWidth;
            int height = options.outHeight;
            return new Point(width, height);
        } catch (FileNotFoundException e) {
            return new Point(0, 0);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, "getBitmapBound" + e.getMessage());
                }
            }
        }
    }

    /**
     * bytes转换mb
     *
     * @param sizeInBytes 容量大小
     * @return mb
     */
    public static float getSizeInMb(long sizeInBytes) {
        DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        df.applyPattern("0.0");
        String result = df.format((float) sizeInBytes / 1024 / 1024);
        Log.d(TAG, "getSizeInMB: " + result);
        // in some case , 0.0 will be 0,0
        result = result.replaceAll(",", ".");
        return Float.parseFloat(result);
    }
}
