package com.zhongjh.albumcamerarecorder.utils;

import com.zhongjh.albumcamerarecorder.album.widget.CheckView;

import java.util.List;

import com.zhongjh.common.entity.MultiMedia;

/**
 * @author zhongjh
 */
public class MultiMediaUtils {

    /**
     * 获取相同数据的索引
     *
     * @param items 数据列表
     * @param item  当前数据
     * @return 索引
     */
    public static int checkedNumOf(List<MultiMedia> items, MultiMedia item) {
        int index = -1;
        if (item.getMediaUri() != null) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getMediaUri() != null && items.get(i).getMediaUri().equals(item.getMediaUri())
                        && items.get(i).getId() == item.getId()) {
                    index = i;
                    break;
                }
            }
        } else if (item.getUri() != null) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getUri() != null && items.get(i).getUri().equals(item.getUri())
                        && items.get(i).getId() == item.getId()) {
                    index = i;
                    break;
                }
            }
        } else if (item.getDrawableId() != -1) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getDrawableId() != -1 && items.get(i).getDrawableId() == item.getDrawableId()
                        && items.get(i).getId() == item.getId()) {
                    index = i;
                    break;
                }
            }
        } else if (item.getUrl() != null) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getUrl() != null && items.get(i).getUrl().equals(item.getUrl())
                        && items.get(i).getId() == item.getId()) {
                    index = i;
                    break;
                }
            }
        }
        // 如果选择的为 -1 就是未选状态，否则选择基础数量+1
        return index == -1 ? CheckView.UNCHECKED : index + 1;
    }

    /**
     * 获取相同数据的對象
     *
     * @param items 数据列表
     * @param item  当前数据
     * @return 索引
     */
    public static MultiMedia checkedMultiMediaOf(List<MultiMedia> items, MultiMedia item) {
        MultiMedia multiMedia = null;
        if (item.getMediaUri() != null) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getMediaUri().equals(item.getMediaUri())) {
                    multiMedia = items.get(i);
                    break;
                }
            }
        } else if (item.getUri() != null) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getUri().equals(item.getUri())) {
                    multiMedia = items.get(i);
                    break;
                }
            }
        } else if (item.getDrawableId() != -1) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getDrawableId() == item.getDrawableId()) {
                    multiMedia = items.get(i);
                    break;
                }
            }
        } else if (item.getUrl() != null) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getUrl().equals(item.getUrl())) {
                    multiMedia = items.get(i);
                    break;
                }
            }
        }
        return multiMedia;
    }


}
