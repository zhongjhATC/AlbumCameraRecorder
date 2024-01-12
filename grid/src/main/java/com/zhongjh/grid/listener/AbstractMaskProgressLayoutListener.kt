package com.zhongjh.grid.listener

import android.view.View
import com.zhongjh.grid.apapter.PhotoAdapter
import com.zhongjh.grid.entity.ProgressMedia
import com.zhongjh.grid.widget.PlayProgressView

/**
 * 抽象接口
 * @author zhongjh
 * @date 2021/9/7
 */
open class AbstractMaskProgressLayoutListener : MaskProgressLayoutListener {

    override fun onItemAdd(
        view: View,
        progressMedia: ProgressMedia,
        alreadyImageCount: Int,
        alreadyVideoCount: Int,
        alreadyAudioCount: Int
    ) {
    }

    override fun onItemClick(view: View, progressMedia: ProgressMedia) {}

    override fun onItemStartUploading(progressMedia: ProgressMedia, viewHolder: PhotoAdapter.PhotoViewHolder) {}

    override fun onItemAudioStartUploading(progressMedia: ProgressMedia, playProgressView: PlayProgressView) {}

    override fun onItemClose(progressMedia: ProgressMedia) {}

    override fun onItemAudioStartDownload(view: View, url: String) {}

    override fun onItemVideoStartDownload(view: View, progressMedia: ProgressMedia): Boolean {
        return false
    }

    override fun onAddDataSuccess(progressMedia: List<ProgressMedia>) {}

}