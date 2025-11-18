package com.zhongjh.multimedia.album.repository

import android.util.Log
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.multimedia.album.entity.Album
import com.zhongjh.multimedia.album.loader.MediaLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * 数据仓库类，统一管理数据加载
 */
class MediaRepository(private val mediaLoader: MediaLoader) {
    /**
     * 分页加载媒体数据（返回 Flow，支持响应式数据流）
     */
    fun loadMediaPage(bucketId: Long, page: Int, pageSize: Int): Flow<List<LocalMedia>> = flow {
        emit(mediaLoader.loadMediaMore(bucketId, page, pageSize))
    }.catch { e ->
        // 统一错误处理
        emit(emptyList<LocalMedia>().toMutableList())
        Log.e("MediaRepository", "load media failed", e)
    }.flowOn(Dispatchers.IO) // 数据加载在 IO 线程执行

    /**
     * 加载相册列表
     */
    fun loadAlbums(): Flow<List<Album>> = flow {
        emit(mediaLoader.loadMediaAlbum())
    }.catch { e ->
        emit(emptyList<Album>().toMutableList())
        Log.e("MediaRepository", "load albums failed", e)
    }.flowOn(Dispatchers.IO)
}