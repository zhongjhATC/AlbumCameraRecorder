package com.zhongjh.multimedia.model

import android.content.Context
import android.util.Log
import com.zhongjh.common.entity.IncapableCause
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.entity.SelectedItem
import com.zhongjh.common.enums.Constant.IMAGE
import com.zhongjh.common.enums.Constant.IMAGE_VIDEO
import com.zhongjh.common.enums.Constant.VIDEO
import com.zhongjh.multimedia.R
import com.zhongjh.multimedia.album.entity.SelectedCountMessage
import com.zhongjh.multimedia.settings.AlbumSpec.mediaTypeExclusive
import com.zhongjh.multimedia.utils.LocalMediaUtils.isAcceptable
import com.zhongjh.multimedia.utils.SelectableUtils.imageMaxCount
import com.zhongjh.multimedia.utils.SelectableUtils.imageVideoMaxCount
import com.zhongjh.multimedia.utils.SelectableUtils.isImageMaxCount
import com.zhongjh.multimedia.utils.SelectableUtils.isVideoMaxCount
import com.zhongjh.multimedia.utils.SelectableUtils.videoMaxCount

/**
 * 选择数据源,处理相关逻辑
 * 这是贯穿相册的整个数据
 *
 * @author zhongjh
 */
open class SelectedData(private val mContext: Context) {

    /**
     * 【唯一数据源】只保留这一个集合，存放媒体+选中顺序
     */
    val selectedItems = mutableListOf<SelectedItem>()

    /**
     * 当前选择的图片数量
     */
    private var mSelectedImageCount = 0

    /**
     * 当前选择的视频数量
     */
    private var mSelectedVideoCount = 0

    /**
     * 当前选择的所有类型，列表如果包含了图片和视频，就会变成混合类型
     */
    private var mCollectionType = COLLECTION_UNDEFINED

    /**
     * 将资源对象添加到已选中集合
     *
     * @param item 数据
     */
    fun add(item: LocalMedia): Boolean {
        // 先判断是否已存在
        if (isSelected(item)) {
            return false
        }

        val newOrder = selectedItems.size + 1
        selectedItems.add(SelectedItem(item, newOrder))
        // 如果只选中了图片Item， mCollectionType 设置为 COLLECTION_IMAGE
        // 如果只选中了图片影音资源，mCollectionType 设置为 COLLECTION_IMAGE
        // 如果两种都选择了，mCollectionType 设置为 COLLECTION_MIXED
        // 同步图文计数缓存
        if (item.isImage()) {
            mSelectedImageCount++
        } else if (item.isVideo()) {
            mSelectedVideoCount++
        }

        // 如果是空的数据源
        if (mCollectionType == COLLECTION_UNDEFINED) {
            if (item.isImage()) {
                // 如果是图片，就设置图片类型
                mCollectionType = COLLECTION_IMAGE
            } else if (item.isVideo()) {
                // 如果是视频，就设置视频类型
                mCollectionType = COLLECTION_VIDEO
            }
        } else if (mCollectionType == COLLECTION_IMAGE) {
            // 如果当前是图片类型
            if (item.isVideo()) {
                // 选择了视频，就设置混合模式
                mCollectionType = COLLECTION_MIXED
            }
        } else if (mCollectionType == COLLECTION_VIDEO) {
            // 如果当前是图片类型
            if (item.isImage()) {
                // 选择了图片，就设置混合模式
                mCollectionType = COLLECTION_MIXED
            }
        }
        return true
    }

    /**
     * 添加所有选择的数据源
     *
     * @param localMediaArrayList 选择的数据源
     */
    fun addAll(localMediaArrayList: ArrayList<LocalMedia>) {
        localMediaArrayList.forEach { add(it) }
    }

    /**
     * 删除数据源某项
     *
     * @param item 数据
     * @return 是否删除成功
     */
    fun remove(item: LocalMedia): Boolean {
        val targetIndex = selectedItems.indexOfFirst {
            it.media.fileId == item.fileId
        }
        if (targetIndex == -1) {
            return false
        }
        val removedItem = selectedItems.removeAt(targetIndex)
        // 先扣减图文缓存计数
        if (removedItem.media.isImage()) {
            mSelectedImageCount--
        } else if (removedItem.media.isVideo()) {
            mSelectedVideoCount--
        }
        // 删除后统一重排所有序号，保证数字连续
        reIndexAllOrder()

        if (selectedItems.isEmpty()) {
            // 如果删除后没有数据，设置当前类型为空
            mCollectionType = COLLECTION_UNDEFINED
        } else {
            if (mCollectionType == COLLECTION_MIXED) {
                currentMaxSelectable()
                Log.d("currentMaxSelectable", "currentMaxSelectable")
            }
        }
        Log.d("onSaveInstanceState", selectedItems.size.toString() + " remove")
        return false
    }

    /**
     * 重置数据源
     *
     * @param items          数据源
     * @param collectionType 类型
     */
    fun overwrite(items: ArrayList<LocalMedia>, collectionType: Int) {
        mCollectionType = if (items.isEmpty()) {
            COLLECTION_UNDEFINED
        } else {
            collectionType
        }
        // 全量重建缓存
        rebuildAllCache(items)
        Log.d("onSaveInstanceState", selectedItems.size.toString() + " overwrite")
    }

    /**
     * 该item是否在选择中
     *
     * @param item 数据源
     * @return 返回是否选择
     */
    fun isSelected(item: LocalMedia): Boolean {
        return selectedItems.any { it.media.fileId == item.fileId }
    }

    /**
     * 返回选择的num，0=未选中
     *
     * @param item 数据
     * @return 选择的索引，最终返回的选择了第几个
     */
    fun checkedNumOf(item: LocalMedia): Int {
        return selectedItems.firstOrNull { it.media.fileId == item.fileId }?.order ?: 0
    }

    /**
     * 获取数据源长度
     *
     * @return 数据源长度
     */
    fun count(): Int {
        return selectedItems.size
    }

    /**
     * 当前数量 和 当前选择最大数量比较 是否相等
     *
     * @return boolean
     */
    fun maxSelectableReached(): Boolean {
        Log.d("onSaveInstanceState", selectedItems.size.toString() + " maxSelectableReached")
        return selectedItems.size == currentMaxSelectable()
    }

    /**
     * 验证当前item是否满足可以被选中的条件
     *
     * @param item 数据item
     * @return 弹窗
     */
    fun isAcceptable(item: LocalMedia): IncapableCause? {
        Log.d("onSaveInstanceState", selectedItems.size.toString() + " isAcceptable")
        var maxSelectableReached = false
        var maxSelectable = 0
        var type = ""
        val selectedCountMessage: SelectedCountMessage
        // 判断是否混合视频图片模式
        if (!mediaTypeExclusive) {
            // 混合检查
            item.mimeType?.let { mimeType ->
                if (mimeType.startsWith(IMAGE)) {
                    selectedCountMessage = isImageMaxCount(mSelectedImageCount, mSelectedVideoCount)
                    if (selectedCountMessage.isMaxSelectableReached) {
                        maxSelectableReached = true
                        maxSelectable = selectedCountMessage.maxCount
                        type = selectedCountMessage.type
                    }
                } else if (mimeType.startsWith(VIDEO)) {
                    selectedCountMessage = isVideoMaxCount(mSelectedVideoCount, mSelectedImageCount)
                    if (selectedCountMessage.isMaxSelectableReached) {
                        maxSelectableReached = true
                        maxSelectable = selectedCountMessage.maxCount
                        type = selectedCountMessage.type
                    }
                }
            }
            return newIncapableCause(item, maxSelectableReached, maxSelectable, true, type)
        } else {
            // 非混合模式
            maxSelectableReached = maxSelectableReached()
            maxSelectable = currentMaxSelectable()
            return newIncapableCause(item, maxSelectableReached, maxSelectable, false, null)
        }
    }

    /**
     * 删除中间条目后，重新统一编排所有选中序号，保证连续 1、2、3...
     */
    private fun reIndexAllOrder() {
        selectedItems.forEachIndexed { index, selectedItem ->
            selectedItem.order = index + 1
        }
    }

    /**
     * 验证当前item是否满足可以被选中的条件
     *
     * @param item                 数据item
     * @param maxSelectableReached 是否已经选择最大值
     * @param maxSelectable        选择的最大数量
     * @param isMashup             提示是否提示
     * @param type                 类型
     * @return 弹窗
     */
    private fun newIncapableCause(item: LocalMedia, maxSelectableReached: Boolean, maxSelectable: Int, isMashup: Boolean, type: String?): IncapableCause? {
        // 检查是否超过最大设置数量
        if (maxSelectableReached) {
            val cause = getCause(maxSelectable, isMashup, type)
            // 生成窗口
            return IncapableCause(cause)
        } else if (typeConflict(item)) {
            // 判断选择资源(图片跟视频)是否类型冲突
            return IncapableCause(mContext.getString(R.string.z_multi_library_error_type_conflict))
        }
        // 过滤文件
        return isAcceptable(mContext, item)
    }

    /**
     * 根据相关参数构造文本消息
     *
     * @param maxSelectable 选择的最大数量
     * @param isMashup      提示是否提示
     * @param type          类型
     * @return 文本消息
     */
    private fun getCause(maxSelectable: Int, isMashup: Boolean, type: String?): String {
        var cause = ""
        if (isMashup) {
            when (type) {
                IMAGE_VIDEO -> cause = mContext.resources.getString(
                    R.string.z_multi_library_error_over_count,
                    maxSelectable
                )
                IMAGE -> cause = mContext.resources.getString(
                    R.string.z_multi_library_error_over_count_image,
                    maxSelectable
                )
                VIDEO -> cause = mContext.resources.getString(
                    R.string.z_multi_library_error_over_count_video,
                    maxSelectable
                )
                else -> {}
            }
        } else {
            cause = mContext.resources.getString(
                R.string.z_multi_library_error_over_count,
                maxSelectable
            )
        }
        return cause
    }

    /**
     * 返回最多选择的数量
     *
     * @return 数量
     */
    private fun currentMaxSelectable(): Int {
        // 判断是否能同时选择视频和图片
        val leastCount = if (!mediaTypeExclusive) {
            // 返回视频+图片
            imageVideoMaxCount
        } else {
            when (mCollectionType) {
                COLLECTION_IMAGE -> imageMaxCount
                COLLECTION_VIDEO -> videoMaxCount
                else -> imageVideoMaxCount
            }
        }

        Log.d("onSaveInstanceState", selectedItems.size.toString() + " currentMaxSelectable")
        return leastCount
    }

    /**
     * 判断选择资源(图片跟视频)是否类型冲突
     */
    private fun typeConflict(item: LocalMedia): Boolean {
        // 是否可以同时选择不同的资源类型 true表示不可以 false表示可以
        return (mediaTypeExclusive
                && ((item.isImage() && (mCollectionType == COLLECTION_VIDEO || mCollectionType == COLLECTION_MIXED))
                || (item.isVideo() && (mCollectionType == COLLECTION_IMAGE || mCollectionType == COLLECTION_MIXED))))
    }

    /**
     *  重建序号映射 + 图文计数缓存
     */
    private fun rebuildAllCache(items: ArrayList<LocalMedia>) {
        selectedItems.clear()
        mSelectedImageCount = 0
        mSelectedVideoCount = 0
        // 根据所有已选择的数据赋值
        items.forEachIndexed { index, media ->
            selectedItems.add(SelectedItem(media, index + 1))
            // 同步图文计数缓存
            if (media.isImage()) {
                mSelectedImageCount++
            } else if (media.isVideo()) {
                mSelectedVideoCount++
            }
        }
    }

    companion object {
        /**
         * 数据源的标记
         */
        const val STATE_SELECTION: String = "state_selection"

        /**
         * 空的数据类型
         */
        const val COLLECTION_UNDEFINED: Int = 0x00

        /**
         * 图像数据类型
         */
        const val COLLECTION_IMAGE: Int = 0x01

        /**
         * 视频数据类型
         */
        const val COLLECTION_VIDEO: Int = 0x01 shl 1

        /**
         * 图像和视频混合类型
         */
        private const val COLLECTION_MIXED = COLLECTION_IMAGE or COLLECTION_VIDEO
    }
}
