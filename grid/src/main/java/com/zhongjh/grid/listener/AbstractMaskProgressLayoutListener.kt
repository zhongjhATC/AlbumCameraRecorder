package com.zhongjh.grid.listener

import android.view.View
import com.zhongjh.grid.apapter.PhotoAdapter
import com.zhongjh.grid.entity.GridMedia
import com.zhongjh.grid.widget.PlayProgressView

/**
 * 抽象接口
 * @author zhongjh
 * @date 2021/9/7
 */
open class AbstractMaskProgressLayoutListener : MaskProgressLayoutListener {

    override fun onItemAdd(
        view: View,
        gridMedia: GridMedia,
        alreadyImageCount: Int,
        alreadyVideoCount: Int,
        alreadyAudioCount: Int
    ) {
    }

    override fun onItemClick(view: View, gridMedia: GridMedia) {}

    override fun onItemStartUploading(gridMedia: GridMedia, viewHolder: PhotoAdapter.PhotoViewHolder) {}

    override fun onItemAudioStartUploading(gridMedia: GridMedia, playProgressView: PlayProgressView) {}

    override fun onItemClose(gridMedia: GridMedia) {}

    override fun onItemAudioStartDownload(view: View, url: String) {}

    override fun onItemVideoStartDownload(view: View, gridMedia: GridMedia): Boolean {
        return false
    }

    override fun onAddDataSuccess(gridMedia: List<GridMedia>) {}

}