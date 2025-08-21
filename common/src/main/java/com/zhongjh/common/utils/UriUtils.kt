package com.zhongjh.common.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import com.zhongjh.common.utils.FileInputOutputUtils.writeFileFromInputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

/**
 * uri工具类
 *
 * @author zhongjh
 * @date 2022/01/05
 */
object UriUtils {
    private val TAG: String = UriUtils::class.java.simpleName

    private const val PRIMARY = "primary"
    private const val IMAGE = "image"
    private const val VIDEO = "video"
    private const val AUDIO = "audio"
    private const val RAW = "raw:"
    private const val MSF = "msf:"

    private const val URI_AUTHORITY_GOOGLE = "com.google.android.apps.photos.content"
    private const val URI_AUTHORITY_TENCENT = "com.tencent.mtt.fileprovider"
    private const val URI_AUTHORITY_HUAWEI = "com.huawei.hidisk.fileprovider"

    private const val FILES_PATH = "/files_path/"
    private const val CACHE_PATH = "/cache_path/"
    private const val EXTERNAL_FILES_PATH = "external_files_path"
    private const val EXTERNAL_CACHE_PATH = "external_cache_path"

    /**
     * Uri转换file
     *
     * @param uri The uri.
     * @return file
     */
    fun uriToFile(context: Context, uri: Uri?): File? {
        if (uri == null) {
            return null
        }
        val file = uriToFileReal(context, uri)
        if (file != null) {
            return file
        }
        return copyUri2Cache(context, uri)
    }

    /**
     * uri转换成file
     *
     * @return file
     */
    @SuppressLint("ObsoleteSdkInt")
    private fun uriToFileReal(context: Context?, uri: Uri?): File? {
        if (context == null || uri == null) {
            Log.d(TAG, " context or uri is null. -> uriToFile")
            return null
        }
        val scheme = uri.scheme
        val path = uri.path
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && path != null) {
            // 处理关键字的uri是否能直接使用file
            val file = uriToFilePathKeywords(context, uri, path)
            if (file != null && file.exists()) {
                Log.d(TAG, "$uri -> $path")
                return file
            }
        }
        if (ContentResolver.SCHEME_FILE == scheme) {
            // 如果是file形式的uri可以直接转换file
            if (path != null) {
                return File(path)
            }
            Log.d(TAG, "$uri parse failed. -> new File")
            return null
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
            && DocumentsContract.isDocumentUri(context, uri)
        ) {
            // 处理Android 21以上的版本并且是document类型的Uri
            return uriToFileAndroidKitkat(context, uri)
        } else if (ContentResolver.SCHEME_CONTENT == scheme) {
            // Android 19以下直接查询数据库获取文件
            return getFileFromUri(context, uri, "uriToFile -> getFileFromUri")
        } else {
            Log.d(TAG, "$uri parse failed. -> uriToFile")
            return null
        }
    }

    /**
     * uri转换成file
     * 有些uri.getPath是直接跟真实路径有关,所以直接比较是否有关键字就直接替换成真实路径
     * 如果转换成file，但是file.exists()是不存在的，那么说明转成真实路径该uri是无效的，该方法行不通换其他方法继续转换file
     *
     *
     *
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
    private fun uriToFilePathKeywords(context: Context, uri: Uri, path: String): File? {
        val externals = arrayOf("/external/", "/external_path/")
        var file: File?
        for (external in externals) {
            if (path.startsWith(external)) {
                file = File(
                    Environment.getExternalStorageDirectory().absolutePath
                            + path.replace(external, "/")
                )
                if (file.exists()) {
                    Log.d(TAG, "$uri -> $external")
                    return file
                }
            }
        }
        file = null

        if (path.startsWith(FILES_PATH)) {
            file = File(
                context.filesDir.absolutePath
                        + path.replace(FILES_PATH, "/")
            )
        } else if (path.startsWith(CACHE_PATH)) {
            file = File(
                context.cacheDir.absolutePath
                        + path.replace(CACHE_PATH, "/")
            )
        } else if (path.startsWith(EXTERNAL_FILES_PATH)) {
            file = File(
                context.getExternalFilesDir(null)?.absolutePath
                        + path.replace(EXTERNAL_FILES_PATH, "/")
            )
        } else if (path.startsWith(EXTERNAL_CACHE_PATH)) {
            file = File(
                context.externalCacheDir?.absolutePath
                        + path.replace(EXTERNAL_CACHE_PATH, "/")
            )
        }
        return file
    }

    /**
     * uri转换成file
     * 处理document类型的Uri 通过document id 处理
     * 兼容4.4以上版本，Android10以下版本
     *
     * @return 文件真实地址
     */
    private fun uriToFileAndroidKitkat(context: Context, uri: Uri): File? {
        if (isExternalStorageDocument(uri)) {
            return uriToFileFromExternalStorageDocument(context, uri)
        } else if (isDownloadsDocument(uri)) {
            return uriToFileFromDownloadsDocument(context, uri)
        } else if (isMediaDocument(uri)) {
            return uriToFileFromMediaDocument(context, uri)
        } else if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            return getFileFromUri(context, uri, "uriToFileAndroidKitkat - content")
        } else {
            Log.d(TAG, "$uri parse failed. -> else null")
            return null
        }
    }

    /**
     * uri转换成file
     *
     * @param context 上下文
     * @param uri     uri，该uri类型是系统文件未经过第三方App修饰
     * @return 文件真实地址
     */
    private fun uriToFileFromExternalStorageDocument(context: Context, uri: Uri): File? {
        val docId = DocumentsContract.getDocumentId(uri)
        val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val type = split[0]
        if (PRIMARY.equals(type, ignoreCase = true)) {
            // 获取内置sd卡目录
            return File(Environment.getExternalStorageDirectory().toString() + "/" + split[1])
        } else {
            // 通过反射获取外置sd卡目录 http://stackoverflow.com/questions/28605278/android-5-sd-card-label
            val storageManager = context.applicationContext.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            try {
                val storageVolumeClazz = Class.forName("android.os.storage.StorageVolume")
                val getVolumeList = storageManager.javaClass.getMethod("getVolumeList")
                val getUuid = storageVolumeClazz.getMethod("getUuid")
                val getState = storageVolumeClazz.getMethod("getState")
                val getPath = storageVolumeClazz.getMethod("getPath")
                val isPrimary = storageVolumeClazz.getMethod("isPrimary")
                val isEmulated = storageVolumeClazz.getMethod("isEmulated")
                val result = getVolumeList.invoke(storageManager)

                if (result != null) {
                    val length = java.lang.reflect.Array.getLength(result)
                    for (i in 0 until length) {
                        val storageVolumeElement = java.lang.reflect.Array.get(result, i)

                        val mounted = Environment.MEDIA_MOUNTED == getState.invoke(storageVolumeElement) || Environment.MEDIA_MOUNTED_READ_ONLY == getState.invoke(storageVolumeElement)

                        // 判断如果sd卡没有挂载，就不需要获取文件详细信息
                        if (!mounted) {
                            continue
                        }
                        val isPrimaryB = isPrimary.invoke(storageVolumeElement)
                        val isEmulatedB = isEmulated.invoke(storageVolumeElement)
                        if (isPrimaryB == null || isEmulatedB == null) {
                            continue
                        }
                        // 判断sd卡是否主存储卡、是否系统虚拟，否则就不需要获取文件信息
                        if (isPrimaryB as Boolean && isEmulatedB as Boolean) {
                            continue
                        }

                        // 获取文件相关信息
                        val uuid = getUuid.invoke(storageVolumeElement) as String

                        if (uuid == type) {
                            return File(getPath.invoke(storageVolumeElement)?.toString() + "/" + split[1])
                        }
                    }
                }
            } catch (ex: Exception) {
                Log.d(TAG, "$uri parse failed. $ex -> uriToFileFromExternalStorageDocument - isExternalStorageDocument")
            }
        }
        Log.d(TAG, "$uri parse failed. -> uriToFileFromExternalStorageDocument - isExternalStorageDocument")
        return null
    }

    /**
     * uri转换成file
     *
     * @param context 上下文
     * @param uri     uri，该uri类型处于系统下的下载文件夹里面
     * @return 文件真实地址
     */
    private fun uriToFileFromDownloadsDocument(context: Context, uri: Uri): File? {
        var id = DocumentsContract.getDocumentId(uri)
        if (TextUtils.isEmpty(id)) {
            Log.d(TAG, "$uri parse failed(id is null). -> uriToFileFromDownloadsDocument - isDownloadsDocument")
            return null
        }
        if (id.startsWith(RAW)) {
            return File(id.substring(4))
        } else if (id.startsWith(MSF)) {
            id = id.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
        }

        val availableId: Long
        try {
            availableId = id.toLong()
        } catch (e: Exception) {
            return null
        }

        val contentUriPrefixesToTry = arrayOf(
            "content://downloads/public_downloads",
            "content://downloads/all_downloads",
            "content://downloads/my_downloads"
        )

        for (contentUriPrefix in contentUriPrefixesToTry) {
            val contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), availableId)
            try {
                val file = getFileFromUri(context, contentUri, "uriToFileFromDownloadsDocument - isDownloadsDocument")
                if (file != null) {
                    return file
                }
            } catch (ignore: Exception) {
            }
        }
        Log.d(TAG, "$uri parse failed. -> uriToFileFromDownloadsDocument - isDownloadsDocument")
        return null
    }

    /**
     * uri转换成file
     *
     * @param context 上下文
     * @param uri     uri，该uri类型处于系统下的多媒体文件夹（IMAGE、VIDEO、AUDIO）里面
     * @return 文件真实地址
     */
    private fun uriToFileFromMediaDocument(context: Context, uri: Uri): File? {
        val docId = DocumentsContract.getDocumentId(uri)
        val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val type = split[0]
        val contentUri = if (IMAGE == type) {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        } else if (VIDEO == type) {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        } else if (AUDIO == type) {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        } else {
            Log.d(TAG, "$uri parse failed. -> uriToFileFromMediaDocument - isMediaDocument")
            return null
        }
        val selection = "_id=?"
        val selectionArgs = arrayOf(split[1])
        return getFileFromUri(context, contentUri, selection, selectionArgs, "uriToFileFromMediaDocument - isMediaDocument")
    }

    /**
     * uri转换file
     *
     * @param context    上下文
     * @param uri        uri
     * @param methodCode 来自于哪个方法作为标记
     * @return file
     */
    private fun getFileFromUri(context: Context, uri: Uri, methodCode: String): File? {
        return getFileFromUri(context, uri, null, null, methodCode)
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
    private fun getFileFromUri(
        context: Context, uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?,
        methodCode: String
    ): File? {
        if (URI_AUTHORITY_GOOGLE == uri.authority) {
            // 特殊处理比如摩托罗拉
            if (!TextUtils.isEmpty(uri.lastPathSegment)) {
                return File(uri.lastPathSegment.toString())
            }
        } else if (URI_AUTHORITY_TENCENT == uri.authority) {
            // 特殊处理qq浏览器
            val path = uri.path
            if (!TextUtils.isEmpty(path)) {
                val fileDir = Environment.getExternalStorageDirectory()
                return File(fileDir, path!!.substring("/QQBrowser".length))
            }
        } else if (URI_AUTHORITY_HUAWEI == uri.authority) {
            // 特殊处理华为
            val path = uri.path
            if (!TextUtils.isEmpty(path)) {
                return File(path!!.replace("/root", ""))
            }
        }

        // 正式的查找数据库
        val cursor = context.contentResolver.query(
            uri, arrayOf("_data"), selection, selectionArgs, null
        )
        if (cursor == null) {
            Log.d(TAG, "$uri parse failed(cursor is null). -> $methodCode")
            return null
        }
        try {
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex("_data")
                if (columnIndex > -1) {
                    return File(cursor.getString(columnIndex))
                } else {
                    Log.d(TAG, "$uri parse failed(columnIndex: $columnIndex is wrong). -> $methodCode")
                    return null
                }
            } else {
                Log.d(TAG, "$uri parse failed(moveToFirst return false). -> $methodCode")
                return null
            }
        } catch (e: Exception) {
            Log.d(TAG, "$uri parse failed. -> $methodCode")
            return null
        } finally {
            cursor.close()
        }
    }

    /**
     * 在最后如果转成file依然为null，则通过流进制形式
     *
     * @param context 上下文
     * @param uri     uri
     * @return file
     */
    private fun copyUri2Cache(context: Context, uri: Uri): File? {
        Log.d("UriUtils", "copyUri2Cache() called")
        var `is`: InputStream? = null
        try {
            `is` = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "" + System.currentTimeMillis())
            writeFileFromInputStream(file.absolutePath, `is`)
            return file
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "copyUri2Cache" + e.message)
            return null
        } finally {
            if (`is` != null) {
                try {
                    `is`.close()
                } catch (e: IOException) {
                    Log.e(TAG, "copyUri2Cache" + e.message)
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
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * 下载类型
     * 一般这类型的文件格式是如下：
     * content://com.android.downloads.documents/
     *
     * @param uri 需要检查的uri
     * @return Uri是否是DownloadsProvider类型
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * 多媒体类型
     *
     * @param uri 需要检查的uri
     * @return Uri是否是多媒体类型
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }
}