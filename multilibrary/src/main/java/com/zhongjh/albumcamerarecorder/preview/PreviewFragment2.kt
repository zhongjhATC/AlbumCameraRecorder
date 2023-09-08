package com.zhongjh.albumcamerarecorder.preview

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.zhongjh.albumcamerarecorder.album.ui.main.MainModel
import com.zhongjh.albumcamerarecorder.preview.base.BasePreviewFragment2
import com.zhongjh.common.entity.LocalMedia
import java.util.ArrayList

class PreviewFragment2 : BasePreviewFragment2() {

    /**
     * 来源于Activity的MainModel
     */
    private lateinit var mMainModel: MainModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mMainModel = ViewModelProvider(requireActivity())[MainModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    /**
     * 获取当前的数据
     */
    override fun getDatas(): ArrayList<LocalMedia> {
        return mMainModel.getLocalMedias().value?.data ?: ArrayList<LocalMedia>()
    }
}