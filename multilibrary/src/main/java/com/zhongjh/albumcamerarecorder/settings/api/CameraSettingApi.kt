package com.zhongjh.albumcamerarecorder.settings.api

import androidx.camera.core.ImageCapture
import com.zhongjh.albumcamerarecorder.camera.ui.camera.BaseCameraFragment
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.CameraStateManagement
import com.zhongjh.albumcamerarecorder.camera.ui.camera.presenter.BaseCameraPicturePresenter
import com.zhongjh.albumcamerarecorder.camera.ui.camera.presenter.BaseCameraVideoPresenter
import com.zhongjh.albumcamerarecorder.settings.CameraSetting
import com.zhongjh.common.coordinator.VideoMergeCoordinator
import com.zhongjh.albumcamerarecorder.camera.listener.OnCaptureListener
import com.zhongjh.common.enums.MimeType

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
    fun cameraFragment(baseCameraFragment: BaseCameraFragment<CameraStateManagement, BaseCameraPicturePresenter, BaseCameraVideoPresenter>): CameraSetting

    /**
     * 支持的类型：图片，视频
     * 这个优先于 [com.zhongjh.albumcamerarecorder.settings.MultiMediaSetting.choose]
     *
     * @param mimeTypes 类型
     * @return [CameraSetting] for fluent API.
     */
    fun mimeTypeSet(mimeTypes: Set<MimeType>): CameraSetting

    /**
     * 设置每秒的录制帧数
     *
     * @param videoFrameRate 录制帧数
     * @return [CameraSetting] for fluent API.
     */
    fun videoFrameRate(videoFrameRate: Int): CameraSetting

    /**
     * 设置编码比特率
     *
     * @param videoBitRate 编码比特率
     * @return [CameraSetting] for fluent API.
     */
    fun videoBitRate(videoBitRate: Int): CameraSetting

    /**
     * 是否开启图片高清拍摄
     * 注意开启该模式后，录制界面不能同时存在拍摄图片功能和录制视频功能
     *
     * @param enable whether to enable
     * @return [CameraSetting] for fluent API.
     */
    fun enableImageHighDefinition(enable: Boolean): CameraSetting

    /**
     * 是否开启视频高清录制
     * 注意开启该模式后，录制界面不能同时存在拍摄图片功能和录制视频功能
     *
     * @param enable whether to enable
     * @return [CameraSetting] for fluent API.
     */
    fun enableVideoHighDefinition(enable: Boolean): CameraSetting

    /**
     * 最长录制时间,默认10秒
     *
     * @param duration 最长录制时间,单位为秒
     * @return [CameraSetting] for fluent API.
     */
    fun duration(duration: Int): CameraSetting

    /**
     * 最短录制时间限制，单位为毫秒，即是如果长按在1500毫秒内，都暂时不开启录制
     *
     * @param minDuration 最短录制时间限制，单位为毫秒
     * @return [CameraSetting] for fluent API.
     */
    fun minDuration(minDuration: Int): CameraSetting

    /**
     * 点击即录制（点击拍摄图片功能则失效）
     *
     * @param isClickRecord 是：开启该功能，否：关闭该功能
     * @return [CameraSetting] for fluent API.
     */
    fun isClickRecord(isClickRecord: Boolean): CameraSetting

    /**
     * 启动视频编辑功能，目前只有视频分段录制，后续会增加
     *
     * @param videoEditManager 视频编辑协调者
     * @return [CameraSetting] for fluent API.
     */
    fun videoMerge(videoEditManager: VideoMergeCoordinator): CameraSetting

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
     * 自定义拍照、录制的输出文件夹路径
     *
     * @param outPutCameraDir 输出文件夹路径
     */
    fun outPutCameraDir(outPutCameraDir: String): CameraSetting

    /**
     * 自定义拍照、录制文件名
     *
     * @param outPutCameraFileName 输出文件的名称
     */
    fun outPutCameraFileName(outPutCameraFileName: String): CameraSetting

    /**
     * 自定义图片的后缀名格式
     *
     * @param imageFormat 图片的后缀名格式
     */
    fun imageFormat(imageFormat: String): CameraSetting

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
     * 基于之前有人需求拍照后获取当前拍照的方位，所以开放该接口
     *
     * @param listener [OnCaptureListener]
     * @return [CameraSetting] this
     */
    fun setOnCaptureListener(listener: OnCaptureListener): CameraSetting
}