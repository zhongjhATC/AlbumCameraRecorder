package com.zhongjh.combined

import android.app.Activity
import android.content.Intent
import android.view.View
import com.zhongjh.albumcamerarecorder.preview.PreviewFragment2
import com.zhongjh.albumcamerarecorder.settings.GlobalSetting
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec
import com.zhongjh.albumcamerarecorder.settings.MultiMediaSetting.Companion.obtainLocalMediaResult
import com.zhongjh.displaymedia.apapter.AudioAdapter.AudioHolder
import com.zhongjh.displaymedia.apapter.ImagesAndVideoAdapter
import com.zhongjh.displaymedia.entity.DisplayMedia
import com.zhongjh.displaymedia.listener.AbstractDisplayMediaLayoutListener
import com.zhongjh.displaymedia.listener.DisplayMediaLayoutListener
import com.zhongjh.displaymedia.widget.DisplayMediaLayout

/**
 * 协调多个控件之间代码，更加简化代码
 *
 * @param activity           启动的activity
 * @param requestCode        请求打开AlbumCameraRecorder的Code
 * @param globalSetting      AlbumCameraRecorder
 * @param maskProgressLayout Mask控件
 * @param listener           事件
 *
 * @author zhongjh
 * @date 2021/9/6
 */
class Combined(
    var activity: Activity,
    var requestCode: Int,
    globalSetting: GlobalSetting,
    private var maskProgressLayout: DisplayMediaLayout,
    listener: AbstractDisplayMediaLayoutListener
) {

    /**
     * 最大选择数量，如果设置为null，那么能选择的总数量就是 maxImageSelectable+maxVideoSelectable+maxAudioSelectable 的总数.
     * 最大值初始化时都先缓存下来,避免globalSetting.alreadyCount多次改变了总值
     */
    private var maxSelectable: Int? = null

    /**
     * 最大图片选择数量
     * 最大值初始化时都先缓存下来,避免globalSetting.alreadyCount多次改变了总值
     */
    private var maxImageSelectable: Int? = null

    /**
     * 最大视频选择数量
     * 最大值初始化时都先缓存下来,避免globalSetting.alreadyCount多次改变了总值
     */
    private var maxVideoSelectable: Int? = null

    /**
     * 最大音频选择数量
     * 最大值初始化时都先缓存下来,避免globalSetting.alreadyCount多次改变了总值
     */
    private var maxAudioSelectable: Int? = null

    /**
     * AlbumCameraRecorder和Mask控件合并
     */
    init {
        maxSelectable = GlobalSpec.maxSelectable
        maxImageSelectable = GlobalSpec.maxImageSelectable
        maxVideoSelectable = GlobalSpec.maxVideoSelectable
        maxAudioSelectable = GlobalSpec.maxAudioSelectable
        maskProgressLayout.displayMediaLayoutListener = object : DisplayMediaLayoutListener {
            override fun onItemVideoStartDownload(view: View, displayMedia: DisplayMedia, position: Int): Boolean {
                return listener.onItemVideoStartDownload(view, displayMedia, position)
            }

            override fun onItemStartUploading(
                displayMedia: DisplayMedia, viewHolder: ImagesAndVideoAdapter.PhotoViewHolder
            ) {
                listener.onItemStartUploading(displayMedia, viewHolder)
            }

            override fun onItemAudioStartDownload(audioHolder: AudioHolder, displayMedia: DisplayMedia, url: String) {
                listener.onItemAudioStartDownload(audioHolder, displayMedia, url)
            }

            override fun onAddDataSuccess(displayMedias: List<DisplayMedia>) {
            }

            override fun onItemAdd(
                view: View,
                displayMedia: DisplayMedia,
                alreadyImageCount: Int,
                alreadyVideoCount: Int,
                alreadyAudioCount: Int
            ) {
                // 点击Add
                globalSetting.alreadyCount(
                    maxSelectable, maxImageSelectable, maxVideoSelectable, maxAudioSelectable,
                    alreadyImageCount, alreadyVideoCount, alreadyAudioCount
                )
                globalSetting.forResult(requestCode)
                listener.onItemAdd(view, displayMedia, alreadyImageCount, alreadyVideoCount, alreadyAudioCount)
            }

            override fun onItemClick(view: View, displayMedia: DisplayMedia) {
                // 点击详情
                if (displayMedia.isImageOrGif() || displayMedia.isVideo()) {
                    // 预览
//                    globalSetting.openPreviewData(activity, requestCode,
//                            maskProgressLayout.getImagesAndVideos(),
//                            maskProgressLayout.getImagesAndVideos().indexOf(multiMediaView));
                }
                listener.onItemClick(view, displayMedia)
            }

            override fun onItemClose(displayMedia: DisplayMedia) {
                listener.onItemClose(displayMedia)
            }
        }
    }

    /**
     * 封装Activity的onActivityResult
     *
     * @param requestCode 请求码
     * @param data        返回的数据
     */
    fun onActivityResult(requestCode: Int, data: Intent) {
        if (this.requestCode == requestCode) {
            // 如果是在预览界面点击了确定
            if (data.getBooleanExtra(PreviewFragment2.EXTRA_RESULT_APPLY, false)) {
                // 获取选择的数据
                val selected = obtainLocalMediaResult(data) ?: return
                // 循环判断，如果不存在，则删除
                for (i in maskProgressLayout.getImagesAndVideos().indices.reversed()) {
                    var k = 0
                    for (localMedia in selected) {
                        if (maskProgressLayout.getImagesAndVideos()[i] != localMedia) {
                            k++
                        }
                    }
                    if (k == selected.size) {
                        // 所有都不符合，则删除
                        maskProgressLayout.removePosition(i)
                    }
                }
            } else {
                val result = obtainLocalMediaResult(data)
                maskProgressLayout.addLocalFileStartUpload(result)
            }
        }
    }
}
