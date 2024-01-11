package com.zhongjh.grid.listener

import android.view.View
import com.zhongjh.grid.entity.ProgressMedia

/**
 * "抽象接口"
 * @author zhongjh
 * @date 2021/9/7
 */
open class AbstractMaskProgressLayoutListener : MaskProgressLayoutListener {

    override fun onItemAdd(view: View, progressMedia: ProgressMedia,
                           alreadyImageCount: Int, alreadyVideoCount: Int, alreadyAudioCount: Int) {}

    override fun onItemClick(view: View, progressMedia: ProgressMedia) {}

    override fun onItemStartUploading(progressMedia: ProgressMedia) {}

    override fun onItemClose(view: View, progressMedia: ProgressMedia) {}

    override fun onItemAudioStartDownload(view: View, url: String) {}

    override fun onItemVideoStartDownload(view: View, progressMedia: ProgressMedia): Boolean {
        return false
    }

    override fun onAddDataSuccess(progressMedia: List<ProgressMedia>) {}

}