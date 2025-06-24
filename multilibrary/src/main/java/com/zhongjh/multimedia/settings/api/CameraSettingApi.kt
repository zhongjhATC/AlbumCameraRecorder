package com.zhongjh.multimedia.settings.api

import androidx.camera.core.ImageCapture
import com.zhongjh.multimedia.camera.ui.camera.BaseCameraFragment
import com.zhongjh.multimedia.camera.ui.camera.state.CameraStateManager
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraPictureManager
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraVideoManager
import com.zhongjh.multimedia.settings.CameraSetting
import com.zhongjh.multimedia.camera.listener.OnCaptureListener
import com.zhongjh.common.enums.MimeType
import com.zhongjh.multimedia.camera.listener.OnInitCameraManager

/**
 * 有关拍摄界面的动态设置
 *
 * @author zhongjh
 * @date 2019/3/20
 */
interface CameraSettingApi {
    /**
     * 赋予自定义的CameraFragment
     * 如果设置则使用自定义的CameraFragment,否则使用默认的CameraFragment
     *
     * @param baseCameraFragment CameraFragment的基类，必须继承它实现才可设置
     * @return [CameraSetting] for fluent API.
     */
    fun cameraFragment(baseCameraFragment: BaseCameraFragment<CameraStateManager, CameraPictureManager, CameraVideoManager>): CameraSetting

    /**
     * 支持的类型：图片，视频。只识别图片、视频类型，并不会具体到更细致的类型
     * 这个优先于 [com.zhongjh.multimedia.settings.MultiMediaSetting.choose]
     *
     * @param mimeTypes 类型
     * @return [CameraSetting] for fluent API.
     */
    fun mimeTypeSet(mimeTypes: Set<MimeType>): CameraSetting

    /**
     * 最长录制时间,默认10秒
     *
     * @param duration 最长录制时间,单位为秒
     * @return [CameraSetting] for fluent API.
     */
    fun maxDuration(duration: Int): CameraSetting

    /**
     * 最短录制时间限制，单位为毫秒，即是如果长按在1500毫秒内，都暂时不开启录制
     *
     * @param minDuration 最短录制时间限制，单位为毫秒
     * @return [CameraSetting] for fluent API.
     */
    fun minDuration(minDuration: Int): CameraSetting

    /**
     * 长按准备时间，单位为毫秒，即是如果长按在1000毫秒内，都暂时不开启录制
     */
    fun readinessDuration(readinessDuration: Int): CameraSetting

    /**
     * 点击即录制（点击拍摄图片功能则失效）
     *
     * @param isClickRecord 是：开启该功能，否：关闭该功能
     * @return [CameraSetting] for fluent API.
     */
    fun isClickRecord(isClickRecord: Boolean): CameraSetting

    /**
     * 水印资源,可通过layout赋值水印，所处于的位置等等都可通过layout本身来处理
     *
     * @param watermarkResource 水印资源的layout id
     * @return [CameraSetting] for fluent API.
     */
    fun watermarkResource(watermarkResource: Int): CameraSetting

    /**
     * 更换 切换前置/后置摄像头图标资源
     *
     * @param imageSwitch 切换前置/后置摄像头图标资源
     * @return [CameraSetting] for fluent API.
     */
    fun imageSwitch(imageSwitch: Int): CameraSetting

    /**
     * 更换 闪光灯开启状态图标
     *
     * @param imageFlashOn 闪光灯开启状态图标
     * @return [CameraSetting] for fluent API.
     */
    fun imageFlashOn(imageFlashOn: Int): CameraSetting

    /**
     * 更换 闪光灯关闭状态图标
     *
     * @param imageFlashOff 闪光灯关闭状态图标
     * @return [CameraSetting] for fluent API.
     */
    fun imageFlashOff(imageFlashOff: Int): CameraSetting

    /**
     * 更换 闪光灯自动状态图标
     *
     * @param imageFlashAuto 闪光灯自动状态图标
     * @return [CameraSetting] for fluent API.
     */
    fun imageFlashAuto(imageFlashAuto: Int): CameraSetting

    /**
     * 更换 闪光灯默认模式，默认是闪光灯关闭模式
     *
     * @param flashMode 闪光灯默认模式
     * @return [CameraSetting] for fluent API.
     */
    fun flashMode(@ImageCapture.FlashMode flashMode: Int): CameraSetting

    /**
     * 通过Key触发拍照事件
     *
     * @param keyCode 例如升音量键或者降音量键都触发拍照事件则传递 KeyEvent.KEYCODE_VOLUME_DOWN|KeyEvent.KEYCODE_VOLUME_UP
     * @return [CameraSetting] for fluent API.
     */
    fun onKeyDownTakePhoto(keyCode: Int): CameraSetting

    /**
     * 自定义视频的后缀名格式
     *
     * @param videoFormat 视频的后缀名格式
     */
    fun videoFormat(videoFormat: String): CameraSetting

    /**
     * 是否开启闪光灯记忆模式，默认关闭
     * 在开启闪光某个模式（例如闪光灯开启模式）后，在界面结束时，会自动记录当前模式（例如闪光灯开启模式）
     * 下次再打开时，依然是这个模式（例如闪光灯开启模式）
     *
     * @param enableFlashMemoryModel 是否开启
     * @return [CameraSetting] for fluent API.
     */
    fun enableFlashMemoryModel(enableFlashMemoryModel: Boolean): CameraSetting

    /**
     * 有关拍摄后添加、删除图片后触发的事件
     * 例如有需求拍照后获取当前拍照的方位，可以通过该接口设置
     *
     * @param listener [OnCaptureListener]
     * @return [CameraSetting] this
     */
    fun setOnCaptureListener(listener: OnCaptureListener): CameraSetting

    /**
     * 有关拍摄界面初始化时触发的事件
     *
     * @param listener [OnCaptureListener]
     * @return [CameraSetting] this
     */
    fun setOnInitCameraManager(listener: OnInitCameraManager): CameraSetting
}