package com.zhongjh.multimedia.model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.zhongjh.common.entity.LocalMedia
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

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
    private val _selectedDataChangeEvent = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val selectedDataChangeEvent: SharedFlow<Int> = _selectedDataChangeEvent.asSharedFlow()

    /**
     * 选择的数据添加
     */
    fun addSelectedData(item: LocalMedia, position: Int) {
        Log.d("AlbumFragmentFlow","SelectedModel.addSelectedData")
        item.isChecked = true
        selectedData.add(item)
        // 通知更新
        _selectedDataChangeEvent.tryEmit(position)
    }

    /**
     * 选择的数据删除
     */
    fun removeSelectedData(item: LocalMedia, position: Int) {
        Log.d("AlbumFragmentFlow","SelectedModel.removeSelectedData")
        item.isChecked = false
        selectedData.remove(item)
        // 通知更新
        _selectedDataChangeEvent.tryEmit(position)
    }

    /**
     * 清空所有数据源
     */
    fun clearAllData() {
        getSelectedData().clearAll()
    }


}