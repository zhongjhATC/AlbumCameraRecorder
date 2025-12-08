package com.zhongjh.multimedia.camera.listener

import android.net.Uri
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.effects.OverlayEffect
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView

/**
 * 初始化事件
 *
 * @author zhongjh
 * @date 2018/10/11
 */
@Suppress("EmptyMethod")
interface OnInitCameraManager {

    /**
     * 自定义摄像头预览参数
     * 拍照模式 或者 拍照+录像模式有效
     *
     * @param previewBuilder 可自定义摄像头预览参数
     * @param screenAspectRatio 计算当前手机得出的适合比例
     * @param rotation 当前预览摄像头的角度
     */
    fun initPreview(previewBuilder: Preview.Builder, screenAspectRatio: Int, rotation: Int)

    /**
     * 自定义拍摄图片参数
     * 拍照模式 或者 拍照+录像模式有效
     *
     * @param imageBuilder 可自定义拍摄图片参数
     * @param screenAspectRatio 计算当前手机得出的适合比例
     * @param rotation 当前预览摄像头的角度
     */
    fun initImageCapture(imageBuilder: ImageCapture.Builder, screenAspectRatio: Int, rotation: Int)

    /**
     * 自定义图片分析参数
     * 只有拍照模式模式有效
     *
     * @param imageAnalyzerBuilder 自定义图片分析参数
     * @param screenAspectRatio 计算当前手机得出的适合比例
     * @param rotation 当前预览摄像头的角度
     */
    fun initImageAnalyzer(imageAnalyzerBuilder: ImageAnalysis.Builder, screenAspectRatio: Int, rotation: Int)

    /**
     * 自定义录制视频参数,录制视频比较特殊,如果要设置分辨率和比例请在 initVideoRecorder 执行
     * 录像模式 或者 拍照+录像模式有效
     *
     * @param videoCaptureBuilder 自定义录制视频参数
     * @param rotation 当前预览摄像头的角度
     */
    fun initVideoCapture(videoCaptureBuilder: VideoCapture.Builder<Recorder>, rotation: Int)

    /**
     * 自定义录制视频参数,录制视频比较特殊,如果要设置录制角度，请在initVideoCapture 执行
     * @param screenAspectRatio 计算当前手机得出的适合比例
     */
    fun initVideoRecorder(recorder: Recorder.Builder, screenAspectRatio: Int)

    /**
     * 是否使用默认的叠加效果
     * 如果initOverlayEffect返回不为null，则以自定义的水印为准
     */
    fun isDefaultOverlayEffect(): Boolean

    /**
     * 自定义叠加效果,一般用于水印,实时画面
     * @param previewView 预览view,用于获取宽高
     */
    fun initOverlayEffect(previewView: PreviewView): OverlayEffect?

    /**
     * 自定义图片水印效果,这个建议只有在自定义高分辨率拍照、预览的时候才设置
     * @param uri 图片uri
     * @param path 源图路径
     */
    fun initWatermarkedImage(uri: Uri, path: String)
}