package gaode.zhongjh.com.common.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import androidx.core.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;

import gaode.zhongjh.com.common.enums.MultimediaTypes;

/**
 *
 * @author zhongjh
 * @date 2019/4/1
 * File的有关工具，取自于 https://github.com/baishixian/Share2/blob/master/share2/src/main/java/gdut/bsx/share2/FileUtil.java
 * 主要用来解决部分手机需要系统的原声uri才可以启动默认的播放器
 */
public class FileUtil {

    private static final String TAG = "FileUtil";

    /**
     * Get file uri
     * @param context context
     * @param shareContentType shareContentType {@link MultimediaTypes}
     * @param file file
     * @return Uri
     */
    public static Uri getFileUri (Context context, @MultimediaTypes int shareContentType, File file){

        if (context == null) {
            Log.e(TAG,"getFileUri current activity is null.");
            return null;
        }

        if (file == null || !file.exists()) {
            Log.e(TAG,"getFileUri file is null or not exists.");
            return null;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG,"getFileUri miss WRITE_EXTERNAL_STORAGE permission.");
            return null;
        }

        Uri uri = null;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            uri = Uri.fromFile(file);
        } else {

            switch (shareContentType) {
                case MultimediaTypes.PICTURE:
                    uri = getImageContentUri(context, file);
                    break;
                case MultimediaTypes.VIDEO:
                    uri = getVideoContentUri(context, file);
                    break;
                case MultimediaTypes.AUDIO:
                    uri = getAudioContentUri(context, file);
                    break;
                case MultimediaTypes.BLEND:
                    uri = getFileContentUri(context, file);
                    break;
                case MultimediaTypes.ADD:
                    break;
            }
        }

        if (uri == null) {
            uri = forceGetFileUri(file);
        }

        return uri;
    }

    /**
     * forceGetFileUri
     * @param shareFile shareFile
     * @return Uri
     */
    private static Uri forceGetFileUri(File shareFile) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                @SuppressLint("PrivateApi")
                Method rMethod = StrictMode.class.getDeclaredMethod("disableDeathOnFileUriExposure");
                rMethod.invoke(null);
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }

        return Uri.parse("file://" + shareFile.getAbsolutePath());
    }

    /**
     * getFileContentUri
     * @param context context
     * @param file file
     * @return Uri
     */
    private static Uri getFileContentUri(Context context, File file) {
        String volumeName = "external";
        String filePath = file.getAbsolutePath();
        String[] projection = new String[]{MediaStore.Files.FileColumns._ID};
        Uri uri = null;

        Cursor cursor = context.getContentResolver().query(MediaStore.Files.getContentUri(volumeName), projection,
                MediaStore.Images.Media.DATA + "=? ", new String[] { filePath }, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID));
                uri = MediaStore.Files.getContentUri(volumeName, id);
            }
            cursor.close();
        }

        return uri;
    }

    /**
     * Gets the content:// URI from the given corresponding path to a file
     *
     * @param context context
     * @param imageFile imageFile
     * @return content Uri
     */
    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID }, MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);
        Uri uri = null;

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                Uri baseUri = Uri.parse("content://media/external/images/media");
                uri = Uri.withAppendedPath(baseUri, "" + id);
            }

            cursor.close();
        }

        if (uri == null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, filePath);
            uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }

        return uri;
    }

    /**
     * Gets the content:// URI from the given corresponding path to a file
     *
     * @param context context
     * @param videoFile videoFile
     * @return content Uri
     */
    private static Uri getVideoContentUri(Context context, File videoFile) {
        Uri uri = null;
        String filePath = videoFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Video.Media._ID }, MediaStore.Video.Media.DATA + "=? ",
                new String[] { filePath }, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                Uri baseUri = Uri.parse("content://media/external/video/media");
                uri = Uri.withAppendedPath(baseUri, "" + id);
            }

            cursor.close();
        }

        if (uri == null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.DATA, filePath);
            uri = context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        }

        return uri;
    }


    /**
     * Gets the content:// URI from the given corresponding path to a file
     *
     * @param context context
     * @param audioFile audioFile
     * @return content Uri
     */
    private static Uri getAudioContentUri(Context context, File audioFile) {
        Uri uri = null;
        String filePath = audioFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Media._ID }, MediaStore.Audio.Media.DATA + "=? ",
                new String[] { filePath }, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                Uri baseUri = Uri.parse("content://media/external/audio/media");
                uri = Uri.withAppendedPath(baseUri, "" + id);
            }

            cursor.close();
        }
        if (uri == null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.Media.DATA, filePath);
            uri = context.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
        }

        return uri;
    }

}
