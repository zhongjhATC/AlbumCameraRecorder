package com.zhongjh.albumcamerarecorder.album.widget.albumspinner;

import com.zhongjh.albumcamerarecorder.album.entity.Album;

/**
 * 专辑目录事件
 *
 * @author zhongjh
 */
public interface OnAlbumItemClickListener {
    /**
     * 专辑目录单击事件
     *
     * @param position 索引
     * @param album 专辑
     */
    void onItemClick(int position, Album album);
}
