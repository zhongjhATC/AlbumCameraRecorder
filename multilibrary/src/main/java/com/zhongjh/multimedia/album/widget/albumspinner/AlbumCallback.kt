package com.zhongjh.multimedia.album.widget.albumspinner

import androidx.recyclerview.widget.DiffUtil
import com.zhongjh.multimedia.album.entity.Album

/**
 * 比较差异，更快的实例化数据
 *
 * 新老数据集赋值
 * @param oldData 旧数据
 * @param newData 新数据
 *
 * @author zhongjh
 * @date 2022/9/28
 */
class AlbumCallback(private val oldData: List<Album>, private val newData: List<Album>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldData.size
    }

    override fun getNewListSize(): Int {
        return newData.size
    }

    /**
     * 判断是不是同一个Item：如果Item有唯一标志的Id的话，建议此处判断id
     * @param oldItemPosition 旧索引数据
     * @param newItemPosition 新的索引数据
     * @return 如果相同返回true,否则返回false
     */
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldAlbum = oldData[oldItemPosition]
        val newAlbum = newData[newItemPosition]
        return oldAlbum.id == newAlbum.id
    }

    /**
     * 判断两个Item的内容是否相同
     * @param oldItemPosition 旧索引数据
     * @param newItemPosition 新的索引数据
     * @return 如果相同返回true,否则返回false
     */
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // 默认内容是相同的，只要有一项不同，则返回false
        val oldAlbum = oldData[oldItemPosition]
        val newAlbum = newData[newItemPosition]
        return oldAlbum.equalsAlbum(newAlbum)
    }
}
