package com.zhongjh.gridview.listener

import android.view.View
import com.zhongjh.gridview.apapter.GridAdapter
import com.zhongjh.gridview.entity.GridMedia

/**
 * 抽象接口
 * @author zhongjh
 * @date 2021/9/7
 */
open class AbstractGridViewListener : GridViewListener {

    override fun onItemAdd(
        view: View,
        gridMedia: GridMedia,
        alreadyImageCount: Int,
        alreadyVideoCount: Int,
        alreadyAudioCount: Int
    ) {
    }

    override fun onItemClick(view: View, gridMedia: GridMedia) {}

    override fun onItemStartUploading(
        gridMedia: GridMedia,
        viewHolder: GridAdapter.PhotoViewHolder
    ) {
    }

    override fun onItemClose(gridMedia: GridMedia) {}

    override fun onItemStartDownload(
        view: View,
        gridMedia: GridMedia,
        position: Int
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun onAddDataSuccess(gridMedia: List<GridMedia>) {}

}