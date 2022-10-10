package com.zhongjh.albumcamerarecorder.settings

import com.zhongjh.albumcamerarecorder.camera.ui.camera.BaseCameraFragment
import com.zhongjh.albumcamerarecorder.camera.listener.OnCameraViewListener
import com.zhongjh.albumcamerarecorder.camera.listener.OnCaptureListener
import com.zhongjh.albumcamerarecorder.camera.ui.camera.presenter.BaseCameraPicturePresenter
import com.zhongjh.albumcamerarecorder.camera.ui.camera.presenter.BaseCameraVideoPresenter
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.CameraStateManagement
import com.zhongjh.albumcamerarecorder.settings.CameraSpec.cleanInstance
import com.zhongjh.albumcamerarecorder.settings.api.CameraSettingApi
import com.zhongjh.common.coordinator.VideoMergeCoordinator
import com.zhongjh.common.enums.MimeType
import com.zhongjh.common.enums.MimeType.Companion.ofImage
import com.zhongjh.common.enums.MimeType.Companion.ofVideo
import java.lang.ref.WeakReference

/**
 * 有关拍摄界面的动态设置
 *
 * @author zhongjh
 * @date 2018/12/26
 */
class CameraSetting : CameraSettingApi {

    private val mCameraSpec: CameraSpec = cleanInstance

    /**
     * 销毁事件
     */
    fun onDestroy() {
        mCameraSpec.onCameraViewListener = null
    }

    /**
     * 赋予自定义的CameraFragment
     * 如果设置则使用自定义的CameraFragment,否则使用默认的CameraFragment
     * 每次使用要重新赋值，因为会在每次关闭界面后删除该Fragment
     */
    var baseCameraFragment: BaseCameraFragment<CameraStateManagement, BaseCameraPicturePresenter, BaseCameraVideoPresenter>? =
            null

    override fun cameraFragment(baseCameraFragment: BaseCameraFragment<CameraStateManagement, BaseCameraPicturePresenter, BaseCameraVideoPresenter>): CameraSetting {
        this.baseCameraFragment = baseCameraFragment
        return this
    }

    fun clearCameraFragment() {
        this.baseCameraFragment = null
    }

    override fun mimeTypeSet(mimeTypes: Set<MimeType>): CameraSetting {
        // 如果设置了高清模式，则优先以高清模式为准
        if (!mCameraSpec.enableImageHighDefinition && !mCameraSpec.enableVideoHighDefinition) {
            mCameraSpec.mimeTypeSet = mimeTypes
        }
        return this
    }

    override fun enableImageHighDefinition(enable: Boolean): CameraSetting {
        mCameraSpec.enableImageHighDefinition = enable
        // 如果启用图片高清，就禁用录制视频
        if (enable) {
            mCameraSpec.mimeTypeSet = ofImage()
        }
        return this
    }

    override fun enableVideoHighDefinition(enable: Boolean): CameraSetting {
        mCameraSpec.enableVideoHighDefinition = enable
        // 如果启用视频高清，就禁用拍摄图片,并且单击就能录制
        if (enable) {
            mCameraSpec.mimeTypeSet = ofVideo()
            mCameraSpec.isClickRecord = true
        }
        return this
    }

    override fun duration(duration: Int): CameraSetting {
        mCameraSpec.duration = duration
        return this
    }

    override fun minDuration(minDuration: Int): CameraSetting {
        mCameraSpec.minDuration = minDuration
        return this
    }

    override fun isClickRecord(isClickRecord: Boolean): CameraSetting {
        mCameraSpec.isClickRecord = isClickRecord
        return this
    }

    override fun videoMerge(videoEditManager: VideoMergeCoordinator): CameraSetting {
        mCameraSpec.videoMergeCoordinator = videoEditManager
        return this
    }

    override fun watermarkResource(watermarkResource: Int): CameraSetting {
        mCameraSpec.watermarkResource = watermarkResource
        return this
    }

    override fun imageSwitch(imageSwitch: Int): CameraSetting {
        mCameraSpec.imageSwitch = imageSwitch
        return this
    }

    override fun imageFlashOn(imageFlashOn: Int): CameraSetting {
        mCameraSpec.imageFlashOn = imageFlashOn
        return this
    }

    override fun imageFlashOff(imageFlashOff: Int): CameraSetting {
        mCameraSpec.imageFlashOff = imageFlashOff
        return this
    }

    override fun imageFlashAuto(imageFlashAuto: Int): CameraSetting {
        mCameraSpec.imageFlashAuto = imageFlashAuto
        return this
    }

    override fun flashModel(flashModel: Int): CameraSetting {
        mCameraSpec.flashModel = flashModel
        return this
    }

    override fun onKeyDownTakePhoto(keyCode: Int): CameraSetting {
        mCameraSpec.keyCodeTakePhoto = keyCode
        return this
    }

    override fun enableFlashMemoryModel(enableFlashMemoryModel: Boolean): CameraSetting {
        mCameraSpec.enableFlashMemoryModel = enableFlashMemoryModel
        return this
    }

    override fun setOnCameraViewListener(listener: OnCameraViewListener): CameraSetting {
        mCameraSpec.onCameraViewListener = WeakReference(listener).get()
        return this
    }

    override fun setOnCaptureListener(listener: OnCaptureListener): CameraSetting {
        mCameraSpec.onCaptureListener = WeakReference(listener).get()
        return this
    }

}