package com.zhongjh.gridview.listener

import android.view.View
import com.zhongjh.gridview.apapter.GridAdapter
import com.zhongjh.common.entity.GridMedia

/**
 * MaskProgressLayout的有关事件
 *
 * @author zhongjh
 * @date 2018/10/18
 */
interface GridViewListener {
    /**
     * 点击➕号的事件
     *
     * @param view              当前itemView
     * @param gridMedia         当前数据
     * @param alreadyImageCount 目前已经显示的几个图片数量
     * @param alreadyVideoCount 目前已经显示的几个视频数量
     * @param alreadyAudioCount 目前已经显示的几个音频数量
     */
    fun onItemAdd(
        view: View,
        gridMedia: GridMedia,
        alreadyImageCount: Int,
        alreadyVideoCount: Int,
        alreadyAudioCount: Int
    )

    /**
     * 点击item的事件
     *
     * @param view           点击的view
     * @param gridMedia 传递的多媒体
     */
    fun onItemClick(view: View, gridMedia: GridMedia)

    /**
     * 回调删除事件
     *
     * @param gridMedia 传递的多媒体
     */
    fun onItemClose(gridMedia: GridMedia)

    /**
     * 开始上传 - 指刚添加后的
     *
     * @param gridMedia 传递的多媒体
     */
    fun onItemStartUploading(gridMedia: GridMedia, viewHolder: GridAdapter.PhotoViewHolder)

    /**
     * 开始下载视频/音频
     *
     * @param view           点击的view
     * @param gridMedia 传递的多媒体
     * @param position 视频/图片的索引
     *
     * @return 是否触发后面的事件
     */
    fun onItemStartDownload(view: View, gridMedia: GridMedia, position: Int): Boolean
}