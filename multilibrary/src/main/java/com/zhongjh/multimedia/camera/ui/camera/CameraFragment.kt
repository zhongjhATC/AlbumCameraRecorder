package com.zhongjh.multimedia.camera.ui.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.camera.view.PreviewView
import androidx.recyclerview.widget.RecyclerView
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraPictureManager
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraVideoManager
import com.zhongjh.multimedia.camera.ui.camera.state.CameraStateManager
import com.zhongjh.multimedia.camera.widget.FocusView
import com.zhongjh.multimedia.camera.widget.PhotoVideoLayout
import com.zhongjh.multimedia.databinding.FragmentCameraZjhBinding
import com.zhongjh.multimedia.widget.ImageViewTouch
import com.zhongjh.multimedia.widget.childclickable.IChildClickableLayout

/**
 * 继承于BaseCameraFragment
 *
 * @author zhongjh
 * @date 2022/8/12
 * @noinspection unused
 */
class CameraFragment : BaseCameraFragment<CameraStateManager, CameraPictureManager, CameraVideoManager>() {
    override val cameraPictureManager: CameraPictureManager = CameraPictureManager(this)
    override val cameraVideoManager: CameraVideoManager = CameraVideoManager(this)
    override val cameraStateManager: CameraStateManager = CameraStateManager(this)
    lateinit var mBinding: FragmentCameraZjhBinding

    override fun setContentView(inflater: LayoutInflater, container: ViewGroup?): View {
        mBinding = FragmentCameraZjhBinding.inflate(inflater)
        return mBinding.root
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        mainActivity?.let { mainActivity ->
            mBinding.previewView.previewStreamState.observe(mainActivity) { }
        }
    }

    override val childClickableLayout: IChildClickableLayout
        get() = mBinding.rlMain

    override val topView: View
        get() = mBinding.clMenu

    override val previewView: PreviewView
        get() = mBinding.previewView

    override val focusView: FocusView
        get() = mBinding.focusView

    override val recyclerViewPhoto: RecyclerView
        get() = mBinding.rlPhoto

    override val multiplePhotoView: Array<View>
        get() = arrayOf(mBinding.vLine1, mBinding.vLine2)

    override val photoVideoLayout: PhotoVideoLayout
        get() = mBinding.pvLayout

    override val singlePhotoView: ImageViewTouch
        get() = mBinding.imgPhoto

    override val closeView: View
        get() = mBinding.imgClose

    override val flashView: ImageView
        get() = mBinding.imgFlash

    override val switchView: ImageView
        get() = mBinding.imgSwitch

    companion object {
        fun newInstance(): CameraFragment {
            return CameraFragment()
        }
    }
}
