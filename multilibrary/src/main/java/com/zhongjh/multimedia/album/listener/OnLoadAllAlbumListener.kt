package com.zhongjh.multimedia.album.listener

import com.zhongjh.multimedia.album.entity.Album

/**
 * 返回数据的事件
 *
 * @author zhongjh
 * @date 2022/9/9
 */
interface OnLoadAllAlbumListener {
    /**
     * 查询完成后会回调该事件
     *
     * @param data 查询后的数据源
     */
    fun onLoadAllAlbumComplete(data: List<Album?>?)

    /**
     * 异常信息
     * @param t 异常
     */
    fun onFail(t: Throwable?)
}
