package com.zhongjh.multimedia.album.widget.albumspinner

import com.zhongjh.multimedia.album.entity.Album

/**
 * 专辑目录事件
 *
 * @author zhongjh
 */
interface OnAlbumItemClickListener {
    /**
     * 专辑目录单击事件
     *
     * @param position 索引
     * @param album 专辑
     */
    fun onItemClick(position: Int, album: Album)
}
