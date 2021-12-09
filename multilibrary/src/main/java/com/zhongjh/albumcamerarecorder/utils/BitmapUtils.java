package com.zhongjh.albumcamerarecorder.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.zhongjh.common.utils.MediaStoreCompat;

import static com.zhongjh.albumcamerarecorder.camera.common.Constants.TYPE_PICTURE;
import static com.zhongjh.albumcamerarecorder.camera.common.Constants.TYPE_VIDEO;

/**
 * Bitmap操作常用工具类
 *
 * @author Clock
 * @date 2015/12/31
 */
public class BitmapUtils {

    private BitmapUtils() {
    }

    /**
     * 插入图片、视频到图库
     *
     * @param context  上下文
     * @param file     要保存的文件
     * @param type     mp4 jpeg
     * @param duration video专属的时长,图片传-1即可
     */
    public static Uri displayToGallery(Context context, File file, int type, int duration, String directory, MediaStoreCompat mediaStoreCompat) {
        if (file == null || !file.exists()) {
            return null;
        }
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // 插入file数据到相册
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, AppUtils.getAppName(context));
            values.put(MediaStore.Images.Media.DISPLAY_NAME, file.getName());
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Images.Media.ORIENTATION, 0);
            values.put(MediaStore.Images.Media.SIZE, file.length());
            Uri external = null;
            switch (type) {
                case TYPE_VIDEO:
                    values.put(MediaStore.Images.Media.MIME_TYPE, "video/mp4");
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + File.separator + directory);
                    external = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    break;
                case TYPE_PICTURE:
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + directory);
                    external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    break;
                default:
                    break;
            }

            // 需要增加这个，不然AndroidQ识别不到TAG_DATETIME_ORIGINAL创建时间
            try {
                ExifInterface exif = new ExifInterface(file.getPath());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault());
                exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, simpleDateFormat.format(System.currentTimeMillis()));
                exif.saveAttributes();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ContentResolver resolver = context.getContentResolver();
            uri = resolver.insert(external, values);

            try {
                OutputStream out = resolver.openOutputStream(uri);
                FileInputStream fis = new FileInputStream(file);
                FileUtils.copy(fis, out);
                fis.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            String photoPath = file.getAbsolutePath();
            uri = mediaStoreCompat.getUri(photoPath);
            // 添加到图库数据库
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, photoPath);
            values.put(MediaStore.Images.Media.TITLE, AppUtils.getAppName(context));
            values.put(MediaStore.Images.Media.DISPLAY_NAME, file.getName());
            values.put(MediaStore.Images.Media.SIZE, file.length());
            switch (type) {
                case TYPE_VIDEO:
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
                case TYPE_PICTURE:
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    break;
                default:
                    break;
            }
            // 这个判断AndroidQ的就是用来解决ACTION_MEDIA_SCANNER_SCAN_FILE过时的方式
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
        }
        return uri;
    }
}