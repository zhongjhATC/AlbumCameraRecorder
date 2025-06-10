package com.zhongjh.multimedia.album.widget.albumspinner;

import com.zhongjh.multimedia.album.entity.Album2;
import com.zhongjh.multimedia.album.entity.Album2;

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
    void onItemClick(int position, Album2 album);
}
