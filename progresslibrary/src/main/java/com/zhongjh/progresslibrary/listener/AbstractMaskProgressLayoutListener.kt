package com.zhongjh.progresslibrary.listener

import android.view.View
import com.zhongjh.progresslibrary.entity.MultiMediaView

/**
 * "抽象接口"
 * @author zhongjh
 * @date 2021/9/7
 */
open class AbstractMaskProgressLayoutListener : MaskProgressLayoutListener {

    override fun onItemAdd(view: View, multiMediaView: MultiMediaView,
                           alreadyImageCount: Int, alreadyVideoCount: Int, alreadyAudioCount: Int) {}

    override fun onItemClick(view: View, multiMediaView: MultiMediaView) {}

    override fun onItemStartUploading(multiMediaView: MultiMediaView) {}

    override fun onItemClose(view: View, multiMediaView: MultiMediaView) {}

    override fun onItemAudioStartDownload(view: View, url: String) {}

    override fun onItemVideoStartDownload(view: View, multiMediaView: MultiMediaView): Boolean {
        return false
    }

    override fun onAddDataSuccess(multiMediaViews: List<MultiMediaView>) {}

}