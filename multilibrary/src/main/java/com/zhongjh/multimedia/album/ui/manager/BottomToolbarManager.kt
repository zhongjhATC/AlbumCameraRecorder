package com.zhongjh.multimedia.album.ui.manager

import android.view.View
import com.zhongjh.multimedia.R
import com.zhongjh.multimedia.databinding.FragmentAlbumZjhBinding
import com.zhongjh.multimedia.model.OriginalManage
import com.zhongjh.multimedia.settings.AlbumSpec

class BottomToolbarManager(
    private val binding: FragmentAlbumZjhBinding, private val albumSpec: AlbumSpec,
    private val originalManage: OriginalManage
) {

    /**
     * 更新选中数量与按钮状态
     */
    fun updateSelectedState(selectedCount: Int) {
        binding.buttonPreview.isEnabled = selectedCount > 0
        binding.buttonApply.isEnabled = selectedCount > 0
        binding.buttonApply.text = if (selectedCount == 0) {
            binding.root.context.getString(R.string.z_multi_library_button_sure_default)
        } else {
            binding.root.context.getString(R.string.z_multi_library_button_sure, selectedCount)
        }
    }

    /**
     * 更新原图控件状态
     */
    fun updateOriginalState(enabled: Boolean) {
        binding.original.setChecked(enabled)
        binding.groupOriginal.visibility = if (albumSpec.originalEnable) View.VISIBLE else View.INVISIBLE
    }

}