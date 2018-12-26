package com.zhongjh.cameraviewsoundrecorder.album.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;


import com.zhongjh.cameraviewsoundrecorder.R;
import com.zhongjh.cameraviewsoundrecorder.album.entity.IncapableCause;
import com.zhongjh.cameraviewsoundrecorder.album.entity.Item;
import com.zhongjh.cameraviewsoundrecorder.settings.AlbumSpec;
import com.zhongjh.cameraviewsoundrecorder.album.enums.MimeType;
import com.zhongjh.cameraviewsoundrecorder.album.filter.Filter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public final class PhotoMetadataUtils {
    private static final String TAG = PhotoMetadataUtils.class.getSimpleName();
    private static final int MAX_WIDTH = 1600;
    private static final String SCHEME_CONTENT = "content";

    private PhotoMetadataUtils() {
        throw new AssertionError("oops! the utility class is about to be instantiated...");
    }

    public static int getPixelsCount(ContentResolver resolver, Uri uri) {
        Point size = getBitmapBound(resolver, uri);
        return size.x * size.y;
    }

    /**
     *
     * @param uri 图片uri
     * @param activity 界面
     * @return xy
     */
    public static Point getBitmapSize(Uri uri, Activity activity) {
        ContentResolver resolver = activity.getContentResolver(); // ContentResolver共享数据库
        Point imageSize = getBitmapBound(resolver, uri);
        int w = imageSize.x;
        int h = imageSize.y;
        // 判断图片是否旋转了
        if (PhotoMetadataUtils.shouldRotate(resolver, uri)) {
            w = imageSize.y;
            h = imageSize.x;
        }
        if (h == 0) return new Point(MAX_WIDTH, MAX_WIDTH);
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        // 获取屏幕的宽度高度
        float screenWidth = (float) metrics.widthPixels;
        float screenHeight = (float) metrics.heightPixels;
        // 屏幕宽度 / 图片宽度 = ？？
        float widthScale = screenWidth / w;
        float heightScale = screenHeight / h;
//        if (widthScale > heightScale) {
//            return new Point((int) (w * widthScale), (int) (h * heightScale));
//        }
        // ?? * 宽度
        return new Point((int) (w * widthScale), (int) (h * heightScale));
    }

    /**
     * 获取长度和宽度
     * @param resolver ContentResolver共享数据库
     * @param uri 图片uri
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
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 查询图片
     * @param resolver ContentResolver共享数据库
     * @param uri 图片的uri
     * @return 图片路径
     */
    public static String getPath(ContentResolver resolver, Uri uri) {
        if (uri == null) {
            return null;
        }

        if (SCHEME_CONTENT.equals(uri.getScheme())) {
            Cursor cursor = null;
            try {
                cursor = resolver.query(uri, new String[]{MediaStore.Images.ImageColumns.DATA},
                        null, null, null);
                if (cursor == null || !cursor.moveToFirst()) {
                    return null;
                }
                return cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return uri.getPath();
    }

    /**
     * 过滤文件
     * @param context
     * @param item
     * @return
     */
    public static IncapableCause isAcceptable(Context context, Item item) {
        // 判断资源类型是否已设置可选
        if (!isSelectableType(context, item)) {
            return new IncapableCause(context.getString(R.string.error_file_type));
        }

        // 过滤不符合用户设定的资源 Filter提供抽象方法，由用户自行设置过滤规则
        if (AlbumSpec.getInstance().filters != null) {
            for (Filter filter : AlbumSpec.getInstance().filters) {
                IncapableCause incapableCause = filter.filter(context, item);
                if (incapableCause != null) {
                    return incapableCause;
                }
            }
        }
        return null;
    }

    /**
     * 判断资源类型是否已设置可选
     * @param context 上下文
     * @param item 数据源
     * @return
     */
    private static boolean isSelectableType(Context context, Item item) {
        if (context == null) {
            return false;
        }

        ContentResolver resolver = context.getContentResolver();
        // 循环当前类型配置
        for (MimeType type : AlbumSpec.getInstance().mimeTypeSet) {
            // 如果当前类型配置 相等 当前数据
            if (type.checkType(resolver, item.getContentUri())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否应该纠正旋转
     * @param resolver  ContentResolver共享数据库
     * @param uri 图片uri
     * @return 如果图片本身旋转了90或者270就返回是，需要纠正，否则否
     */
    private static boolean shouldRotate(ContentResolver resolver, Uri uri) {
        // 获取 ExifInterface,实际上Exif格式就是在JPEG格式头部插入了数码照片的信息，包括拍摄时的光圈、快门、白平衡、ISO、焦距、日期时间等各种和拍摄条件以及相机品牌、型号、色彩编码、拍摄时录制的声音以及GPS全球定位系统数据、缩略图等。
        ExifInterface exif;
        try {
            exif = ExifInterfaceCompat.newInstance(getPath(resolver, uri));
        } catch (IOException e) {
            Log.e(TAG, "could not read exif info of the image: " + uri);
            return false;
        }
        // 获取图片的旋转
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
        return orientation == ExifInterface.ORIENTATION_ROTATE_90
                || orientation == ExifInterface.ORIENTATION_ROTATE_270;
    }

    /**
     * bytes转换mb
     * @param sizeInBytes 容量大小
     * @return mb
     */
    public static float getSizeInMB(long sizeInBytes) {
        DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        df.applyPattern("0.0");
        String result = df.format((float) sizeInBytes / 1024 / 1024);
        Log.e(TAG, "getSizeInMB: " + result);
        result = result.replaceAll(",", "."); // in some case , 0.0 will be 0,0
        return Float.valueOf(result);
    }
}
