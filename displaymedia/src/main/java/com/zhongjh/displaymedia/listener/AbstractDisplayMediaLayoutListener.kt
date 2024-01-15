package com.zhongjh.displaymedia.listener

import android.view.View
import com.zhongjh.displaymedia.apapter.ImagesAndVideoAdapter
import com.zhongjh.displaymedia.entity.DisplayMedia
import com.zhongjh.displaymedia.widget.AudioProgressView

/**
 * 抽象接口
 * @author zhongjh
 * @date 2021/9/7
 */
open class AbstractDisplayMediaLayoutListener : DisplayMediaLayoutListener {

    override fun onItemAdd(
        view: View,
        displayMedia: DisplayMedia,
        alreadyImageCount: Int,
        alreadyVideoCount: Int,
        alreadyAudioCount: Int
    ) {
    }

    override fun onItemClick(view: View, displayMedia: DisplayMedia) {}

    override fun onItemStartUploading(displayMedia: DisplayMedia, viewHolder: ImagesAndVideoAdapter.PhotoViewHolder) {}

    override fun onItemAudioStartUploading(displayMedia: DisplayMedia, audioProgressView: AudioProgressView) {}

    override fun onItemClose(displayMedia: DisplayMedia) {}

    override fun onItemAudioStartDownload(view: View, url: String) {}

    override fun onItemVideoStartDownload(view: View, displayMedia: DisplayMedia): Boolean {
        return false
    }

    override fun onAddDataSuccess(displayMedia: List<DisplayMedia>) {}

}