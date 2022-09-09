package com.zhongjh.albumcamerarecorder.album.listener;

import java.util.List;

/**
 * 返回数据的事件
 *
 * @author zhongjh
 * @date 2022/9/9
 */
public interface OnQueryDataListener<T> {

    /**
     * 查询完成后会回调该事件
     *
     * @param data 查询后的数据源
     */
    void onComplete(List<T> data);

}
