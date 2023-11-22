package com.zhongjh.albumcamerarecorder.album.loader

import android.app.Application
import android.provider.MediaStore
import android.util.Log
import com.zhongjh.albumcamerarecorder.album.entity.Album2
import com.zhongjh.albumcamerarecorder.settings.AlbumSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.max

class MedaiLoader(val application: Application) {

    companion object {

        private const val DURATION: String = "duration"
        private const val BUCKET_DISPLAY_NAME = "bucket_display_name"
        private const val BUCKET_ID = "bucket_id"
        private const val ORIENTATION = "orientation"
        private const val MEDIA_TYPE = MediaStore.Files.FileColumns.MEDIA_TYPE

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
            application.contentResolver
                .query(
                    QUERY_URI,
                    PROJECTION,
                    getAlbumSelection(),
                    getSelectionArgs(),
                    getSortOrder()
                )?.use { data ->
                    if (data.count > 0) {
                        var totalCount = 0L
                        val mediaUnique = LocalMedia()
                        val bucketSet = hashSetOf<Long>()
                        val countMap = hashMapOf<Long, Long>()
                        data.moveToFirst()
                        do {
                            val media = parse(mediaUnique, data)
                            if (config.mListenerInfo.onQueryFilterListener?.onFilter(media) == true) {
                                continue
                            }
                            var newCount = countMap[media.bucketId]
                            if (newCount == null) {
                                newCount = 1L
                            } else {
                                newCount++
                            }
                            countMap[media.bucketId] = newCount
                            if (bucketSet.contains(media.bucketId)) {
                                continue
                            }
                            val mediaAlbum = LocalMediaAlbum()
                            mediaAlbum.bucketId = media.bucketId
                            mediaAlbum.bucketDisplayName = media.bucketDisplayName
                            mediaAlbum.bucketDisplayCover = media.path
                            mediaAlbum.bucketDisplayMimeType = media.mimeType
                            albumList += mediaAlbum
                            bucketSet.add(media.bucketId)
                        } while (data.moveToNext())

                        // create custom sandbox dir media album
                        config.sandboxDir?.let { sandboxDir ->
                            val mediaList = loadAppInternalDir(sandboxDir)
                            if (mediaList.isNotEmpty()) {
                                mediaList.first().let { firstMedia ->
                                    val sandboxMediaAlbum = LocalMediaAlbum()
                                    sandboxMediaAlbum.bucketId = firstMedia.bucketId
                                    sandboxMediaAlbum.bucketDisplayName =
                                        firstMedia.bucketDisplayName
                                    sandboxMediaAlbum.bucketDisplayCover = firstMedia.path
                                    sandboxMediaAlbum.bucketDisplayMimeType = firstMedia.mimeType
                                    sandboxMediaAlbum.totalCount = mediaList.size
                                    sandboxMediaAlbum.source.addAll(mediaList.toMutableList())
                                    albumList.add(sandboxMediaAlbum)
                                    countMap[firstMedia.bucketId] = mediaList.size.toLong()
                                }
                            }
                        }

                        // calculate album count
                        albumList.forEach { mediaAlbum ->
                            countMap[mediaAlbum.bucketId]?.let { count ->
                                mediaAlbum.totalCount = count.toInt()
                                totalCount += mediaAlbum.totalCount
                            }
                        }

                        // create all media album
                        val allMediaAlbum = LocalMediaAlbum()
                        val bucketDisplayName =
                            config.defaultAlbumName ?: if (config.mediaType == MediaType.AUDIO)
                                application.getString(R.string.ps_all_audio) else application.getString(
                                R.string.ps_camera_roll
                            )
                        allMediaAlbum.bucketDisplayName = bucketDisplayName
                        allMediaAlbum.bucketId = SelectorConstant.DEFAULT_ALL_BUCKET_ID
                        allMediaAlbum.totalCount = totalCount.toInt()
                        albumList.first().let { firstAlbum ->
                            allMediaAlbum.bucketDisplayCover = firstAlbum.bucketDisplayCover
                            allMediaAlbum.bucketDisplayMimeType = firstAlbum.bucketDisplayMimeType
                        }
                        albumList.add(0, allMediaAlbum)
                    }
                    // close cursor
                    data.close()
                }
        }
        return albumList.apply {
            this.sortByDescending { it.totalCount }
        }
    }

    /**
     * 查询专辑的 sql 条件
     */
    private fun getAlbumSelection(): String {
        val duration = getDurationCondition()
        val fileSize = getFileSizeCondition()
        return if (AlbumSpec.onlyShowImages()) {
            // 只查询图片
            "$MEDIA_TYPE=?${getImageMimeTypeCondition()} AND $fileSize"
        } else if (AlbumSpec.onlyShowVideos()) {
            // 只查询视频
            "$MEDIA_TYPE=?${getVideoMimeTypeCondition()} AND $duration"
        } else {
            // 查询所有
            "($MEDIA_TYPE=?${getImageMimeTypeCondition()} OR $MEDIA_TYPE=?${getVideoMimeTypeCondition()} AND $duration) AND $fileSize"
        }
    }

    /**
     * 获取 duration 的 sql条件语句，只针对视频
     */
    private fun getDurationCondition(): String {
        val maxValue =
            if (AlbumSpec.videoMaxSecond == 0L) {
                Long.MAX_VALUE
            } else {
                AlbumSpec.videoMaxSecond
            }
        return String.format(
            Locale.CHINA,
            "%d <%s $DURATION and $DURATION <= %d",
            max(0L, AlbumSpec.videoMinSecond),
            "=",
            maxValue
        )
    }

    /**
     * 获取 文件大小 的 sql条件语句
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
     * 查询 图片 的 sql语句
     */
    private fun getImageMimeTypeCondition(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append(" AND ").append(MediaStore.MediaColumns.MIME_TYPE).append("='")
            .append(mimeType)
            .append("'")
        if (!config.isGif && !config.onlyQueryImageFormat.contains(MediaUtils.ofGIF())) {
            stringBuilder.append(NOT_GIF)
        }
        if (!config.isWebp && !config.onlyQueryImageFormat.contains(MediaUtils.ofWebp())) {
            stringBuilder.append(NOT_WEBP)
        }
        if (!config.isBmp && !config.onlyQueryImageFormat.contains(MediaUtils.ofBMP())
            && !config.onlyQueryImageFormat.contains(MediaUtils.ofXMSBMP())
            && !config.onlyQueryImageFormat.contains(MediaUtils.ofVNDBMP())
        ) {
            stringBuilder.append(NOT_BMP).append(NOT_XMS_BMP).append(NOT_VND_WAP_BMP)
        }
        if (!config.isHeic && !config.onlyQueryImageFormat.contains(MediaUtils.ofHeic())) {
            stringBuilder.append(NOT_HEIC)
        }
        return stringBuilder.toString()
    }

}