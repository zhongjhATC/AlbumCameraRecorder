package com.zhongjh.multimedia.album.listener;

import java.util.List;

/**
 * @author zhongjh
 * @date 2022-10-17
 */
public interface OnQueryAllAlbumListener<T> {

    void onComplete(List<T> result);
}
