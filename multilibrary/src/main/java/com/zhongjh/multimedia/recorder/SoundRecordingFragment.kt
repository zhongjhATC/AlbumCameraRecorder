package com.zhongjh.multimedia.recorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import com.zhongjh.multimedia.camera.ui.camera.CameraFragment
import com.zhongjh.multimedia.databinding.FragmentSoundrecordingZjhBinding
import com.zhongjh.multimedia.recorder.widget.SoundRecordingLayout

/**
 * 录音
 *
 * @author zhongjh
 * @date 2018/8/22
 */
class SoundRecordingFragment : BaseSoundRecordingFragment() {
    lateinit var mBinding: FragmentSoundrecordingZjhBinding

    companion object {
        fun newInstance(): SoundRecordingFragment {
            return SoundRecordingFragment()
        }
    }

    override fun setContentView(inflater: LayoutInflater, container: ViewGroup?): View {
        mBinding = FragmentSoundrecordingZjhBinding.inflate(inflater)
        return mBinding.root
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
    }

    override val soundRecordingLayout: SoundRecordingLayout
        get() = mBinding.pvLayout
    override val chronometer: Chronometer
        get() = mBinding.chronometer


}
