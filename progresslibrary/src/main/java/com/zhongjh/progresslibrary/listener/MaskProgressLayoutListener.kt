package com.zhongjh.progresslibrary.listener

import android.view.View
import com.zhongjh.progresslibrary.entity.MultiMediaView

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
     * @param multiMediaView    当前数据
     * @param alreadyImageCount 目前已经显示的几个图片数量
     * @param alreadyVideoCount 目前已经显示的几个视频数量
     * @param alreadyAudioCount 目前已经显示的几个音频数量
     */
    fun onItemAdd(view: View, multiMediaView: MultiMediaView, alreadyImageCount: Int, alreadyVideoCount: Int, alreadyAudioCount: Int)

    /**
     * 点击item的事件
     *
     * @param view           点击的view
     * @param multiMediaView 传递的多媒体
     */
    fun onItemClick(view: View, multiMediaView: MultiMediaView)

    /**
     * 开始上传 - 指刚添加后的
     *
     * @param multiMediaView 传递的多媒体
     */
    fun onItemStartUploading(multiMediaView: MultiMediaView)

    /**
     * 回调删除事件
     *
     * @param view           点击的view
     * @param multiMediaView 传递的多媒体
     */
    fun onItemClose(view: View, multiMediaView: MultiMediaView)

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
     * @param multiMediaView 传递的多媒体
     *
     * @return 是否触发后面的事件
     */
    fun onItemVideoStartDownload(view: View, multiMediaView: MultiMediaView): Boolean

    /**
     * 加载数据完毕
     */
    fun onAddDataSuccess(multiMediaViews: List<MultiMediaView>)
}