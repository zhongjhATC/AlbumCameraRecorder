package com.zhongjh.displaymedia.listener

import android.view.View
import com.zhongjh.displaymedia.apapter.AudioAdapter
import com.zhongjh.displaymedia.apapter.ImagesAndVideoAdapter
import com.zhongjh.displaymedia.entity.DisplayMedia

/**
 * MaskProgressLayout的有关事件
 *
 * @author zhongjh
 * @date 2018/10/18
 */
interface DisplayMediaLayoutListener {
    /**
     * 点击➕号的事件
     *
     * @param view              当前itemView
     * @param displayMedia    当前数据
     * @param alreadyImageCount 目前已经显示的几个图片数量
     * @param alreadyVideoCount 目前已经显示的几个视频数量
     * @param alreadyAudioCount 目前已经显示的几个音频数量
     */
    fun onItemAdd(
        view: View,
        displayMedia: DisplayMedia,
        alreadyImageCount: Int,
        alreadyVideoCount: Int,
        alreadyAudioCount: Int
    )

    /**
     * 点击item的事件
     *
     * @param view           点击的view
     * @param displayMedia 传递的多媒体
     */
    fun onItemClick(view: View, displayMedia: DisplayMedia)

    /**
     * 开始上传 - 指刚添加后的
     *
     * @param displayMedia 传递的多媒体
     */
    fun onItemStartUploading(
        displayMedia: DisplayMedia,
        viewHolder: ImagesAndVideoAdapter.PhotoViewHolder
    )

    /**
     * 回调删除事件
     *
     * @param displayMedia 传递的多媒体
     */
    fun onItemClose(displayMedia: DisplayMedia)

    /**
     * 开始下载音频
     *
     * @param audioHolder 点击的view
     * @param url  网址
     */
    fun onItemAudioStartDownload(audioHolder: AudioAdapter.AudioHolder, url: String)

    /**
     * 开始下载视频
     *
     * @param view           点击的view
     * @param displayMedia 传递的多媒体
     * @param position 视频/图片的索引
     *
     * @return 是否触发后面的事件
     */
    fun onItemVideoStartDownload(view: View, displayMedia: DisplayMedia, position: Int): Boolean

    /**
     * 加载数据完毕
     */
    fun onAddDataSuccess(displayMedias: List<DisplayMedia>)
}