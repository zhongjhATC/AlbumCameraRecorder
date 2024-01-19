package com.zhongjh.displaymedia.listener

import android.view.View
import com.zhongjh.displaymedia.apapter.AudioAdapter
import com.zhongjh.displaymedia.apapter.ImagesAndVideoAdapter
import com.zhongjh.displaymedia.entity.DisplayMedia

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

    override fun onItemAudioStartUploading(displayMedia: DisplayMedia, viewHolder: AudioAdapter.VideoHolder) {}

    override fun onItemClose(displayMedia: DisplayMedia) {}

    override fun onItemAudioStartDownload(holder: AudioAdapter.VideoHolder, url: String) {}

    override fun onItemVideoStartDownload(view: View, displayMedia: DisplayMedia): Boolean {
        return false
    }

    override fun onAddDataSuccess(displayMedia: List<DisplayMedia>) {}

}