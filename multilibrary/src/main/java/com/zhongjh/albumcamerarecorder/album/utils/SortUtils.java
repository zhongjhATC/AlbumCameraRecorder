package com.zhongjh.albumcamerarecorder.album.utils;

import com.zhongjh.albumcamerarecorder.album.entity.Album2;

import java.util.Collections;
import java.util.List;


/**
 * 排序工具类
 * @author zhongjh
 */
public class SortUtils {

    /**
     * 根据数量Count进行排序
     *
     * @param albums 专辑列表源
     */
    public static void sortFolder(List<Album2> albums) {
        Collections.sort(albums, (lhs, rhs) -> {
            if (lhs.getData().isEmpty() || rhs.getData().isEmpty()) {
                return 0;
            }
            int lSize = lhs.getCount();
            int rSize = rhs.getCount();
            return Integer.compare(rSize, lSize);
        });
    }

}
