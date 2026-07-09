package com.zhongjh.multimedia.model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.utils.LogUtil
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 选中变更事件
 * @param position 当前操作索引
 * @param isMaxStateChanged 是否引起「达到上限/解除上限」状态切换
 */
data class SelectChangeEvent(
    val position: Int,
    val isMaxStateChanged: Boolean
)

/**
 * 选择数据的ViewModel，缓存相关数据给它的子Fragment共同使用
 *
 * @author zhongjh
 * @date 2023/10/19
 */
class SelectedModel(application: Application) : AndroidViewModel(application) {

    /**
     * 当前选择的数据操作文件类
     */
    private val selectedData: SelectedData = SelectedData(application)

    /**
     * 提供 selectedData 的只读访问接口
     */
    fun getSelectedData(): SelectedData = selectedData

    /**
     * 当前选择的数据更改
     */
    private val _selectedDataChangeEvent = MutableSharedFlow<SelectChangeEvent>(extraBufferCapacity = 1)
    val selectedDataChangeEvent: SharedFlow<SelectChangeEvent> = _selectedDataChangeEvent.asSharedFlow()

    /**
     * 选择的数据添加
     */
    fun addSelectedData(item: LocalMedia, position: Int) {
        LogUtil.d("AlbumFragmentFlow","SelectedModel.addSelectedData")
        val beforeMax = selectedData.maxSelectableReached()

        item.isChecked = true
        selectedData.add(item)

        val afterMax = selectedData.maxSelectableReached()
        val event = SelectChangeEvent(
            position = position,
            isMaxStateChanged = beforeMax != afterMax
        )
        // 通知更新
        _selectedDataChangeEvent.tryEmit(event)
    }

    /**
     * 选择的数据删除
     */
    fun removeSelectedData(item: LocalMedia, position: Int) {
        LogUtil.d("AlbumFragmentFlow","SelectedModel.removeSelectedData")
        val beforeMax = selectedData.maxSelectableReached()

        item.isChecked = false
        selectedData.remove(item)

        val afterMax = selectedData.maxSelectableReached()

        val event = SelectChangeEvent(
            position = position,
            isMaxStateChanged = beforeMax != afterMax
        )
        // 通知更新
        _selectedDataChangeEvent.tryEmit(event)
    }

    /**
     * 清空所有数据源
     */
    fun clearAllData() {
        getSelectedData().clearAll()
    }


}