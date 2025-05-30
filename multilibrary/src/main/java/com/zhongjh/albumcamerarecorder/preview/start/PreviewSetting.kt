package com.zhongjh.albumcamerarecorder.preview.start

import android.content.Intent
import android.os.Bundle
import com.zhongjh.albumcamerarecorder.preview.enum.PreviewType
import com.zhongjh.albumcamerarecorder.preview.start.api.PreviewApi
import com.zhongjh.common.entity.LocalMedia

/**
 * 因为打开预览界面的方式很多种，该类单独抽取出来做处理
 * 主要是将intent封装成链式代码
 *
 * @param previewType 预览类型
 */
class PreviewSetting(previewType: PreviewType) : PreviewApi {

    companion object {

        /**
         * 数据源的标记
         */
        const val PREVIEW_DATA = "preview_data"

        /**
         * 当前索引
         */
        const val CURRENT_POSITION = "current_position"

        /**
         * 告诉接收数据的界面是直接 add 数据源
         */
        const val EXTRA_RESULT_APPLY = "extra_result_apply"
        const val EXTRA_RESULT_IS_EDIT = "extra_result_is_edit"

        /**
         * 设置是否开启原图
         */
        const val EXTRA_RESULT_ORIGINAL_ENABLE = "extra_result_original_enable"

        /**
         * 设置是否启动 '确定' 功能
         */
        const val APPLY_ENABLE = "apply_enable"

        /**
         * 设置是否启动选择功能
         */
        const val SELECTED_ENABLE = "selected_enable"

        /**
         * 设置是否开启编辑功能
         */
        const val EDIT_ENABLE = "edit_enable"

        /**
         * 验证当前item是否满足可以被选中的条件
         */
        const val IS_SELECTED_CHECK = "is_selected_check"

        /**
         * 预览类型，表达从什么界面打开进来的
         */
        const val PREVIEW_TYPE = "preview_type"
    }

    /**
     * 每次使用配置前都重新清除配置
     */
    private val previewSpec: PreviewSpec = PreviewSpec.cleanInstance

    init {
        previewSpec.previewType = previewType
    }

    override fun setData(localMediaArrayList: ArrayList<LocalMedia>): PreviewSetting {
        previewSpec.data = localMediaArrayList
        return this
    }

    override fun setCurrentPosition(currentPosition: Int): PreviewSetting {
        previewSpec.currentPosition = currentPosition
        return this
    }

    override fun isOriginal(isOriginal: Boolean): PreviewSetting {
        previewSpec.isOriginal = isOriginal
        return this
    }

    override fun isSelectedCheck(isSelectedCheck: Boolean): PreviewSetting {
        previewSpec.isSelectedCheck = isSelectedCheck
        return this
    }

    override fun isEdit(isEdit: Boolean): PreviewSetting {
        previewSpec.isEdit = isEdit
        return this
    }

    override fun isApply(isApply: Boolean): PreviewSetting {
        previewSpec.isApply = isApply
        return this
    }

    override fun setIntent(intent: Intent) {
        intent.putExtra(PREVIEW_TYPE, previewSpec.previewType)
        intent.putParcelableArrayListExtra(PREVIEW_DATA, previewSpec.data)
        intent.putExtra(CURRENT_POSITION, previewSpec.currentPosition)
        intent.putExtra(EXTRA_RESULT_ORIGINAL_ENABLE, previewSpec.isOriginal)
        intent.putExtra(IS_SELECTED_CHECK, previewSpec.isSelectedCheck)
        intent.putExtra(EDIT_ENABLE, previewSpec.isEdit)
        intent.putExtra(APPLY_ENABLE, previewSpec.isApply)
    }

    override fun setBundle(bundle: Bundle) {
        bundle.putSerializable(PREVIEW_TYPE, previewSpec.previewType)
        bundle.putParcelableArrayList(PREVIEW_DATA, previewSpec.data)
        bundle.putInt(CURRENT_POSITION, previewSpec.currentPosition)
        bundle.putBoolean(EXTRA_RESULT_ORIGINAL_ENABLE, previewSpec.isOriginal)
        bundle.putBoolean(IS_SELECTED_CHECK, previewSpec.isSelectedCheck)
        bundle.putBoolean(EDIT_ENABLE, previewSpec.isEdit)
        bundle.putBoolean(APPLY_ENABLE, previewSpec.isApply)
    }

}