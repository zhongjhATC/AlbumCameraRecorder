package com.zhongjh.albumcamerarecorder.preview.start

import com.zhongjh.albumcamerarecorder.preview.enum.PreviewType
import com.zhongjh.common.entity.GridMedia
import com.zhongjh.common.entity.LocalMedia

/**
 * 预览界面的相关配置
 */
object PreviewSpec {

    /**
     * 预览类型
     */
    var previewType: PreviewType = PreviewType.ALBUM_ACTIVITY

    /**
     * 数据源
     */
    var localMediaArrayList: ArrayList<LocalMedia>? = null

    /**
     * 当前选择的数据索引
     */
    var currentPosition: Int = 0

    /**
     * 设置是否开启原图
     */
    var isOriginal: Boolean = true

    /**
     * 设置是否开启 选择时验证是否满足可以被选中的条件
     */
    var isSelectedCheck: Boolean = true

    /**
     * 设置是否开启编辑功能
     */
    var isEdit: Boolean = true

    /**
     * 设置是否启动 '确定' 功能
     */
    var isApply: Boolean = true

    val cleanInstance = PreviewSpec
        get() {
            val previewSpec: PreviewSpec = field
            previewSpec.reset()
            return previewSpec
        }

    /**
     * 重置
     */
    private fun reset() {
        previewType = PreviewType.ALBUM_ACTIVITY
        localMediaArrayList = null
        currentPosition = 0
        isOriginal = true
        isSelectedCheck = true
        isEdit = true
        isApply = true
    }
}