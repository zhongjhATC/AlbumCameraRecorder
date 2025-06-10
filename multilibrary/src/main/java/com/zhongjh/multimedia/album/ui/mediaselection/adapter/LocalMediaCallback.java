package com.zhongjh.multimedia.album.ui.mediaselection.adapter;

import androidx.recyclerview.widget.DiffUtil;

import com.zhongjh.common.entity.LocalMedia;

import java.util.List;

/**
 * 比较差异，更快的实例化数据
 *
 * @author zhongjh
 * @date 2023/8/04
 */
public class LocalMediaCallback extends DiffUtil.Callback {

    private final List<LocalMedia> oldData;
    private final List<LocalMedia> newData;

    /**
     * 新老数据集赋值
     *
     * @param oldData 旧数据
     * @param newData 新数据
     */
    public LocalMediaCallback(List<LocalMedia> oldData, List<LocalMedia> newData) {
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
        LocalMedia oldObject = oldData.get(oldItemPosition);
        LocalMedia newObject = newData.get(newItemPosition);
        return oldObject.getFileId() == newObject.getFileId();
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
        LocalMedia oldObject = oldData.get(oldItemPosition);
        LocalMedia newObject = newData.get(newItemPosition);
        return oldObject.equalsLocalMedia(newObject);
    }
}
