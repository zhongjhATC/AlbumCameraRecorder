package com.zhongjh.albumcamerarecorder.settings

import com.zhongjh.albumcamerarecorder.camera.listener.OnCameraViewListener
import com.zhongjh.albumcamerarecorder.camera.listener.OnCaptureListener
import com.zhongjh.albumcamerarecorder.settings.CameraSpec.cleanInstance
import com.zhongjh.albumcamerarecorder.settings.api.CameraSettingApi
import com.zhongjh.common.coordinator.VideoMergeCoordinator
import com.zhongjh.common.enums.MimeType
import com.zhongjh.common.enums.MimeType.Companion.ofImage
import com.zhongjh.common.enums.MimeType.Companion.ofVideo

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
        mCameraSpec.onCameraViewListener = listener
        return this
    }

    override fun setOnCaptureListener(listener: OnCaptureListener): CameraSetting {
        mCameraSpec.onCaptureListener = listener
        return this
    }

}