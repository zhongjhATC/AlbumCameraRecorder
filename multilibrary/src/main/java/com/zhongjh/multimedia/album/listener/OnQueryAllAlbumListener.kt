package com.zhongjh.multimedia.album.listener

/**
 * @author zhongjh
 * @date 2022-10-17
 */
interface OnQueryAllAlbumListener<T> {
    fun onComplete(result: List<T>?)
}
