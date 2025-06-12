package com.zhongjh.gridview.apapter;

import androidx.recyclerview.widget.DiffUtil;

import com.zhongjh.common.entity.GridMedia;

import java.util.List;
import java.util.Objects;

/**
 * 比较差异，更快的实例化数据
 *
 * @author zhongjh
 * @date 2022/9/28
 */
public class GridCallback extends DiffUtil.Callback {

    private final List<GridMedia> oldData;
    private final List<GridMedia> newData;

    /**
     * 新老数据集赋值
     *
     * @param oldData 旧数据
     * @param newData 新数据
     */
    public GridCallback(List<GridMedia> oldData, List<GridMedia> newData) {
        this.oldData = oldData;
        this.newData = newData;
    }

    @Override
    public int getOldListSize() {
        return oldData == null ? 0 : oldData.size();
    }

    @Override
    public int getNewListSize() {
        return newData == null ? 0 : newData.size();
    }

    /**
     * 判断是不是同一个Item：如果Item有唯一标志的Id的话，建议此处判断id
     *
     * @param oldItemPosition 旧索引数据
     * @param newItemPosition 新的索引数据
     * @return 如果相同返回true, 否则返回false
     */
    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        GridMedia oldGridMedia = oldData.get(oldItemPosition);
        GridMedia newGridMedia = newData.get(newItemPosition);
        return Objects.equals(oldGridMedia.getId(), newGridMedia.getId());
    }

    /**
     * 判断两个Item的内容是否相同
     *
     * @param oldItemPosition 旧索引数据
     * @param newItemPosition 新的索引数据
     * @return 如果相同返回true, 否则返回false
     */
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        // 默认内容是相同的，只要有一项不同，则返回false
        GridMedia oldGridMedia = oldData.get(oldItemPosition);
        GridMedia newGridMedia = newData.get(newItemPosition);
        return oldGridMedia.equalsLocalMedia(newGridMedia);
    }
}
