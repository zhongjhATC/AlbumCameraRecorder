package com.zhongjh.albumcamerarecorder

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zhongjh.albumcamerarecorder.album.loader.MediaLoader
import com.zhongjh.albumcamerarecorder.album.loader.MediaPageLoader
import com.zhongjh.albumcamerarecorder.album.ui.album.SelectedData
import com.zhongjh.common.entity.LocalMedia

/**
 * 选择数据的ViewModel，缓存相关数据给它的子Fragment共同使用
 *
 * @author zhongjh
 * @date 2023/10/19
 */
class SelectedModel(application: Application) : AndroidViewModel(application) {

    private val tag: String = this@SelectedModel.javaClass.simpleName

    /**
     * 当前选择的数据操作文件类
     */
    var selectedData: SelectedData

    /**
     * 当前选择的数据更改
     */
    private val _selectedDataChange = MutableLiveData<LocalMedia>()
    val selectedDataChange: LiveData<LocalMedia> get() = _selectedDataChange

    init {
        selectedData =
            SelectedData(
                application
            )
    }

    /**
     * 选择的数据添加
     */
    fun addSelectedData(item: LocalMedia) {
        selectedData.add(item)
        // 通知更新
        _selectedDataChange.postValue(item)
    }


}