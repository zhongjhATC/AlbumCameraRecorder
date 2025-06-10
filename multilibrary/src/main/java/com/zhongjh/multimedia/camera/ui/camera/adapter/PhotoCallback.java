package com.zhongjh.multimedia.camera.ui.camera.adapter;

import androidx.recyclerview.widget.DiffUtil;

import com.zhongjh.multimedia.camera.entity.BitmapData;
import com.zhongjh.multimedia.camera.entity.BitmapData;

import java.util.List;
import java.util.Objects;

/**
 * 比较差异，更快的实例化数据
 *
 * @author zhongjh
 * @date 2022/9/28
 */
public class PhotoCallback extends DiffUtil.Callback {

    private final List<BitmapData> oldData;
    private final List<BitmapData> newData;

    /**
     * 新老数据集赋值
     *
     * @param oldData 旧数据
     * @param newData 新数据
     */
    public PhotoCallback(List<BitmapData> oldData, List<BitmapData> newData) {
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
        BitmapData oldBitmapData = oldData.get(oldItemPosition);
        BitmapData newBitmapData = newData.get(newItemPosition);
        return Objects.equals(oldBitmapData.getTemporaryId(), newBitmapData.getTemporaryId());
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
        BitmapData oldBitmapData = oldData.get(oldItemPosition);
        BitmapData newBitmapData = newData.get(newItemPosition);
        return oldBitmapData.equalsBitmapData(newBitmapData);
    }
}
