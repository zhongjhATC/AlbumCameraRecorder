package com.zhongjh.multimedia.camera.ui.camera.manager

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.Builder
import androidx.camera.core.ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
import androidx.camera.core.ImageCapture.Metadata
import androidx.camera.core.ImageCapture.OutputFileOptions
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.MeteringPointFactory
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.VideoCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.zhongjh.multimedia.camera.listener.OnCameraManageListener
import com.zhongjh.multimedia.camera.listener.OnCameraXOrientationEventListener
import com.zhongjh.multimedia.camera.listener.OnCameraXPreviewViewTouchListener
import com.zhongjh.multimedia.camera.ui.camera.CameraFragment.ViewHolder
import com.zhongjh.multimedia.camera.ui.camera.impl.ICameraView
import com.zhongjh.multimedia.settings.CameraSpec
import com.zhongjh.multimedia.utils.FileMediaUtil
import com.zhongjh.multimedia.utils.MediaStoreUtils.DCIM_CAMERA
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.enums.MediaType
import com.zhongjh.common.enums.MimeType
import com.zhongjh.common.utils.DisplayMetricsUtils
import com.zhongjh.common.utils.UriUtils
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


/**
 * 专门处理camerax的类
 */
class CameraManage(private val appCompatActivity: AppCompatActivity, val viewHolder: ViewHolder, val iCameraView: ICameraView) :
    OnCameraXOrientationEventListener.OnOrientationChangedListener {

    companion object {
        /**
         * 两个宽高比例
         */
        const val RATIO_4_3_VALUE = 4.0 / 3.0
        const val RATIO_16_9_VALUE = 16.0 / 9.0
        const val TAG = "CameraManage"
    }

    /**
     * 回调监听
     */
    var onCameraManageListener: OnCameraManageListener? = null

    /**
     * 拍摄配置
     */
    private val cameraSpec by lazy { CameraSpec }
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private lateinit var cameraProvider: ProcessCameraProvider
    private var videoCapture: androidx.camera.video.VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private val displayManager by lazy { appCompatActivity.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager }
    private val displayListener by lazy { DisplayListener() }
    private val orientationEventListener by lazy {
        OnCameraXOrientationEventListener(
            appCompatActivity, this
        )
    }
    private lateinit var cameraInfo: CameraInfo

    /**
     * 摄像头控制器
     */
    private lateinit var cameraControl: CameraControl
    private val mainExecutor: Executor by lazy { ContextCompat.getMainExecutor(appCompatActivity) }

    /**
     * 相机模式
     */
    private var useCameraCases = LifecycleCameraController.IMAGE_CAPTURE

    /**
     * 摄像头方向
     */
    private var lensFacing = CameraSelector.LENS_FACING_BACK

    /**
     * 显示id
     */
    private var displayId = -1

    /**
     * 界面是否被覆盖了，如果是被覆盖的情况下，会结束录制，同时不会处理录制后打开视频文件的相关处理
     */
    private var isActivityPause = false

    /**
     * 是否启动音频
     */
    var isAudio = false

    /**
     * 输出最后一帧的状态
     */
    private var lastStreamState: PreviewView.StreamState? = null

    /**
     * ui初始化，必须是ui线程
     */
    fun init() {
        displayManager.registerDisplayListener(displayListener, null)
        viewHolder.previewView.post { displayId = viewHolder.previewView.display.displayId }
        startCheckOrientation()
        startCamera()

        // 选择数据改变
        viewHolder.previewView.previewStreamState
            .observe(appCompatActivity) { streamState ->
                if (lastStreamState == null) {
                    lastStreamState = streamState
                }
                when (streamState) {
                    PreviewView.StreamState.IDLE -> {
                        if (lastStreamState != streamState) {
                            lastStreamState = streamState
                            // 停止输出画面后仍会停留在最后一帧,设置黑色前景遮挡住最后一帧画面
                            viewHolder.previewView.foreground =
                                ContextCompat.getDrawable(appCompatActivity, android.R.color.background_dark)
                        }
                    }

                    PreviewView.StreamState.STREAMING -> {
                        if (lastStreamState != streamState) {
                            lastStreamState = streamState
                            // 开始输出画面后清空前景
                            viewHolder.previewView.foreground = null
                        }
                    }

                    else -> {}
                }
            }
    }

    /**
     * onDestroy
     */
    fun onDestroy() {
        displayManager.unregisterDisplayListener(displayListener)
        recording?.close()
        stopCheckOrientation()
    }

    /**
     * App显示出来
     */
    fun onResume() {
        // 重新启动预览
        startCheckOrientation()
        startCamera()
        isActivityPause = false
    }

    /**
     * App被遮挡
     */
    fun onPause() {
        // 解除绑定,这样切换别的界面可以提高性能
        cameraProvider.unbindAll()
        // 停止录制
        isActivityPause = true
        // Activity触发了Pause,通知视频录制重置View
        onCameraManageListener?.onActivityPause()
        stopVideo()
    }

    /**
     * 拍照
     */
    fun takePictures() {
        imageCapture?.let { imageCapture ->
            // 判断是否绑定了mImageCapture
            if (!cameraProvider.isBound(imageCapture)) {
                bindCameraPreviewModeByImage()
            }
            // 设置图片模式
            useCameraCases = LifecycleCameraController.IMAGE_CAPTURE
            // 该设置解决 前置摄像头左右镜像 问题
            val isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
            val metadata = Metadata()
            metadata.isReversedHorizontal = isReversedHorizontal
            // 设置输出路径,因为有可能多图的原因,所以先暂时全部放进cache文件夹里面
            val cameraFile = FileMediaUtil.createCacheFile(appCompatActivity, MediaType.TYPE_PICTURE)
            val fileOptions = OutputFileOptions.Builder(cameraFile).setMetadata(metadata).build()
            // 进行拍照
            imageCapture.takePicture(
                fileOptions, mainExecutor, TakePictureCallback(this@CameraManage, onCameraManageListener)
            )
        }
    }

    /**
     * 录制
     */
    @SuppressLint("MissingPermission")
    fun takeVideo() {
        recording?.resume() ?: let {
            val name = "VIDEO_" + SimpleDateFormat(
                "yyyyMMdd_HHmmssSSS", Locale.US
            ).format(System.currentTimeMillis()) + ".mp4"
            val contentValues = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, name)
                put(MediaStore.Video.Media.RELATIVE_PATH, DCIM_CAMERA)
            }
            val mediaStoreOutput = MediaStoreOutputOptions.Builder(
                appCompatActivity.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            ).setContentValues(contentValues).build()
            val pendingRecording = videoCapture?.output?.prepareRecording(appCompatActivity, mediaStoreOutput)
            if (isAudio) {
                pendingRecording?.withAudioEnabled()
            }
            recording = pendingRecording?.start(ContextCompat.getMainExecutor(appCompatActivity)) { videoRecordEvent ->
                // 视频录制监控回调
                when (videoRecordEvent) {
                    is VideoRecordEvent.Status -> {
                        // 录制时间大于0才代表真正开始,通知长按按钮开始动画
                        if (videoRecordEvent.recordingStats.recordedDurationNanos > 0) {
                            Log.d(TAG, "mRecordedTime 开始" + videoRecordEvent.recordingStats.recordedDurationNanos)
                            onCameraManageListener?.onRecordStart()
                        }
                    }

                    is VideoRecordEvent.Finalize -> {
                        Log.d(
                            TAG,
                            "Finalize  " + videoRecordEvent.error + " " + videoRecordEvent.outputResults.outputUri + " isActivityPause:" + isActivityPause
                        )
                        if (!isActivityPause) {
                            // 完成录制
                            val uri = videoRecordEvent.outputResults.outputUri
                            onCameraManageListener?.onRecordSuccess(
                                UriUtils.uriToFile(
                                    appCompatActivity,
                                    uri
                                ).absolutePath
                            )
                        }
                        isActivityPause = false
                    }

                    is VideoRecordEvent.Pause -> {
                        // 暂停录制
                        Log.d(TAG, "Pause")
                        onCameraManageListener?.onRecordPause(videoRecordEvent.recordingStats.recordedDurationNanos)
                    }

                    is VideoRecordEvent.Resume -> {
                        // 恢复录制
                        Log.d(TAG, "Resume")
                    }
                }
            }
        }
    }

    /**
     * 停止录制,生成文件
     */
    fun stopVideo() {
        recording?.stop()
        recording = null
    }

    /**
     * 恢复录制
     */
    fun resumeVideo() {
        recording?.resume()
    }

    /**
     * 暂停录制
     */
    fun pauseVideo() {
        recording?.pause()
    }

    /**
     * 停止录制
     */
    fun closeVideo() {
        recording?.close()
    }

    /**
     * 设置闪光灯模式
     */
    fun setFlashMode(@ImageCapture.FlashMode flashMode: Int) {
        imageCapture?.flashMode = flashMode
    }

    /**
     * 切换前后摄像头
     */
    fun toggleFacing() {
        lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }
        initCameraPreviewMode()
    }

    /**
     * 启动预览
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(appCompatActivity)
        cameraProviderFuture.addListener({
            try {
                // 在 Runnable 中,添加 ProcessCameraProvider.它用于将相机的生命周期绑定到应用进程中的 LifecycleOwner
                cameraProvider = cameraProviderFuture.get()
                initCameraPreviewMode()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, mainExecutor)
    }

    /**
     * 绑定相机预览模式
     */
    private fun initCameraPreviewMode() {
        // 如果有设置高清模式，则根据相应高清模式更改模式
        if (cameraSpec.enableImageHighDefinition) {
            bindCameraPreviewModeByImage()
        } else if (cameraSpec.enableVideoHighDefinition) {
            bindCameraPreviewModeByVideo()
        } else {
            // 最后再判断具体什么模式
            if (cameraSpec.onlySupportImages()) {
                bindCameraPreviewModeByImage()
            } else if (cameraSpec.onlySupportVideos()) {
                bindCameraPreviewModeByVideo()
            } else {
                bindCameraPreviewModeImageAndVideo()
            }
        }
    }

    /**
     * 绑定 - 只拍照模式
     */
    private fun bindCameraPreviewModeByImage() {
        try {
            // 获取适合的比例
            val screenAspectRatio: Int =
                aspectRatio(
                    DisplayMetricsUtils.getScreenWidth(appCompatActivity),
                    DisplayMetricsUtils.getScreenHeight(appCompatActivity)
                )
            // 获取当前预览的角度
            val rotation: Int = viewHolder.previewView.display.rotation
            // 获取前后置摄像头
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            // 初始化 Preview 对象，在其上调用 build，从取景器中获取 Surface 提供程序，然后在预览上进行设置。
            val preview = Preview.Builder().setTargetAspectRatio(screenAspectRatio).setTargetRotation(rotation).build()
            // 初始化 ImageCapture
            initImageCapture(screenAspectRatio)
            // 初始化 ImageAnalysis
            imageAnalyzer =
                ImageAnalysis.Builder().setTargetAspectRatio(screenAspectRatio).setTargetRotation(rotation).build()
            // 确保没有任何内容绑定到 cameraProvider
            cameraProvider.unbindAll()
            // 绑定preview
            preview.setSurfaceProvider(viewHolder.previewView.surfaceProvider)
            // 因为是只拍照模式,所以将 mImageCapture 用例与现有 preview 和 mImageAnalyzer 用例绑定
            val camera = cameraProvider.bindToLifecycle(
                (appCompatActivity as LifecycleOwner), cameraSelector, preview, imageCapture, imageAnalyzer
            )
            onCameraManageListener?.bindSucceed()
            cameraInfo = camera.cameraInfo
            cameraControl = camera.cameraControl
            initCameraPreviewListener()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 绑定 - 只录制视频模式
     */
    private fun bindCameraPreviewModeByVideo() {
        try {
            // 获取当前预览的角度
            val rotation: Int = viewHolder.previewView.display.rotation
            // 获取前后置摄像头
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            // 初始化 Preview 对象，在其上调用 build，从取景器中获取 Surface 提供程序，然后在预览上进行设置。
            val preview = Preview.Builder().setTargetRotation(rotation).build().also {
                it.setSurfaceProvider(viewHolder.previewView.surfaceProvider)
            }
            // 初始化 VideoCapture
            initVideoCapture()
            // 确保没有任何内容绑定到 cameraProvider
            cameraProvider.unbindAll()
            // 因为是只录制模式,所以将 mVideoCapture 用例与现有 preview 绑定
            val camera =
                cameraProvider.bindToLifecycle(
                    (appCompatActivity as LifecycleOwner),
                    cameraSelector,
                    preview,
                    videoCapture
                )
            onCameraManageListener?.bindSucceed()
            cameraInfo = camera.cameraInfo
            cameraControl = camera.cameraControl
            initCameraPreviewListener()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 绑定 - 拍照+录制的模式
     */
    private fun bindCameraPreviewModeImageAndVideo() {
        try {
            // 获取适合的比例
            val screenAspectRatio: Int = aspectRatio(
                DisplayMetricsUtils.getScreenWidth(appCompatActivity),
                DisplayMetricsUtils.getScreenHeight(appCompatActivity)
            )
            // 获取当前预览的角度
            val rotation: Int = viewHolder.previewView.display.rotation
            // 获取前后置摄像头
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            // 初始化 Preview 对象，在其上调用 build，从取景器中获取 Surface 提供程序，然后在预览上进行设置。
            val preview = Preview.Builder().setTargetRotation(rotation).build().also {
                it.setSurfaceProvider(viewHolder.previewView.surfaceProvider)
            }
            // 初始化 ImageCapture
            initImageCapture(screenAspectRatio)
            // 初始化 VideoCapture
            initVideoCapture()
            val useCase = UseCaseGroup.Builder()
            useCase.addUseCase(preview)
            imageCapture?.let {
                useCase.addUseCase(it)
            }
            videoCapture?.let {
                useCase.addUseCase(it)
            }
            val useCaseGroup = useCase.build()
            // 确保没有任何内容绑定到 cameraProvider
            cameraProvider.unbindAll()
            // 将 imageCapture 用例与现有 preview 和 videoCapture 用例绑定(注意：不要绑定 imageAnalyzer，因为不支持 preview + imageCapture + videoCapture + imageAnalysis 组合)
            val camera =
                cameraProvider.bindToLifecycle((appCompatActivity as LifecycleOwner), cameraSelector, useCaseGroup)
            onCameraManageListener?.bindSucceed()
            cameraInfo = camera.cameraInfo
            cameraControl = camera.cameraControl
            initCameraPreviewListener()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 初始化ImageCapture
     *
     * @param screenAspectRatio 计算后适合的比例
     */
    private fun initImageCapture(screenAspectRatio: Int) {
        Log.d(TAG, "initImageCapture display.rotation:" + viewHolder.previewView.display.rotation)
        // 初始化 拍照类 imageCapture,设置 优先考虑延迟而不是图像质量、设置比例、设置角度
        imageCapture = Builder().setCaptureMode(CAPTURE_MODE_MINIMIZE_LATENCY).setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(viewHolder.previewView.display.rotation).build()
    }

    /**
     * 初始化VideoCapture
     */
    @SuppressLint("RestrictedApi")
    private fun initVideoCapture() {
        val videoBuilder = VideoCapture.Builder()
        videoBuilder.setTargetRotation(viewHolder.previewView.display.rotation)
        // 设置相关属性
        if (cameraSpec.videoFrameRate > 0) {
            videoBuilder.setVideoFrameRate(cameraSpec.videoFrameRate)
        }
        if (cameraSpec.videoBitRate > 0) {
            videoBuilder.setBitRate(cameraSpec.videoBitRate)
        }
        videoCapture = androidx.camera.video.VideoCapture.withOutput(Recorder.Builder().build())
    }

    /**
     * 通过计算来设置最合适的比例
     * 假设 宽度1080×高度1920
     * 1. 首先取最大值和最小值，取1920和1080
     * 2. 然后1920 / 1080 = 1.7
     * 3. 然后分别是规范的是1.333333比例和1.777778比例
     * 4. 接着用1.7-1.333333  和 1.7-1.777778 比较
     * 5. 上面哪个小，就设置哪个比例
     *
     * @param width 预览宽度
     * @param height 预览高度
     *
     * @return 返回合适的宽高比
     */
    private fun aspectRatio(width: Int, height: Int): Int {
        val aspect = max(width, height).toDouble()
        val previewRatio = aspect / min(width, height)
        return if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            AspectRatio.RATIO_4_3
        } else {
            AspectRatio.RATIO_16_9
        }
    }

    /**
     * 检测手机方向
     */
    private fun startCheckOrientation() {
        orientationEventListener.star()
    }

    /**
     * 停止检测手机方向
     */
    private fun stopCheckOrientation() {
        orientationEventListener.stop()
    }

    /**
     * 初始化预览的触摸事件
     */
    private fun initCameraPreviewListener() {
        val zoomState = cameraInfo.zoomState
        val onCameraXPreviewViewTouchListener = OnCameraXPreviewViewTouchListener(appCompatActivity)
        onCameraXPreviewViewTouchListener.setCustomTouchListener(object :
            OnCameraXPreviewViewTouchListener.CustomTouchListener {

            override fun zoom(delta: Float) {
                // 进行缩放
                zoomState.value?.let {
                    val currentZoomRatio = it.zoomRatio
                    cameraControl.setZoomRatio(currentZoomRatio * delta)
                }
            }

            override fun click(x: Float, y: Float) {
                // 控制对焦目标给xy坐标
                val meteringPointFactory: MeteringPointFactory = viewHolder.previewView.meteringPointFactory
                val point = meteringPointFactory.createPoint(x, y)
                val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                    .setAutoCancelDuration(3, TimeUnit.SECONDS).build()
                if (cameraInfo.isFocusMeteringSupported(action)) {
                    cameraControl.cancelFocusAndMetering()
                    viewHolder.focusView.setDisappear(false)
                    viewHolder.focusView.startFocusIng((Point(x.toInt(), y.toInt())))
                    val future = cameraControl.startFocusAndMetering(action)
                    future.addListener({
                        try {
                            val result = future.get()
                            viewHolder.focusView.setDisappear(true)
                            if (result.isFocusSuccessful) {
                                viewHolder.focusView.changeFocusSuccess()
                            } else {
                                viewHolder.focusView.changeFocusFailed()
                            }
                        } catch (ignored: java.lang.Exception) {
                        }
                    }, mainExecutor)
                }
            }

            override fun doubleClick(x: Float, y: Float) {
                // 双击控制缩放
                zoomState.value?.let {
                    val currentZoomRatio = it.zoomRatio
                    val minZoomRatio = it.minZoomRatio
                    // 如果当前比最小的缩放大
                    if (currentZoomRatio > minZoomRatio) {
                        // 重置
                        cameraControl.setLinearZoom(0f)
                    } else {
                        cameraControl.setLinearZoom(0.5f)
                    }
                }
            }
        })
        viewHolder.previewView.setOnTouchListener(onCameraXPreviewViewTouchListener)
    }

    /**
     * 拍照回调
     */
    private class TakePictureCallback(
        cameraManage: CameraManage, onCameraManageListener: OnCameraManageListener?
    ) : ImageCapture.OnImageSavedCallback {

        private val mCameraManageReference: WeakReference<CameraManage> = WeakReference<CameraManage>(cameraManage)
        private val mOnCameraManageListenerReference: WeakReference<OnCameraManageListener> =
            WeakReference<OnCameraManageListener>(onCameraManageListener)

        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            val cameraManage: CameraManage? = mCameraManageReference.get()
            val uri = outputFileResults.savedUri
            cameraManage?.stopCheckOrientation()
            val onCameraManageListenerReference: OnCameraManageListener? = mOnCameraManageListenerReference.get()
            onCameraManageListenerReference?.let {
                val path: String = if (MimeType.isContent(uri.toString())) uri.toString() else uri?.path.toString()
                onCameraManageListenerReference.onPictureSuccess(path)
            }
        }

        override fun onError(exception: ImageCaptureException) {
            Log.d(TAG, "onError")
            mOnCameraManageListenerReference.get()
                ?.onError(exception.imageCaptureError, exception.message, exception.cause)
        }

    }

    private fun getTargetRotation(): Int {
        imageCapture?.let {
            return it.targetRotation
        } ?: let {
            return 0
        }
    }

    /**
     * 使用显示器更改事件，更改图片拍摄器的旋转
     */
    internal inner class DisplayListener : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {}
        override fun onDisplayRemoved(displayId: Int) {}
        override fun onDisplayChanged(displayId: Int) {
            Log.d(TAG, "onDisplayChanged:$displayId and " + this@CameraManage.displayId)
            if (displayId == this@CameraManage.displayId && null != viewHolder.previewView.display) {
                imageCapture?.targetRotation = viewHolder.previewView.display.rotation
                imageAnalyzer?.targetRotation = viewHolder.previewView.display.rotation
            }
        }
    }

    /**
     * 监控手机角度事件触发
     */
    override fun onOrientationChanged(orientation: Int) {
        Log.d(TAG, "rotation:$orientation")
        imageCapture?.targetRotation = orientation
        imageAnalyzer?.targetRotation = orientation
    }

}