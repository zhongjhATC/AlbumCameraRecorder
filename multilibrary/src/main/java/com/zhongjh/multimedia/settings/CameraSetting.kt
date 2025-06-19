package com.zhongjh.multimedia.settings

import androidx.camera.core.ImageCapture.FlashMode
import com.zhongjh.multimedia.camera.ui.camera.BaseCameraFragment
import com.zhongjh.multimedia.camera.listener.OnCaptureListener
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraPictureManager
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraVideoManager
import com.zhongjh.multimedia.camera.ui.camera.state.CameraStateManager
import com.zhongjh.multimedia.settings.CameraSpec.cleanInstance
import com.zhongjh.multimedia.settings.api.CameraSettingApi
import com.zhongjh.common.enums.MimeType
import com.zhongjh.common.enums.MimeType.Companion.ofImage
import com.zhongjh.common.enums.MimeType.Companion.ofVideo
import com.zhongjh.multimedia.camera.listener.OnInitCameraManager
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
        mCameraSpec.onCaptureListener = null
        mCameraSpec.onInitCameraManager = null
    }

    /**
     * 赋予自定义的CameraFragment
     * 如果设置则使用自定义的CameraFragment,否则使用默认的CameraFragment
     * 每次使用要重新赋值，因为会在每次关闭界面后删除该Fragment
     */
    var baseCameraFragment: BaseCameraFragment<CameraStateManager, CameraPictureManager, CameraVideoManager>? =
        null

    override fun cameraFragment(baseCameraFragment: BaseCameraFragment<CameraStateManager, CameraPictureManager, CameraVideoManager>): CameraSetting {
        this.baseCameraFragment = baseCameraFragment
        return this
    }

    fun clearCameraFragment() {
        this.baseCameraFragment = null
    }

    override fun mimeTypeSet(mimeTypes: Set<MimeType>): CameraSetting {
        mCameraSpec.mimeTypeSet = mimeTypes
        return this
    }

    override fun duration(duration: Int): CameraSetting {
        mCameraSpec.maxDuration = duration
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

    override fun flashMode(@FlashMode flashMode: Int): CameraSetting {
        mCameraSpec.flashMode = flashMode
        return this
    }

    override fun onKeyDownTakePhoto(keyCode: Int): CameraSetting {
        mCameraSpec.keyCodeTakePhoto = keyCode
        return this
    }

    override fun videoFormat(videoFormat: String): CameraSetting {
        mCameraSpec.videoFormat = videoFormat
        return this
    }

    override fun enableFlashMemoryModel(enableFlashMemoryModel: Boolean): CameraSetting {
        mCameraSpec.enableFlashMemoryModel = enableFlashMemoryModel
        return this
    }

    override fun setOnCaptureListener(listener: OnCaptureListener): CameraSetting {
        mCameraSpec.onCaptureListener = WeakReference(listener).get()
        return this
    }

    override fun setOnInitCameraManager(listener: OnInitCameraManager): CameraSetting {
        mCameraSpec.onInitCameraManager = WeakReference(listener).get()
        return this
    }

}