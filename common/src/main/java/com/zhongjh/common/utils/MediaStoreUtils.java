package com.zhongjh.common.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.IntDef;
import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.SimpleDateFormat;
import java.util.Locale;


/**
 * 相册操作常用工具类
 *
 * @author Clock
 * @author zhongjh
 * @date 2015/12/31
 * @date 2022/01/05
 */
public class MediaStoreUtils {

    @IntDef({
            MediaTypes.TYPE_PICTURE,
            MediaTypes.TYPE_VIDEO,
            MediaTypes.TYPE_AUDIO
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface MediaTypes {

        /**
         * 图片
         */
        int TYPE_PICTURE = 0x001;
        /**
         * 视频
         */
        int TYPE_VIDEO = 0x002;
        /**
         * 音频
         */
        int TYPE_AUDIO = 0x003;

    }


    /**
     * 插入图片、视频到图库
     *
     * @param context          上下文
     * @param file             要保存的文件
     * @param type             mp4 jpeg
     * @param duration         video专属的时长,图片传-1即可
     * @param width            宽
     * @param height           高
     * @param directory        子文件目录
     * @param mediaStoreCompat mediaStoreCompat
     */
    public static Uri displayToGallery(Context context, File file, @MediaTypes int type, long duration, int width, int height,
                                       String directory, MediaStoreCompat mediaStoreCompat) {
        if (file == null || !file.exists()) {
            return null;
        }
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uri = displayToGalleryAndroidQ(context, file, type, duration, width, height, directory, mediaStoreCompat);
        } else {
            String photoPath = file.getPath();
            uri = mediaStoreCompat.getUri(photoPath);
            // 添加到图库数据库
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, photoPath);
            values.put(MediaStore.Images.Media.TITLE, AppUtils.getAppName(context));
            values.put(MediaStore.Images.Media.DISPLAY_NAME, file.getName());
            values.put(MediaStore.Images.Media.SIZE, file.length());
            values.put(MediaStore.Images.Media.WIDTH, width);
            values.put(MediaStore.Images.Media.HEIGHT, height);
            switch (type) {
                case MediaTypes.TYPE_VIDEO:
                    values.put(MediaStore.Images.Media.MIME_TYPE, "video/mp4");
                    // 计算时间
                    if (duration == 0) {
                        MediaPlayer mp = MediaPlayer.create(context, uri);
                        duration = mp.getDuration();
                        mp.release();
                    }
                    values.put("duration", duration);
                    uri = context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
                    break;
                case MediaTypes.TYPE_PICTURE:
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    break;
                case MediaTypes.TYPE_AUDIO:
                    values.put(MediaStore.Audio.Media.MIME_TYPE, "video/aac");
                    // 计算时间
                    if (duration == 0) {
                        MediaPlayer mp = MediaPlayer.create(context, uri);
                        duration = mp.getDuration();
                        mp.release();
                    }
                    values.put("duration", duration);
                    uri = context.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
                    break;
                default:
                    break;
            }
            // 这个判断AndroidQ的就是用来解决ACTION_MEDIA_SCANNER_SCAN_FILE过时的方式
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
        }
        return uri;
    }

    /**
     * 插入图片、视频到图库
     * 兼容AndroidQ
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private static Uri displayToGalleryAndroidQ(Context context, File file, @MediaTypes int type, long duration, int width, int height,
                                                String directory, MediaStoreCompat mediaStoreCompat) {
        // 插入file数据到相册
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, AppUtils.getAppName(context));
        values.put(MediaStore.Images.Media.DISPLAY_NAME, file.getName());
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.ORIENTATION, 0);
        values.put(MediaStore.Images.Media.SIZE, file.length());
        values.put(MediaStore.Images.Media.WIDTH, width);
        values.put(MediaStore.Images.Media.HEIGHT, height);
        Uri external = null;
        switch (type) {
            case MediaTypes.TYPE_VIDEO:
                values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
                // 计算时间
                if (duration == 0) {
                    String photoPath = file.getPath();
                    Uri uri = mediaStoreCompat.getUri(photoPath);
                    MediaPlayer mp = MediaPlayer.create(context, uri);
                    duration = mp.getDuration();
                    mp.release();
                }
                values.put("duration", duration);
                values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + File.separator + directory);
                external = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                break;
            case MediaTypes.TYPE_PICTURE:
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + directory);
                external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                break;
            case MediaTypes.TYPE_AUDIO:
                values.put(MediaStore.Audio.Media.MIME_TYPE, "video/aac");
                // 计算时间
                if (duration == 0) {
                    String photoPath = file.getPath();
                    Uri uri = mediaStoreCompat.getUri(photoPath);
                    MediaPlayer mp = MediaPlayer.create(context, uri);
                    duration = mp.getDuration();
                    mp.release();
                }
                values.put("duration", duration);
                external = context.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
                break;
            default:
                break;
        }

        // 需要增加这个，不然AndroidQ识别不到TAG_DATETIME_ORIGINAL创建时间
        try {
            ExifInterface exif = new ExifInterface(file.getPath());
            if (TextUtils.isEmpty(exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL))) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault());
                exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, simpleDateFormat.format(System.currentTimeMillis()));
                exif.saveAttributes();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ContentResolver resolver = context.getContentResolver();
        Uri uri = resolver.insert(external, values);

        try {
            OutputStream out = resolver.openOutputStream(uri);
            FileInputStream fis = new FileInputStream(file);
            FileUtils.copy(fis, out);
            fis.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uri;
    }

    /**
     * 获取图片的宽和高度
     *
     * @param pathName 图片文件地址
     * @return 宽高合成的数组
     */
    public static int[] getImageWidthAndHeight(String pathName) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        // 只请求图片宽高，不解析图片像素(请求图片属性但不申请内存，解析bitmap对象，该对象不占内存)
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, opts);
        return new int[]{opts.outWidth, opts.outHeight};
    }

}