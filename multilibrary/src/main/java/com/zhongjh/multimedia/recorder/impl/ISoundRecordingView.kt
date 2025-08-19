package com.zhongjh.multimedia.recorder.impl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import com.zhongjh.multimedia.camera.widget.PhotoVideoLayout
import com.zhongjh.multimedia.recorder.widget.SoundRecordingLayout

/**
 * 录音界面规定view的接口
 *
 * @author zhongjh
 * @date 2025/8/18
 */
interface ISoundRecordingView {

    /**
     * 初始化根布局
     *
     * @param inflater  onCreateView方法下面的inflater
     * @param container onCreateView方法下面的container
     * @return 返回布局View
     */
    fun setContentView(inflater: LayoutInflater, container: ViewGroup?): View

    /**
     * 初始化相关view
     *
     * @param view               初始化好的view
     * @param savedInstanceState savedInstanceState
     */
    fun initView(view: View, savedInstanceState: Bundle?)

    /**
     * 当想使用自带的功能按钮（包括拍摄、录制、录音、确认、取消），请设置它
     *
     * @return soundRecordingLayout
     */
    val soundRecordingLayout: SoundRecordingLayout


    val chronometer: Chronometer

}