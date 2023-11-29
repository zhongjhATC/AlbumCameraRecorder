package com.zhongjh.albumcamerarecorder.album.loader

import android.app.Application
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import com.zhongjh.albumcamerarecorder.R
import com.zhongjh.albumcamerarecorder.album.entity.Album2
import com.zhongjh.albumcamerarecorder.album.utils.PhotoMetadataUtils
import com.zhongjh.albumcamerarecorder.settings.AlbumSpec
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.enums.MimeType
import com.zhongjh.common.enums.MimeType.Companion.isAudio
import com.zhongjh.common.enums.MimeType.Companion.isImageOrGif
import com.zhongjh.common.enums.MimeType.Companion.isVideo
import com.zhongjh.common.utils.SdkVersionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.max

class MediaLoader(val application: Application) {

    companion object {

        private val TAG = MediaLoader::class.java.simpleName
        private const val DURATION: String = "duration"
        private const val BUCKET_DISPLAY_NAME = "bucket_display_name"
        private const val BUCKET_ID = "bucket_id"
        private const val ORIENTATION = "orientation"
        private const val MEDIA_TYPE = MediaStore.Files.FileColumns.MEDIA_TYPE
        const val NOT_GIF = " AND (${MediaStore.MediaColumns.MIME_TYPE} != 'image/gif')"
        const val NOT_WEBP = " AND (${MediaStore.MediaColumns.MIME_TYPE} != 'image/webp')"
        const val NOT_BMP = " AND (${MediaStore.MediaColumns.MIME_TYPE} != 'image/bmp')"
        const val NOT_XMS_BMP = " AND (${MediaStore.MediaColumns.MIME_TYPE} != 'image/x-ms-bmp')"
        const val NOT_VND_WAP_BMP =
            " AND (${MediaStore.MediaColumns.MIME_TYPE} != 'image/vnd.wap.wbmp')"
        const val NOT_HEIC = " AND (${MediaStore.MediaColumns.MIME_TYPE} != 'image/heic')"

        /**
         * 来自于多媒体的数据源标记
         */
        private val QUERY_URI = MediaStore.Files.getContentUri("external")

        /**
         * 查询列
         */
        val PROJECTION = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            DURATION,
            MediaStore.MediaColumns.SIZE,
            BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DISPLAY_NAME,
            BUCKET_ID,
            MediaStore.MediaColumns.DATE_ADDED,
            ORIENTATION
        )
    }

    /**
     * 获取专辑数据
     */
    suspend fun loadMediaAlbum(): MutableList<Album2> {
        val albumList = mutableListOf<Album2>()
        withContext(Dispatchers.IO) {
            val albumSelectionStr = getAlbumSelection()
            val sortOrderStr = getSortOrder()
            Log.d(TAG, "查询语句: $albumSelectionStr 排序语句: $sortOrderStr")
            application.contentResolver
                .query(
                    QUERY_URI,
                    PROJECTION,
                    albumSelectionStr,
                    getSelectionArgs(),
                    sortOrderStr
                )?.use { data ->
                    if (data.count > 0) {
                        var totalCount = 0L
                        val bucketSet = hashSetOf<Long>()
                        val countMap = hashMapOf<Long, Long>()
                        data.moveToFirst()
                        do {
                            val media = parse(data)
                            var newCount = countMap[media.bucketId]
                            if (newCount == null) {
                                newCount = 1L
                            } else {
                                newCount++
                            }
                            // 根据不同的bucketId划分不同的专辑
                            countMap[media.bucketId] = newCount
                            if (bucketSet.contains(media.bucketId)) {
                                continue
                            }
                            val album = Album2()
                            album.id = media.bucketId
                            album.name = media.parentFolderName
                            album.firstImagePath = media.path
                            album.firstMimeType = media.mimeType
                            albumList += album
                            bucketSet.add(media.bucketId)
                        } while (data.moveToNext())

                        // 计算每个相册的数量
                        albumList.forEach { album ->
                            countMap[album.id]?.let { count ->
                                album.count = count.toInt()
                                totalCount += album.count
                            }
                        }

                        // 创建《所有》该专辑
                        val album = Album2()
                        val bucketDisplayName =
                            application.getString(R.string.z_multi_library_album_name_all);
                        album.name = bucketDisplayName
                        album.id = -1
                        album.count = totalCount.toInt()
                        albumList.first().let { firstAlbum ->
                            album.firstImagePath = firstAlbum.firstImagePath
                            album.firstMimeType = firstAlbum.firstMimeType
                        }
                        albumList.add(0, album)
                    }
                    // 关闭 cursor
                    data.close()
                }
        }
        // 根据count排序
        return albumList.apply {
            this.sortByDescending { it.count }
        }
    }

    /**
     * @return 查询专辑的 sql 条件
     */
    private fun getAlbumSelection(): String {
        val duration = getDurationCondition()
        val fileSize = getFileSizeCondition()
        return if (AlbumSpec.onlyShowImages()) {
            // 只查询图片
            "$MEDIA_TYPE=? ${getImageMimeTypeCondition()} AND $fileSize"
        } else if (AlbumSpec.onlyShowVideos()) {
            // 只查询视频
            "$MEDIA_TYPE=? ${getVideoMimeTypeCondition()} AND $duration"
        } else {
            // 查询所有
            "(($MEDIA_TYPE=? ${getImageMimeTypeCondition()}) OR ($MEDIA_TYPE=?${getVideoMimeTypeCondition()} AND $duration)) AND $fileSize"
        }
    }

    /**
     * @return 条件参数
     */
    private fun getSelectionArgs(): Array<String> {
        return if (AlbumSpec.onlyShowImages()) {
            // 只查询图片
            arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString())
        } else if (AlbumSpec.onlyShowVideos()) {
            // 只查询视频
            arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())
        } else {
            // 查询所有
            arrayOf(
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
            )
        }
    }

    /**
     * @return 排序
     */
    private fun getSortOrder(): String {
        return MediaStore.MediaColumns.DATE_MODIFIED + " DESC"
    }

    /**
     * @return Cursor转换成实体类
     */
    private fun parse(data: Cursor): LocalMedia {
        val media = LocalMedia()
        media.id = data.getLong(data.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
        media.bucketId = data.getLong(data.getColumnIndexOrThrow(BUCKET_ID))
        media.fileName =
            data.getString(data.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
        media.parentFolderName = data.getString(data.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME))
        media.absolutePath =
            data.getString(data.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
        media.mimeType =
            data.getString(data.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE))
        // 判断文件类型是否符合规范，不规范就只能取后缀名
        if (!MimeType.isImageOrGif(media.mimeType) && !MimeType.isVideo(media.mimeType)) {
            // 因为某些app保存文件时导致数据库的mimeType不符合规范，所以通过后缀名设置类型
            val extension: String = media.mimeType.substring(media.mimeType.lastIndexOf(".") + 1)
            // 循环图片类型，判断后缀是否是.jpg之类的
            for (mimeTypeImage in MimeType.ofImage()) {
                if (mimeTypeImage.extensions.contains(extension)) {
                    media.mimeType = mimeTypeImage.mimeTypeName
                }
            }
            // 循环视频类型，判断后缀是否是.mp4之类的
            for (mimeTypeVideo in MimeType.ofVideo()) {
                if (mimeTypeVideo.extensions.contains(extension)) {
                    media.mimeType = mimeTypeVideo.mimeTypeName
                }
            }
        }
        // 图片没有具体到某个类型
        if (MimeType.hasMimeTypeOfUnknown(media.mimeType)) {
            val mimeType = MimeType.getMimeType(media.absolutePath)
            media.mimeType =
                if (TextUtils.isEmpty(mimeType)) {
                    media.mimeType
                } else {
                    mimeType.toString()
                }
        }
        media.path =
            if (SdkVersionUtils.isQ) {
                getRealPathUri(media.id, media.mimeType)
            } else {
                media.absolutePath
            }
        media.orientation = data.getInt(data.getColumnIndexOrThrow(ORIENTATION))
        media.duration = data.getLong(data.getColumnIndexOrThrow(DURATION))
        media.size = data.getLong(data.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE))
        media.dateAddedTime =
            data.getLong(data.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED))
        if (media.orientation == 90 || media.orientation == 270) {
            media.width = data.getInt(data.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT))
            media.height = data.getInt(data.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH))
        } else {
            media.width = data.getInt(data.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH))
            media.height = data.getInt(data.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT))
        }
        return media
    }

    /**
     * @return 获取 duration 的 sql条件语句，只针对视频
     */
    private fun getDurationCondition(): String {
        val maxValue =
            if (AlbumSpec.videoMaxSecond == 0) {
                Long.MAX_VALUE
            } else {
                AlbumSpec.videoMaxSecond
            }
        return String.format(
            Locale.CHINA,
            "%d <%s $DURATION and $DURATION <= %d",
            max(0, AlbumSpec.videoMinSecond),
            "=",
            maxValue
        )
    }

    /**
     * @return 获取 文件大小 的 sql条件语句
     */
    private fun getFileSizeCondition(): String {
        val maxS =
            if (AlbumSpec.filterMaxFileSize == 0L) {
                Long.MAX_VALUE
            } else {
                AlbumSpec.filterMaxFileSize
            }
        return String.format(
            Locale.CHINA,
            "%d <%s " + MediaStore.MediaColumns.SIZE + " and " + MediaStore.MediaColumns.SIZE + " <= %d",
            max(0, AlbumSpec.filterMinFileSize), "=", maxS
        )
    }

    /**
     * @return 查询 图片 的 sql语句
     */
    private fun getImageMimeTypeCondition(): String {
        val stringBuilder = StringBuilder()
        // 配置具体到什么类型
        AlbumSpec.mimeTypeSet?.let { mimeTypes ->
            val mimeTypeList = ArrayList<MimeType>()
            mimeTypes.forEach { mimeType ->
                if (MimeType.ofImage().contains(mimeType)) {
                    mimeTypeList.add(mimeType)
                }
            }
            mimeTypeList.forEachIndexed { i, mimeType ->
                if (MimeType.ofImage().contains(mimeType)) {
                    stringBuilder.append(if (i == 0) " AND (" else " OR ")
                        .append(MediaStore.MediaColumns.MIME_TYPE).append("='").append(mimeType)
                        .append("'")
                        .append(if (i == mimeTypeList.size.minus(1)) ")" else "")
                }
            }
        }
        // 根据配置排除类型
        if (!AlbumSpec.isSupportGif && AlbumSpec.mimeTypeSet?.contains(MimeType.GIF) != true) {
            stringBuilder.append(NOT_GIF)
        }
        if (!AlbumSpec.isSupportWebp && AlbumSpec.mimeTypeSet?.contains(MimeType.WEBP) != true) {
            stringBuilder.append(NOT_WEBP)
        }
        if (!AlbumSpec.isSupportBmp && AlbumSpec.mimeTypeSet?.contains(MimeType.BMP) != true
            && AlbumSpec.mimeTypeSet?.contains(MimeType.XMSBMP) != true
            && AlbumSpec.mimeTypeSet?.contains(MimeType.VNDBMP) != true
        ) {
            stringBuilder.append(NOT_BMP).append(NOT_XMS_BMP).append(NOT_VND_WAP_BMP)
        }
        if (!AlbumSpec.isSupportHeic && AlbumSpec.mimeTypeSet?.contains(MimeType.HEIC) != true) {
            stringBuilder.append(NOT_HEIC)
        }
        return stringBuilder.toString()
    }

    /**
     * @return 查询 视频 的 sql语句
     */
    private fun getVideoMimeTypeCondition(): String {
        val stringBuilder = StringBuilder()
        // 配置具体到什么类型
        AlbumSpec.mimeTypeSet?.let { mimeTypes ->
            val mimeTypeList = ArrayList<MimeType>()
            mimeTypes.forEach { mimeType ->
                if (MimeType.ofVideo().contains(mimeType)) {
                    mimeTypeList.add(mimeType)
                }
            }
            mimeTypeList.forEachIndexed { i, mimeType ->
                stringBuilder.append(if (i == 0) " AND (" else " OR ")
                    .append(MediaStore.MediaColumns.MIME_TYPE).append("='").append(mimeType)
                    .append("'")
                    .append(if (i == mimeTypeList.size.minus(1)) ")" else "")
            }
        }
        return stringBuilder.toString()
    }

    /**
     * 根据 bucketId 和 mimeType 获取uri
     *
     * @param bucketId bucketId
     * @param mimeType mimeType
     * @return uri
     */
    private fun getRealPathUri(bucketId: Long, mimeType: String): String {
        val contentUri: Uri = if (isImageOrGif(mimeType)) {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        } else if (isVideo(mimeType)) {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        } else if (isAudio(mimeType)) {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Files.getContentUri("external")
        }
        return ContentUris.withAppendedId(contentUri, bucketId).toString()
    }

    /**
     * 根据游标获取uri
     *
     * @param cursor 游标
     * @return uri
     */
    private fun getFirstUri(cursor: Cursor): String {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
        val mimeType =
            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE))
        return getRealPathUri(id, mimeType)
    }

    /**
     * 根据游标获取mimeType
     *
     * @param cursor 游标
     * @return mimeType
     */
    private fun getFirstCoverMimeType(cursor: Cursor): String? {
        return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE))
    }

    /**
     * 根据游标获取path
     *
     * @param cursor 游标
     * @return url path
     */
    private fun getFirstUrl(cursor: Cursor): String? {
        return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA))
    }

}