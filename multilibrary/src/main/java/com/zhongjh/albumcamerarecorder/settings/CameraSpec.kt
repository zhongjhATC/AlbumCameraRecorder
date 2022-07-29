package com.zhongjh.albumcamerarecorder.settings

import com.zhongjh.albumcamerarecorder.R
import com.zhongjh.albumcamerarecorder.camera.constants.FlashModels
import com.zhongjh.albumcamerarecorder.camera.listener.OnCameraViewListener
import com.zhongjh.albumcamerarecorder.camera.listener.OnCaptureListener
import com.zhongjh.albumcamerarecorder.constants.ModuleTypes
import com.zhongjh.common.coordinator.VideoMergeCoordinator
import com.zhongjh.common.enums.MimeType
import com.zhongjh.common.enums.MimeType.Companion.ofImage
import com.zhongjh.common.enums.MimeType.Companion.ofVideo

/**
 * @author zhongjh
 */
object CameraSpec {

    // region 属性

    /**
     * 选择 视频图片 的类型，MimeType.allOf()
     */
    var mimeTypeSet: Set<MimeType>? = null

    /**
     * 是否开启图片高清拍摄
     * 注意开启该模式后，录制界面不能同时存在拍摄图片功能和录制视频功能
     */
    var enableImageHighDefinition = false

    /**
     * 是否开启视频高清录制
     * 注意开启该模式后，录制界面不能同时存在拍摄图片功能和录制视频功能
     */
    var enableVideoHighDefinition = false

    /**
     * 切换前置/后置摄像头图标资源
     */
    var imageSwitch = R.drawable.ic_camera_zjh

    /**
     * 闪光灯开启状态图标
     */
    var imageFlashOn = R.drawable.ic_flash_on

    /**
     * 闪光灯关闭状态图标
     */
    var imageFlashOff = R.drawable.ic_flash_off

    /**
     * 闪光灯自动状态图标
     */
    var imageFlashAuto = R.drawable.ic_flash_auto

    /**
     * 闪光灯模式
     * 默认闪光灯关闭模式
     */
    var flashModel = FlashModels.TYPE_FLASH_OFF

    /**
     * 通过keyCode触发拍照事件
     */
    var keyCodeTakePhoto = 0

    /**
     * 是否开启闪光灯记忆模式
     * 在开启闪光某个模式（例如闪光灯开启模式）后，在界面结束时，会自动记录当前模式（例如闪光灯开启模式），下次再打开时，依然是这个模式（例如闪光灯开启模式）
     */
    var enableFlashMemoryModel = false

    /**
     * 最长录制时间
     */
    var duration = 10

    /**
     * 最短录制时间限制，单位为毫秒，即是如果长按在1500毫秒内，都暂时不开启录制
     */
    var minDuration = 1500

    /**
     * 视频分段录制合并功能
     */
    var videoMergeCoordinator: VideoMergeCoordinator? = null

    /**
     * 水印资源id
     */
    var watermarkResource = -1

    /**
     * 是否点击即录制（点击拍摄图片功能则失效）
     */
    var isClickRecord = false

    /**
     * 仅支持图片
     */
    fun onlySupportImages(): Boolean {
        return ofImage().containsAll(GlobalSpec.getMimeTypeSet(ModuleTypes.CAMERA))
    }

    /**
     * 仅支持视频
     */
    fun onlySupportVideos(): Boolean {
        return ofVideo().containsAll(GlobalSpec.getMimeTypeSet(ModuleTypes.CAMERA))
    }

    /**
     * @return 是否开启了分段录制视频合并功能
     */
    val isMergeEnable: Boolean
        get() = videoMergeCoordinator != null

    /**
     * CameraView有关事件
     */
    var onCameraViewListener: OnCameraViewListener? = null

    /**
     * 拍摄后操作图片的事件
     */
    var onCaptureListener: OnCaptureListener? = null

    // endregion 属性

    val cleanInstance = CameraSpec
        get() {
            val cameraSpec: CameraSpec = field
            cameraSpec.reset()
            return cameraSpec
        }

    /**
     * 重置
     */
    private fun reset() {
        mimeTypeSet = null
        // 切换前置/后置摄像头图标资源
        imageSwitch = R.drawable.ic_camera_zjh
        // 闪光灯开启状态图标
        imageFlashOn = R.drawable.ic_flash_on
        // 闪光灯关闭状态图标
        imageFlashOff = R.drawable.ic_flash_off
        // 闪光灯自动状态图标
        imageFlashAuto = R.drawable.ic_flash_auto
        flashModel = FlashModels.TYPE_FLASH_OFF
        enableFlashMemoryModel = false
        // 最长录制时间
        duration = 10
        // 最短录制时间限制，单位为毫秒，即是如果长按在1500毫秒内，都暂时不开启录制
        minDuration = 1500
        videoMergeCoordinator = null
        watermarkResource = -1
    }

}