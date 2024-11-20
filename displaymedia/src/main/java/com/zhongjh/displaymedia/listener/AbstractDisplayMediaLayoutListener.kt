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
    override fun onItemStartUploading(
        displayMedia: DisplayMedia,
        viewHolder: ImagesAndVideoAdapter.PhotoViewHolder
    ) {
    }

    override fun onItemClose(displayMedia: DisplayMedia) {}
    override fun onItemAudioStartDownload(audioHolder: AudioAdapter.AudioHolder, url: String) {
    }

    override fun onItemVideoStartDownload(
        view: View,
        displayMedia: DisplayMedia,
        position: Int
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun onAddDataSuccess(displayMedias: List<DisplayMedia>) {}

}