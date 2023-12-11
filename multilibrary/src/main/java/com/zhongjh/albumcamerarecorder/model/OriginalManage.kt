package com.zhongjh.albumcamerarecorder.model

import androidx.fragment.app.Fragment
import com.zhongjh.albumcamerarecorder.R
import com.zhongjh.albumcamerarecorder.album.utils.PhotoMetadataUtils
import com.zhongjh.albumcamerarecorder.settings.AlbumSpec
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.widget.IncapableDialog

/**
 * 统一管理原图有关功能模块
 * 涉及界面：相册界面、预览界面
 */
class OriginalManage(
    private val mFragment: Fragment,
    private val mMainModel: MainModel,
    private val mSelectedModel: SelectedModel,
    private val mAlbumSpec: AlbumSpec
) {

    /**
     * 点击原图
     */
    fun originalClick() {
        // 如果有大于限制大小的，就提示
        val count: Int = countOverMaxSize()
        if (count > 0) {
            val incapableDialog = IncapableDialog.newInstance(
                "",
                mFragment.getString(
                    R.string.z_multi_library_error_over_original_count,
                    count,
                    mAlbumSpec.originalMaxSize
                )
            )
            incapableDialog.show(mFragment.childFragmentManager, IncapableDialog::class.java.name)
            return
        }
        // 设置状态
        mMainModel.setOriginalEnable(!mMainModel.getOriginalEnable())
        // 设置状态是否原图
        mAlbumSpec.onCheckedListener?.onCheck(mMainModel.getOriginalEnable())
    }

    /**
     * 更新原图状态
     */
    fun updateOriginalState() {
        if (countOverMaxSize() > 0) {
            // 如果开启了原图功能
            if (mMainModel.getOriginalEnable()) {
                // 弹框提示取消原图
                val incapableDialog = IncapableDialog.newInstance(
                    "", mFragment.getString(
                        R.string.z_multi_library_error_over_original_size,
                        mAlbumSpec.originalMaxSize
                    )
                )
                incapableDialog.show(
                    mFragment.parentFragmentManager, IncapableDialog::class.java.name
                )
                // 去掉原图按钮的选择状态
                mMainModel.setOriginalEnable(false)

                mAlbumSpec.onCheckedListener?.onCheck(mMainModel.getOriginalEnable())
            }
        }
    }

    /**
     * 返回大于限定mb的图片数量
     *
     * @return 数量
     */
    private fun countOverMaxSize(): Int {
        var count = 0
        val selectedCount: Int = mSelectedModel.selectedData.count()
        for (i in 0 until selectedCount) {
            val item: LocalMedia = mSelectedModel.selectedData.localMedias[i]
            if (item.isImage()) {
                val size = PhotoMetadataUtils.getSizeInMb(item.size)
                if (size > mAlbumSpec.originalMaxSize) {
                    count++
                }
            }
        }
        return count
    }

}