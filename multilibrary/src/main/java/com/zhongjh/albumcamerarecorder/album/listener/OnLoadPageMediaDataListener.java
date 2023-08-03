package com.zhongjh.albumcamerarecorder.album.listener;

import com.zhongjh.albumcamerarecorder.album.entity.LocalMedia;

import java.util.List;

/**
 * 返回数据的事件
 *
 * @author zhongjh
 * @date 2023/7/26
 */
public interface OnLoadPageMediaDataListener {

    /**
     * 查询完成后会回调该事件
     *
     * @param data        查询后的数据源
     * @param currentPage 当前第几页
     * @param isHasMore   是否有下一页
     */
    void onLoadPageMediaDataComplete(List<LocalMedia> data, int currentPage, boolean isHasMore);

}
