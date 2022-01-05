package com.zhongjh.albumcamerarecorder.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.RequiresApi;
import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.zhongjh.albumcamerarecorder.album.utils.PhotoMetadataUtils;
import com.zhongjh.common.utils.MediaStoreCompat;

import static com.zhongjh.albumcamerarecorder.camera.constants.CameraTypes.TYPE_PICTURE;
import static com.zhongjh.albumcamerarecorder.camera.constants.CameraTypes.TYPE_VIDEO;

/**
 * 相册操作常用工具类
 *
 * @author Clock
 * @author zhongjh
 * @date 2015/12/31
 * @date 2022/01/05
 */
public class AlbumUtils {

    private static final String TAG = AlbumUtils.class.getSimpleName();

    private final static String PRIMARY = "primary";
    private final static String IMAGE = "image";
    private final static String VIDEO = "video";
    private final static String AUDIO = "audio";
    private final static String CONTENT = "content";
    private final static String FILE = "file";

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
    public static Uri displayToGallery(Context context, File file, int type, long duration, int width, int height,
                                       String directory, MediaStoreCompat mediaStoreCompat) {
        if (file == null || !file.exists()) {
            return null;
        }
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uri = displayToGalleryAndroidQ(context, file, type, width, height, directory);
        } else {
            String photoPath = file.getAbsolutePath();
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


    /**
     * 插入图片、视频到图库
     * 兼容AndroidQ
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private static Uri displayToGalleryAndroidQ(Context context, File file, int type, int width, int height,
                                                String directory) {
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
     * 把uri保存到沙盒上的file
     * 一般供用压缩使用
     *
     * @return 复制到沙盒上的file
     */
    @SuppressLint("ObsoleteSdkInt")
    public static File uriToFile(Context context, Uri uri) {
        if (context == null || uri == null) {
            return null;
        }

        // 该库21以上，是绝对不会执行这里的，只是做个记录使用
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return uriToFileAndroidJelly(context, uri);
        }

        // 处理Android 21以上的版本并且是document类型的Uri
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && DocumentsContract.isDocumentUri(context, uri)) {
            uriToFileAndroidKitkat(context, uri);
        }


    }

    /**
     * uri转换成file
     * 兼容4.4以下版本
     *
     * @return 文件真实地址
     */
    private static String uriToFileAndroidJelly(final Context context, final Uri uri) {
        if (null == uri) {
            return null;
        }
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            String[] projection = {MediaStore.Images.ImageColumns.DATA};
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    /**
     * uri转换成file
     * 处理document类型的Uri 通过document id 处理
     * 兼容4.4以上版本，Android10以下版本
     *
     * @return 文件真实地址
     */
    private static String uriToFileAndroidKitkat(Context context, Uri uri) {
        if (isExternalStorageDocument(uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            String[] split = docId.split(":");
            String type = split[0];
            if (PRIMARY.equalsIgnoreCase(type)) {
                // 直接返回内部存储
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else {
                // 下面的逻辑是外部存储如何为文档构建URI http://stackoverflow.com/questions/28605278/android-5-sd-card-label
                StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
                try {
                    Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
                } catch (Exception ex) {
                    Log.d(TAG, uri.toString() + " parse failed. " + ex.toString() + " -> uriToFileAndroidKitkat - isExternalStorageDocument");
                }
            }
            Log.d("UriUtils", uri.toString() + " parse failed. -> uriToFileAndroidKitkat - isExternalStorageDocument");
            return null;
        } else if (isDownloadsDocument(uri)) {
            String id = DocumentsContract.getDocumentId(uri);
            Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));
            return getDataColumn(context, contentUri, null, null);
        } else if (isMediaDocument(uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            String[] split = docId.split(":");
            String type = split[0];
            Uri contentUri = null;
            if (IMAGE.equals(type)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if (VIDEO.equals(type)) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if (AUDIO.equals(type)) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }
            String selection = MediaStore.Images.Media._ID + "=?";
            String[] selectionArgs = new String[]{split[1]};
            return getDataColumn(context, contentUri, selection, selectionArgs);
        }
    }

    /**
     * 把uri保存到沙盒上的file
     * 一般供用压缩使用
     * 兼容AndroidQ
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private static File uriToSandboxFileAndroidQ(Uri uri, Context context) {
        File file = null;
        if (uri == null) {
            return file;
        }
        // android10以上转换
        if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            file = new File(uri.getPath());
        } else if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //把文件复制到沙盒目录
            ContentResolver contentResolver = context.getContentResolver();
            String displayName = System.currentTimeMillis() + Math.round((Math.random() + 1) * 1000)
                    + "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri));
            try {
                InputStream is = contentResolver.openInputStream(uri);
                File cache = new File(context.getCacheDir().getAbsolutePath(), displayName);
                FileOutputStream fos = new FileOutputStream(cache);
                FileUtils.copy(is, fos);
                file = cache;
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 内部存储空间
     * 一般这类型的文件格式是如下：
     * content://com.android.externalstorage.documents/document/xxx/xxx/test.jpg
     *
     * @param uri 需要检查的uri
     * @return Uri是否是externalstorage类型
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * 下载类型
     * 一般这类型的文件格式是如下：
     * content://com.android.downloads.documents/
     *
     * @param uri 需要检查的uri
     * @return Uri是否是DownloadsProvider类型
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

}