package com.zhongjh.albumcamerarecorder.settings

import androidx.camera.core.ImageCapture.FLASH_MODE_OFF
import com.zhongjh.albumcamerarecorder.R
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
     * 设置每秒的录制帧数
     */
    var videoFrameRate = 0

    /**
     * 设置编码比特率
     */
    var videoBitRate = 0

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
    var flashMode = FLASH_MODE_OFF

    /**
     * 通过keyCode触发拍照事件
     */
    var keyCodeTakePhoto = 0

    /**
     * 自定义拍照、录制的输出文件夹路径
     */
    var outPutCameraDir: String? = null

    /**
     * 输出文件的名称
     */
    var outPutCameraFileName: String? = null

    /**
     * 输出图片文件的后缀名
     */
    var imageFormat: String? = null

    /**
     * 输出视频文件的后缀名
     */
    var videoFormat: String? = null

    /**
     * 是否开启闪光灯记忆模式
     * 在开启闪光某个模式（例如闪光灯开启模式）后，在界面结束时，会自动记录当前模式（例如闪光灯开启模式），下次再打开时，依然是这个模式（例如闪光灯开启模式）
     */
    var enableFlashMemoryModel = false

    /**
     * 最长录制时间，单位为毫秒
     */
    var maxDuration = 1000

    /**
     * 最短录制时间限制，单位为毫秒，如果录制期间低于2000毫秒，均不算录制
     * 值不能低于2000，如果低于2000还是以2000为准
     */
    var minDuration = 2000

    /**
     * 长按准备时间，单位为毫秒，即是如果长按在1000毫秒内，都暂时不开启录制
     */
    var readinessDuration = 1000

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
        videoFrameRate = 0
        videoBitRate = 0
        // 切换前置/后置摄像头图标资源
        imageSwitch = R.drawable.ic_camera_zjh
        // 闪光灯开启状态图标
        imageFlashOn = R.drawable.ic_flash_on
        // 闪光灯关闭状态图标
        imageFlashOff = R.drawable.ic_flash_off
        // 闪光灯自动状态图标
        imageFlashAuto = R.drawable.ic_flash_auto
        flashMode = FLASH_MODE_OFF
        keyCodeTakePhoto = 0
        outPutCameraDir = null
        outPutCameraFileName = null
        imageFormat = null
        videoFormat = null
        enableFlashMemoryModel = false
        // 最长录制时间
        maxDuration = 1000
        minDuration = 2000
        readinessDuration = 1000
        videoMergeCoordinator = null
        watermarkResource = -1
    }

}