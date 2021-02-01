package com.zhongjh.albumcamerarecorder.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import gaode.zhongjh.com.common.entity.SaveStrategy;
import gaode.zhongjh.com.common.enums.MultimediaTypes;
import gaode.zhongjh.com.common.utils.FileUtil;

import static com.zhongjh.albumcamerarecorder.camera.common.Constants.TYPE_PICTURE;
import static com.zhongjh.albumcamerarecorder.camera.common.Constants.TYPE_VIDEO;

/**
 * Bitmap操作常用工具类
 * Created by Clock on 2015/12/31.
 */
public class BitmapUtils {

    private BitmapUtils() {
    }

    /**
     * 插入图片、视频到图库
     * Android Q需要用到线程来处理 https://blog.csdn.net/guanyingcao/article/details/103634339
     *
     * @param context 上下文
     * @param file    要保存的文件
     * @param type    mp4 jpeg
     */
    public static void displayToGallery(Context context, File file, int type, String directory) {
        if (file == null || !file.exists()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // 插入file数据到相册
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "albumcamerarecorder");
            values.put(MediaStore.Images.Media.DISPLAY_NAME, file.getName());
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Images.Media.ORIENTATION, 0);
            values.put(MediaStore.Images.Media.RELATIVE_PATH, SaveStrategy.RELATIVE_PATH + directory);
            values.put(MediaStore.Images.Media.SIZE, file.length());
            switch (type) {
                case TYPE_VIDEO:
                    values.put(MediaStore.Images.Media.MIME_TYPE, "video/mp4");
                    break;
                case TYPE_PICTURE:
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    break;
            }

            // 需要增加这个，不然AndroidQ识别不到TAG_DATETIME_ORIGINAL创建时间
            try {
                ExifInterface exif = new ExifInterface(file.getPath());
                SimpleDateFormat fmt_Exif = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault());
                exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, fmt_Exif.format(System.currentTimeMillis()));
                exif.saveAttributes();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Uri external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            ContentResolver resolver = context.getContentResolver();
            Uri insertUri = resolver.insert(external, values);

            try {
                OutputStream out = resolver.openOutputStream(insertUri);
                FileInputStream fis = new FileInputStream(file);
                FileUtils.copy(fis,out);
                fis.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            String photoPath = file.getAbsolutePath();
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + photoPath)));
        }
    }
}