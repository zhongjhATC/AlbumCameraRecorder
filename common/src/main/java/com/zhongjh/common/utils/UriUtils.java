package com.zhongjh.common.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;

/**
 * uri工具类
 *
 * @author zhongjh
 * @date 2022/01/05
 */
public class UriUtils {

    private static final String TAG = UriUtils.class.getSimpleName();

    private final static String PRIMARY = "primary";
    private final static String IMAGE = "image";
    private final static String VIDEO = "video";
    private final static String AUDIO = "audio";
    private final static String RAW = "raw:";
    private final static String MSF = "msf:";

    private final static String URI_AUTHORITY_GOOGLE = "com.google.android.apps.photos.content";
    private final static String URI_AUTHORITY_TENCENT = "com.tencent.mtt.fileprovider";
    private final static String URI_AUTHORITY_HUAWEI = "com.huawei.hidisk.fileprovider";

    private final static String FILES_PATH = "/files_path/";
    private final static String CACHE_PATH = "/cache_path/";
    private final static String EXTERNAL_FILES_PATH = "external_files_path";
    private final static String EXTERNAL_CACHE_PATH = "external_cache_path";

    /**
     * Uri转换file
     *
     * @param uri The uri.
     * @return file
     */
    public static File uriToFile(Context context, final Uri uri) {
        if (uri == null) {
            return null;
        }
        File file = uriToFileReal(context, uri);
        if (file != null) {
            return file;
        }
        return copyUri2Cache(context, uri);
    }

    /**
     * uri转换成file
     *
     * @return file
     */
    @SuppressLint("ObsoleteSdkInt")
    private static File uriToFileReal(Context context, Uri uri) {
        if (context == null || uri == null) {
            Log.d(TAG, " context or uri is null. -> uriToFile");
            return null;
        }
        String scheme = uri.getScheme();
        String path = uri.getPath();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && path != null) {
            // 处理关键字的uri是否能直接使用file
            File file = uriToFilePathKeywords(context, uri, path);
            if (file != null && file.exists()) {
                Log.d(TAG, uri.toString() + " -> " + path);
                return file;
            }
        }
        if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            // 如果是file形式的uri可以直接转换file
            if (path != null) {
                return new File(path);
            }
            Log.d(TAG, uri.toString() + " parse failed. -> new File");
            return null;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && DocumentsContract.isDocumentUri(context, uri)) {
            // 处理Android 21以上的版本并且是document类型的Uri
            return uriToFileAndroidKitkat(context, uri);
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            // Android 19以下直接查询数据库获取文件
            return getFileFromUri(context, uri, "uriToFile -> getFileFromUri");
        } else {
            Log.d(TAG, uri.toString() + " parse failed. -> uriToFile");
            return null;
        }
    }

    /**
     * uri转换成file
     * 有些uri.getPath是直接跟真实路径有关,所以直接比较是否有关键字就直接替换成真实路径
     * 如果转换成file，但是file.exists()是不存在的，那么说明转成真实路径该uri是无效的，该方法行不通换其他方法继续转换file
     *
     * <p>
     * 比如该场景产生的uri:
     * 微信打开文件后，然后该文件点击右上角显示菜单栏，然后点击“选择其他应用打开”选项，
     * 选择我们自己开发的app,然后我们app这边获取微信传递过来的uri是如下值:
     * content://com.tencent.mm.external.fileprovider/external/Android/data/com.tencent.mm/MicroMsg/1341a2809122314ba556156adcc246b8/attachment/xxxx.doc
     * 如果使用.getPath代码获取Path则是如下值
     * /external/Android/data/com.tencent.mm/MicroMsg/1341a2809122314ba556156adcc246b8/attachment/xxxx.doc
     *
     * @param context 上下文
     * @param uri     uri
     * @param path    path
     * @return file
     */
    private static File uriToFilePathKeywords(Context context, Uri uri, String path) {
        String[] externals = new String[]{"/external/", "/external_path/"};
        File file;
        for (String external : externals) {
            if (path.startsWith(external)) {
                file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                        + path.replace(external, "/"));
                if (file.exists()) {
                    Log.d(TAG, uri.toString() + " -> " + external);
                    return file;
                }
            }
        }
        file = null;

        if (path.startsWith(FILES_PATH)) {
            file = new File(context.getFilesDir().getAbsolutePath()
                    + path.replace(FILES_PATH, "/"));
        } else if (path.startsWith(CACHE_PATH)) {
            file = new File(context.getCacheDir().getAbsolutePath()
                    + path.replace(CACHE_PATH, "/"));
        } else if (path.startsWith(EXTERNAL_FILES_PATH)) {
            file = new File(context.getExternalFilesDir(null).getAbsolutePath()
                    + path.replace(EXTERNAL_FILES_PATH, "/"));
        } else if (path.startsWith(EXTERNAL_CACHE_PATH)) {
            file = new File(context.getExternalCacheDir().getAbsolutePath()
                    + path.replace(EXTERNAL_CACHE_PATH, "/"));
        }
        return file;
    }

    /**
     * uri转换成file
     * 处理document类型的Uri 通过document id 处理
     * 兼容4.4以上版本，Android10以下版本
     *
     * @return 文件真实地址
     */
    private static File uriToFileAndroidKitkat(Context context, Uri uri) {
        if (isExternalStorageDocument(uri)) {
            return uriToFileFromExternalStorageDocument(context, uri);
        } else if (isDownloadsDocument(uri)) {
            return uriToFileFromDownloadsDocument(context, uri);
        } else if (isMediaDocument(uri)) {
            return uriToFileFromMediaDocument(context, uri);
        } else if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            return getFileFromUri(context, uri, "uriToFileAndroidKitkat - content");
        } else {
            Log.d(TAG, uri.toString() + " parse failed. -> else null");
            return null;
        }
    }

    /**
     * uri转换成file
     *
     * @param context 上下文
     * @param uri     uri，该uri类型是系统文件未经过第三方App修饰
     * @return 文件真实地址
     */
    private static File uriToFileFromExternalStorageDocument(Context context, Uri uri) {
        String docId = DocumentsContract.getDocumentId(uri);
        String[] split = docId.split(":");
        String type = split[0];
        if (PRIMARY.equalsIgnoreCase(type)) {
            // 获取内置sd卡目录
            return new File(Environment.getExternalStorageDirectory() + "/" + split[1]);
        } else {
            // 通过反射获取外置sd卡目录 http://stackoverflow.com/questions/28605278/android-5-sd-card-label
            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            try {
                Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
                Method getVolumeList = storageManager.getClass().getMethod("getVolumeList");
                Method getUuid = storageVolumeClazz.getMethod("getUuid");
                Method getState = storageVolumeClazz.getMethod("getState");
                Method getPath = storageVolumeClazz.getMethod("getPath");
                Method isPrimary = storageVolumeClazz.getMethod("isPrimary");
                Method isEmulated = storageVolumeClazz.getMethod("isEmulated");
                Object result = getVolumeList.invoke(storageManager);

                if (result != null) {
                    final int length = Array.getLength(result);
                    for (int i = 0; i < length; i++) {
                        Object storageVolumeElement = Array.get(result, i);

                        final boolean mounted = Environment.MEDIA_MOUNTED.equals(getState.invoke(storageVolumeElement))
                                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(getState.invoke(storageVolumeElement));

                        // 判断如果sd卡没有挂载，就不需要获取文件详细信息
                        if (!mounted) {
                            continue;
                        }

                        // 判断sd卡是否主存储卡、是否系统虚拟，否则就不需要获取文件信息
                        if ((boolean) isPrimary.invoke(storageVolumeElement)
                                && (boolean) isEmulated.invoke(storageVolumeElement)) {
                            continue;
                        }

                        // 获取文件相关信息
                        String uuid = (String) getUuid.invoke(storageVolumeElement);

                        if (uuid != null && uuid.equals(type)) {
                            return new File(getPath.invoke(storageVolumeElement) + "/" + split[1]);
                        }
                    }
                }
            } catch (Exception ex) {
                Log.d(TAG, uri.toString() + " parse failed. " + ex.toString() + " -> uriToFileFromExternalStorageDocument - isExternalStorageDocument");
            }
        }
        Log.d(TAG, uri.toString() + " parse failed. -> uriToFileFromExternalStorageDocument - isExternalStorageDocument");
        return null;
    }

    /**
     * uri转换成file
     *
     * @param context 上下文
     * @param uri     uri，该uri类型处于系统下的下载文件夹里面
     * @return 文件真实地址
     */
    private static File uriToFileFromDownloadsDocument(Context context, Uri uri) {
        String id = DocumentsContract.getDocumentId(uri);
        if (TextUtils.isEmpty(id)) {
            Log.d(TAG, uri.toString() + " parse failed(id is null). -> uriToFileFromDownloadsDocument - isDownloadsDocument");
            return null;
        }
        if (id.startsWith(RAW)) {
            return new File(id.substring(4));
        } else if (id.startsWith(MSF)) {
            id = id.split(":")[1];
        }

        long availableId;
        try {
            availableId = Long.parseLong(id);
        } catch (Exception e) {
            return null;
        }

        String[] contentUriPrefixesToTry = new String[]{
                "content://downloads/public_downloads",
                "content://downloads/all_downloads",
                "content://downloads/my_downloads"
        };

        for (String contentUriPrefix : contentUriPrefixesToTry) {
            Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), availableId);
            try {
                File file = getFileFromUri(context, contentUri, "uriToFileFromDownloadsDocument - isDownloadsDocument");
                if (file != null) {
                    return file;
                }
            } catch (Exception ignore) {
            }
        }
        Log.d(TAG, uri.toString() + " parse failed. -> uriToFileFromDownloadsDocument - isDownloadsDocument");
        return null;
    }

    /**
     * uri转换成file
     *
     * @param context 上下文
     * @param uri     uri，该uri类型处于系统下的多媒体文件夹（IMAGE、VIDEO、AUDIO）里面
     * @return 文件真实地址
     */
    private static File uriToFileFromMediaDocument(Context context, Uri uri) {
        String docId = DocumentsContract.getDocumentId(uri);
        String[] split = docId.split(":");
        String type = split[0];
        Uri contentUri;
        if (IMAGE.equals(type)) {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if (VIDEO.equals(type)) {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if (AUDIO.equals(type)) {
            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        } else {
            Log.d(TAG, uri.toString() + " parse failed. -> uriToFileFromMediaDocument - isMediaDocument");
            return null;
        }
        final String selection = "_id=?";
        final String[] selectionArgs = new String[]{split[1]};
        return getFileFromUri(context, contentUri, selection, selectionArgs, "uriToFileFromMediaDocument - isMediaDocument");
    }

    /**
     * uri转换file
     *
     * @param context    上下文
     * @param uri        uri
     * @param methodCode 来自于哪个方法作为标记
     * @return file
     */
    private static File getFileFromUri(Context context, final Uri uri, final String methodCode) {
        return getFileFromUri(context, uri, null, null, methodCode);
    }

    /**
     * uri转换file
     * 这种类型uri可以直接通过数据库查询出来
     *
     * @param context       上下文
     * @param uri           uri
     * @param selection     条件语句
     * @param selectionArgs 条件参数
     * @param methodCode    来自于哪个方法作为标记
     * @return file
     */
    private static File getFileFromUri(Context context, final Uri uri,
                                       final String selection,
                                       final String[] selectionArgs,
                                       final String methodCode) {
        if (URI_AUTHORITY_GOOGLE.equals(uri.getAuthority())) {
            // 特殊处理比如摩托罗拉
            if (!TextUtils.isEmpty(uri.getLastPathSegment())) {
                return new File(uri.getLastPathSegment());
            }
        } else if (URI_AUTHORITY_TENCENT.equals(uri.getAuthority())) {
            // 特殊处理qq浏览器
            String path = uri.getPath();
            if (!TextUtils.isEmpty(path)) {
                File fileDir = Environment.getExternalStorageDirectory();
                return new File(fileDir, path.substring("/QQBrowser".length()));
            }
        } else if (URI_AUTHORITY_HUAWEI.equals(uri.getAuthority())) {
            // 特殊处理华为
            String path = uri.getPath();
            if (!TextUtils.isEmpty(path)) {
                return new File(path.replace("/root", ""));
            }
        }

        // 正式的查找数据库
        final Cursor cursor = context.getContentResolver().query(
                uri, new String[]{"_data"}, selection, selectionArgs, null);
        if (cursor == null) {
            Log.d(TAG, uri.toString() + " parse failed(cursor is null). -> " + methodCode);
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                final int columnIndex = cursor.getColumnIndex("_data");
                if (columnIndex > -1) {
                    return new File(cursor.getString(columnIndex));
                } else {
                    Log.d(TAG, uri.toString() + " parse failed(columnIndex: " + columnIndex + " is wrong). -> " + methodCode);
                    return null;
                }
            } else {
                Log.d(TAG, uri.toString() + " parse failed(moveToFirst return false). -> " + methodCode);
                return null;
            }
        } catch (Exception e) {
            Log.d(TAG, uri.toString() + " parse failed. -> " + methodCode);
            return null;
        } finally {
            cursor.close();
        }
    }

    /**
     * 在最后如果转成file依然为null，则通过流进制形式
     *
     * @param context 上下文
     * @param uri     uri
     * @return file
     */
    private static File copyUri2Cache(Context context, Uri uri) {
        Log.d("UriUtils", "copyUri2Cache() called");
        InputStream is = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            File file = new File(context.getCacheDir(), "" + System.currentTimeMillis());
            FileInputOutputUtils.writeFileFromInputStream(file.getAbsolutePath(), is);
            return file;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
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

    /**
     * 多媒体类型
     *
     * @param uri 需要检查的uri
     * @return Uri是否是多媒体类型
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


}