package com.zhongjh.multimedia.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zhongjh.common.entity.LocalMedia

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
    private val _selectedDataChange = MutableLiveData<LocalMedia>()
    val selectedDataChange: LiveData<LocalMedia> get() = _selectedDataChange

    /**
     * 选择的数据添加
     */
    fun addSelectedData(item: LocalMedia) {
        selectedData.add(item)
        // 通知更新
        _selectedDataChange.postValue(item)
    }

    /**
     * 选择的数据删除
     */
    fun removeSelectedData(item: LocalMedia) {
        selectedData.remove(item)
        // 通知更新
        _selectedDataChange.postValue(item)
    }


}