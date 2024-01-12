package com.zhongjh.grid.listener

import android.view.View
import com.zhongjh.grid.apapter.PhotoAdapter
import com.zhongjh.grid.entity.ProgressMedia
import com.zhongjh.grid.widget.PlayProgressView

/**
 * MaskProgressLayout的有关事件
 *
 * @author zhongjh
 * @date 2018/10/18
 */
interface MaskProgressLayoutListener {
    /**
     * 点击➕号的事件
     *
     * @param view              当前itemView
     * @param progressMedia    当前数据
     * @param alreadyImageCount 目前已经显示的几个图片数量
     * @param alreadyVideoCount 目前已经显示的几个视频数量
     * @param alreadyAudioCount 目前已经显示的几个音频数量
     */
    fun onItemAdd(
        view: View,
        progressMedia: ProgressMedia,
        alreadyImageCount: Int,
        alreadyVideoCount: Int,
        alreadyAudioCount: Int
    )

    /**
     * 点击item的事件
     *
     * @param view           点击的view
     * @param progressMedia 传递的多媒体
     */
    fun onItemClick(view: View, progressMedia: ProgressMedia)

    /**
     * 开始上传 - 指刚添加后的
     *
     * @param progressMedia 传递的多媒体实体
     * @param viewHolder 图片/视频中的格子列表ViewHolder
     *
     */
    fun onItemStartUploading(progressMedia: ProgressMedia, viewHolder: PhotoAdapter.PhotoViewHolder)

    /**
     * 回调删除事件
     *
     * @param progressMedia 传递的多媒体
     */
    fun onItemClose(progressMedia: ProgressMedia)

    /**
     * 开始上传音频
     *
     * @param progressMedia 传递的多媒体实体
     * @param playProgressView 音频控件
     *
     */
    fun onItemAudioStartUploading(progressMedia: ProgressMedia, playProgressView: PlayProgressView)

    /**
     * 开始下载音频
     *
     * @param view 点击的view
     * @param url  网址
     */
    fun onItemAudioStartDownload(view: View, url: String)

    /**
     * 开始下载视频
     *
     * @param view           点击的view
     * @param progressMedia 传递的多媒体
     *
     * @return 是否触发后面的事件
     */
    fun onItemVideoStartDownload(view: View, progressMedia: ProgressMedia): Boolean

    /**
     * 加载数据完毕
     */
    fun onAddDataSuccess(progressMedia: List<ProgressMedia>)
}