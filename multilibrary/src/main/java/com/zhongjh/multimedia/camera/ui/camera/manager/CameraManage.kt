package com.zhongjh.multimedia.camera.ui.camera.manager

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.PorterDuff
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Rational
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraEffect.IMAGE_CAPTURE
import androidx.camera.core.CameraEffect.PREVIEW
import androidx.camera.core.CameraEffect.VIDEO_CAPTURE
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
import androidx.camera.core.ViewPort
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.effects.OverlayEffect
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.zhongjh.common.enums.MediaType
import com.zhongjh.common.enums.MimeType
import com.zhongjh.common.utils.DisplayMetricsUtils
import com.zhongjh.common.utils.UriUtils
import com.zhongjh.multimedia.camera.entity.OverlayEffectEntity
import com.zhongjh.multimedia.camera.listener.OnCameraManageListener
import com.zhongjh.multimedia.camera.listener.OnCameraXOrientationEventListener
import com.zhongjh.multimedia.camera.listener.OnCameraXPreviewViewTouchListener
import com.zhongjh.multimedia.camera.widget.FocusView
import com.zhongjh.multimedia.settings.CameraSpec
import com.zhongjh.multimedia.utils.FileMediaUtil
import com.zhongjh.multimedia.utils.MediaStoreUtils.DCIM_CAMERA
import java.io.File
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


/**
 * 处理camerax的类
 */
class CameraManage(appCompatActivity: AppCompatActivity, val previewView: PreviewView, val focusView: FocusView) : OnCameraXOrientationEventListener.OnOrientationChangedListener {

    companion object {
        /**
         * 两个宽高比例
         */
        const val RATIO_4_3_VALUE = 4.0 / 3.0
        const val RATIO_16_9_VALUE = 16.0 / 9.0
        const val TAG = "CameraManage"
    }

    /**
     * 使用弱引用持有Activity
     */
    private val activityRef: WeakReference<AppCompatActivity> = WeakReference(appCompatActivity)

    /**
     * 该接口不用弱引用,时候记得销毁
     */
    private var listener: OnCameraManageListener? = null

    /**
     * 拍摄配置
     */
    private val cameraSpec by lazy { CameraSpec }
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var overlayEffect: OverlayEffect? = null
    private var recording: Recording? = null

    private val displayManager by lazy { activityRef.get()?.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager }
    private val displayListener by lazy { DisplayListener() }
    private val orientationEventListener: OnCameraXOrientationEventListener? by lazy {
        activityRef.get()?.let {
            OnCameraXOrientationEventListener(it, this)
        }
    }
    private lateinit var cameraInfo: CameraInfo

    /**
     * 摄像头控制器
     */
    private lateinit var cameraControl: CameraControl
    private lateinit var mainExecutor: Executor

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
     * 水印实体类
     */
    private val overlayEffectEntity = OverlayEffectEntity()

    /**
     * ui初始化，必须是ui线程
     */
    fun init() {
        displayManager.registerDisplayListener(displayListener, null)
        previewView.post {
            displayId = previewView.display.displayId
        }
        startCheckOrientation()

        // 选择数据改变
        activityRef.get()?.let { activity ->
            mainExecutor = ContextCompat.getMainExecutor(activity)
            previewView.previewStreamState.observe(activity) { streamState ->
                if (lastStreamState == null) {
                    lastStreamState = streamState
                }
                when (streamState) {
                    PreviewView.StreamState.IDLE -> {
                        if (lastStreamState != streamState) {
                            lastStreamState = streamState
                            // 停止输出画面后仍会停留在最后一帧,设置黑色前景遮挡住最后一帧画面
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                previewView.foreground = ContextCompat.getDrawable(activity, android.R.color.background_dark)
                            }
                        }
                    }

                    PreviewView.StreamState.STREAMING -> {
                        if (lastStreamState != streamState) {
                            lastStreamState = streamState
                            // 开始输出画面后清空前景
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                previewView.foreground = null
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    /**
     * onDestroy
     */
    fun onDestroy() {
        // 解除 CameraX 用例绑定
        cameraProvider?.unbindAll()
        displayManager.unregisterDisplayListener(displayListener)
        recording?.close()
        stopCheckOrientation()
        this.listener = null
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
        cameraProvider?.unbindAll()
        // 停止录制
        isActivityPause = true
        // Activity触发了Pause,通知视频录制重置View
        listener?.onActivityPause()
        stopVideo()
    }

    // 设置回调监听
    fun setOnCameraManageListener(listener: OnCameraManageListener?) {
        this.listener = listener
    }

    /**
     * 拍照
     */
    fun takePictures() {
        val imageCapture = imageCapture ?: return
        // 判断是否绑定了mImageCapture
        cameraProvider?.let {
            if (!it.isBound(imageCapture)) {
                initCameraPreviewMode()
            }
        }
        // 设置图片模式
        useCameraCases = LifecycleCameraController.IMAGE_CAPTURE
        // 该设置解决 前置摄像头左右镜像 问题
        val isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
        val metadata = Metadata()
        metadata.isReversedHorizontal = isReversedHorizontal
        // 设置输出路径,因为有可能多图的原因,所以先暂时全部放进cache文件夹里面
        activityRef.get()?.let { activity ->
            val cameraFile = FileMediaUtil.createCacheFile(activity, MediaType.TYPE_PICTURE)
            val fileOptions = OutputFileOptions.Builder(cameraFile).setMetadata(metadata).build()
            // 进行拍照
            imageCapture.takePicture(fileOptions, mainExecutor, TakePictureCallback(this.listener))
        }
    }

    /**
     * 录制
     */
    @SuppressLint("MissingPermission")
    fun takeVideo() {
        val activity = activityRef.get() ?: return
        // recording 如果为空则重新创建
        recording?.resume() ?: run {
            // 区分 Android 版本，使用兼容的文件生成逻辑
            val mediaStoreOutput = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ 保留原逻辑（使用 RELATIVE_PATH）
                val name = "VIDEO_" + SimpleDateFormat(
                    "yyyyMMdd_HHmmssSSS", Locale.US
                ).format(System.currentTimeMillis()) + ".mp4"
                val contentValues = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, name)
                    put(MediaStore.Video.Media.RELATIVE_PATH, DCIM_CAMERA)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4") // 补充 MIME 类型（高版本也建议加）
                }
                MediaStoreOutputOptions.Builder(
                    activity.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                ).setContentValues(contentValues).build()
            } else {
                // Android 6.0 适配逻辑（API 23）
                // 1. 生成绝对路径（使用 DCIM/Camera 公共目录）
                val dcimCameraDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                    "Camera" // 对应 DCIM_CAMERA 路径
                )
                if (!dcimCameraDir.exists()) {
                    dcimCameraDir.mkdirs() // 确保目录存在
                }
                // 2. 生成唯一文件名
                val name = "VIDEO_" + SimpleDateFormat(
                    "yyyyMMdd_HHmmssSSS", Locale.US
                ).format(System.currentTimeMillis()) + ".mp4"
                val videoFile = File(dcimCameraDir, name)
                // 3. 构建 ContentValues（使用绝对路径 DATA 字段）
                val contentValues = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, name)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4") // 必选：指定视频类型
                    put(MediaStore.Video.Media.DATA, videoFile.absolutePath) // 必选：Android 6.0 依赖此路径
                }
                // 4. 构建 MediaStoreOutputOptions（传入文件 URI）
                MediaStoreOutputOptions.Builder(
                    activity.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                ).setContentValues(contentValues)
                    .build()
            }
            val pendingRecording = videoCapture?.output?.prepareRecording(activity, mediaStoreOutput)
            if (isAudio) {
                pendingRecording?.withAudioEnabled()
            }
            recording = pendingRecording?.start(ContextCompat.getMainExecutor(activity)) { videoRecordEvent ->
                // 视频录制监控回调
                when (videoRecordEvent) {
                    is VideoRecordEvent.Start -> {
                        this.listener?.onRecordStart()
                    }

                    is VideoRecordEvent.Status -> {
//                        // 录制时间大于0才代表真正开始,通知长按按钮开始动画
//                        if (videoRecordEvent.recordingStats.recordedDurationNanos > 0) {
//                        }
                    }

                    is VideoRecordEvent.Finalize -> {
                        if (!isActivityPause) {
                            // 完成录制
                            val uri = videoRecordEvent.outputResults.outputUri
                            UriUtils.uriToFile(activity, uri)?.absolutePath?.let { this.listener?.onRecordSuccess(it, uri.toString()) }
                        }
                        isActivityPause = false
                    }

                    is VideoRecordEvent.Pause -> {
                        // 暂停录制
                        this.listener?.onRecordPause(videoRecordEvent.recordingStats.recordedDurationNanos)
                    }

                    is VideoRecordEvent.Resume -> {
                        // 恢复录制
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
        activityRef.get()?.let { activity ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)
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
    }

    /**
     * 绑定相机预览模式
     */
    private fun initCameraPreviewMode() {
        if (cameraSpec.onlySupportImages()) {
            // 只支持拍照
            bindCameraPreviewModeByImage()
        } else if (cameraSpec.onlySupportVideos()) {
            // 只支持录制
            bindCameraPreviewModeByVideo()
        } else {
            // 拍照+录制
            bindCameraPreviewModeImageAndVideo()
        }
    }

    /**
     * 绑定 - 只拍照模式
     */
    private fun bindCameraPreviewModeByImage() {
        val activity = activityRef.get() ?: return
        val cameraProvider = cameraProvider ?: return
        try {
            // 获取适合的比例
            val screenAspectRatio: Int = aspectRatio(DisplayMetricsUtils.getScreenWidth(activity), DisplayMetricsUtils.getScreenHeight(activity))
            // 获取前后置摄像头
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            // 初始化 Preview 对象，在其上调用 build，从取景器中获取 Surface 提供程序，然后在预览上进行设置。
            val preview = initPreview(screenAspectRatio)
            // 初始化 ImageCapture
            initImageCapture(screenAspectRatio)
            // 初始化 ImageAnalysis
            initImageAnalyzer(screenAspectRatio)
            // 初始化OverlayEffect
            initOverlayEffect()
            // 初始化ViewPort
            val viewPort = initViewPort()
            val useCase = UseCaseGroup.Builder().setViewPort(viewPort)
            // 所有功能添加组合
            useCase.addUseCase(preview)
            imageCapture?.let {
                useCase.addUseCase(it)
            }
            imageAnalyzer?.let {
                useCase.addUseCase(it)
            }
            overlayEffect?.let {
                useCase.addEffect(it)
            }
            val useCaseGroup = useCase.build()
            // 确保没有任何内容绑定到 cameraProvider
            cameraProvider.unbindAll()
            // 绑定preview
            preview.surfaceProvider = previewView.surfaceProvider
            // 因为是只拍照模式,所以将 mImageCapture 用例与现有 preview 和 mImageAnalyzer 用例绑定
            val camera = cameraProvider.bindToLifecycle((activity as LifecycleOwner), cameraSelector, useCaseGroup)
            this.listener?.bindSucceed()
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
        val activity = activityRef.get() ?: return
        val cameraProvider = cameraProvider ?: return
        try {
            // 获取适合的比例
            val screenAspectRatio: Int = aspectRatio(DisplayMetricsUtils.getScreenWidth(activity), DisplayMetricsUtils.getScreenHeight(activity))
            // 获取前后置摄像头
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            // 初始化 Preview 对象，在其上调用 build，从取景器中获取 Surface 提供程序，然后在预览上进行设置。
            val preview = initPreview(screenAspectRatio)
            // 初始化 VideoCapture
            initVideoCapture(screenAspectRatio)
            // 初始化OverlayEffect
            initOverlayEffect()
            // 初始化ViewPort
            val viewPort = initViewPort()
            // 所有功能添加组合
            val useCase = UseCaseGroup.Builder().setViewPort(viewPort)
            useCase.addUseCase(preview)
            videoCapture?.let {
                useCase.addUseCase(it)
            }
            overlayEffect?.let {
                useCase.addEffect(it)
            }
            val useCaseGroup = useCase.build()
            // 确保没有任何内容绑定到 cameraProvider
            cameraProvider.unbindAll()
            // 因为是只录制模式,所以将 mVideoCapture 用例与现有 preview 绑定
            val camera = cameraProvider.bindToLifecycle((activity as LifecycleOwner), cameraSelector, useCaseGroup)
            this.listener?.bindSucceed()
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
        val activity = activityRef.get() ?: return
        val cameraProvider = cameraProvider ?: return
        try {
            // 获取适合的比例
            val screenAspectRatio: Int = aspectRatio(DisplayMetricsUtils.getScreenWidth(activity), DisplayMetricsUtils.getScreenHeight(activity))
            // 获取前后置摄像头
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            // 初始化 Preview 对象，在其上调用 build，从取景器中获取 Surface 提供程序，然后在预览上进行设置。
            val preview = initPreview(screenAspectRatio)
            // 初始化 ImageCapture
            initImageCapture(screenAspectRatio)
            // 初始化 VideoCapture
            initVideoCapture(screenAspectRatio)
            // 初始化OverlayEffect
            initOverlayEffect()
            // 初始化ViewPort
            val viewPort = initViewPort()
            // 所有功能添加组合
            val useCase = UseCaseGroup.Builder().setViewPort(viewPort)
            useCase.addUseCase(preview)
            imageCapture?.let {
                useCase.addUseCase(it)
            }
            videoCapture?.let {
                useCase.addUseCase(it)
            }
            overlayEffect?.let {
                useCase.addEffect(it)
            }
            val useCaseGroup = useCase.build()
            // 确保没有任何内容绑定到 cameraProvider
            cameraProvider.unbindAll()
            // 将 imageCapture 用例与现有 preview 和 videoCapture 用例绑定(注意：不要绑定 imageAnalyzer，因为不支持 preview + imageCapture + videoCapture + imageAnalysis 组合)
            val camera = cameraProvider.bindToLifecycle((activity as LifecycleOwner), cameraSelector, useCaseGroup)
            this.listener?.bindSucceed()
            cameraInfo = camera.cameraInfo
            cameraControl = camera.cameraControl
            initCameraPreviewListener()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 初始化Preview
     *
     * @param screenAspectRatio 计算后适合的比例
     */
    private fun initPreview(screenAspectRatio: Int): Preview {
        val previewBuilder = Preview.Builder().setResolutionSelector(
            ResolutionSelector.Builder().setAspectRatioStrategy(AspectRatioStrategy(screenAspectRatio, AspectRatioStrategy.FALLBACK_RULE_AUTO))
                .setResolutionStrategy(ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY).build()
        ).setTargetRotation(previewView.display.rotation)
        cameraSpec.onInitCameraManager?.initPreview(previewBuilder, screenAspectRatio, previewView.display.rotation)
        return previewBuilder.build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }
    }

    /**
     * 初始化ImageCapture
     * 设置分辨率： .setResolutionStrategy(ResolutionStrategy(Size(1920, 1080), ResolutionStrategy.FALLBACK_RULE_NONE))
     *
     * @param screenAspectRatio 计算后适合的比例
     */
    private fun initImageCapture(screenAspectRatio: Int) {
        // 初始化 拍照类 imageCapture
        val imageBuilder = Builder().setCaptureMode(CAPTURE_MODE_MINIMIZE_LATENCY).setResolutionSelector(
            ResolutionSelector.Builder().setAspectRatioStrategy(AspectRatioStrategy(screenAspectRatio, AspectRatioStrategy.FALLBACK_RULE_AUTO))
                .setResolutionStrategy(ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY).build()
        ).setTargetRotation(previewView.display.rotation)
        cameraSpec.onInitCameraManager?.initImageCapture(imageBuilder, screenAspectRatio, previewView.display.rotation)
        imageCapture = imageBuilder.build()
    }

    /**
     * 初始化ImageAnalyzer
     *
     * @param screenAspectRatio 计算后适合的比例
     */
    private fun initImageAnalyzer(screenAspectRatio: Int) {
        // 初始化 拍照类 imageCapture,设置 优先考虑延迟而不是图像质量、设置比例、设置角度
        val imageAnalyzerBuilder = ImageAnalysis.Builder().setResolutionSelector(
            ResolutionSelector.Builder().setAspectRatioStrategy(AspectRatioStrategy(screenAspectRatio, AspectRatioStrategy.FALLBACK_RULE_AUTO))
                .setResolutionStrategy(ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY).build()
        ).setTargetRotation(previewView.display.rotation)
        cameraSpec.onInitCameraManager?.initImageAnalyzer(imageAnalyzerBuilder, screenAspectRatio, previewView.display.rotation)
        imageAnalyzer = imageAnalyzerBuilder.build()
    }

    /**
     * 初始化VideoCapture
     * @param screenAspectRatio 计算后适合的比例
     */
    private fun initVideoCapture(screenAspectRatio: Int) {
        // 根据设备性能动态选择质量（降低分辨率减少抖动）
        val qualitySelector = if (isLowPerformanceDevice()) {
            // 720p
            QualitySelector.from(Quality.SD)
        } else {
            // 1080p
            QualitySelector.from(Quality.HD)
        }
        val recorder = Recorder.Builder().setAspectRatio(screenAspectRatio).setQualitySelector(qualitySelector)
        cameraSpec.onInitCameraManager?.initVideoRecorder(recorder, screenAspectRatio)
        val videoCaptureBuilder = VideoCapture.Builder<Recorder>(recorder.build()).setTargetRotation(previewView.display.rotation)
        cameraSpec.onInitCameraManager?.initVideoCapture(videoCaptureBuilder, previewView.display.rotation)
        videoCapture = videoCaptureBuilder.build()
    }

    // 添加设备性能检测方法
    private fun isLowPerformanceDevice(): Boolean {
        // 可根据设备CPU核心数、内存等判断，这里简化处理
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O || Runtime.getRuntime().availableProcessors() <= 4
    }

    /**
     * 初始化OverlayEffect 叠加效果,一般用于水印,实时画面
     */
    private fun initOverlayEffect() {
        // 优先使用自定义叠加效果
        overlayEffect = cameraSpec.onInitCameraManager?.initOverlayEffect(previewView)
        if (overlayEffect != null) {
            return
        }
        if (cameraSpec.onInitCameraManager?.isDefaultOverlayEffect() == true) {
            overlayEffectEntity.cachedDrawX = previewView.width - overlayEffectEntity.textWidth
            overlayEffectEntity.cachedDrawY = previewView.height - overlayEffectEntity.marginSize - overlayEffectEntity.fixedTextHeight
            // ========== 1. 创建叠加效果 ==========
            overlayEffect = OverlayEffect(PREVIEW or VIDEO_CAPTURE or IMAGE_CAPTURE, 0, Handler(Looper.getMainLooper())) {

            }.apply {
                clearOnDrawListener()

                setOnDrawListener { frame ->
                    // ========== 2. 矩阵变换优化（低版本兼容） ==========
                    val sensorToUi = previewView.sensorToViewTransform
                    if (sensorToUi != null) {
                        // 高效矩阵比较（避免使用equals方法）
                        overlayEffectEntity.tempMatrix.set(overlayEffectEntity.cachedSensorToUi)
                        if (overlayEffectEntity.tempMatrix != sensorToUi) {
                            overlayEffectEntity.cachedSensorToUi.set(sensorToUi)
                            // 重新计算逆矩阵（复用对象）
                            overlayEffectEntity.cachedUiToSensor.reset()
                            if (overlayEffectEntity.cachedSensorToUi.invert(overlayEffectEntity.cachedUiToSensor)) {
                                overlayEffectEntity.cachedUiToSensor.postConcat(frame.sensorToBufferTransform)
                            }
                        }
                    }

                    // ========== 3. 时间格式化优化（每秒更新一次） ==========
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - overlayEffectEntity.lastTimeUpdateTime >= 1000) {
                        overlayEffectEntity.date.time = currentTime // 复用Date对象
                        overlayEffectEntity.cachedTimeText = overlayEffectEntity.dateFormat.format(overlayEffectEntity.date)
                        overlayEffectEntity.lastTimeUpdateTime = currentTime

                        // ========== 4. 绘制优化（减少画布操作） ==========
                        overlayEffectEntity.cachedUiToSensor.takeIf { it.isIdentity.not() }?.let { uiToSensor ->
                            val canvas = frame.overlayCanvas

                            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                            canvas.setMatrix(uiToSensor)

                            canvas.drawText(overlayEffectEntity.cachedTimeText, overlayEffectEntity.cachedDrawX, overlayEffectEntity.cachedDrawY, overlayEffectEntity.textPaint)
                        }
                    }
                    true
                }
            }
        }
    }

    /**
     * 初始化 ViewPort，作用是让预览大小与拍摄的图像相同
     */
    private fun initViewPort(): ViewPort {
        return ViewPort.Builder(
            Rational(previewView.width, previewView.height), previewView.display.rotation
        ).setScaleType(ViewPort.FILL_CENTER).build()
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
        orientationEventListener?.star()
    }

    /**
     * 停止检测手机方向
     */
    private fun stopCheckOrientation() {
        orientationEventListener?.stop()
    }

    /**
     * 初始化预览的触摸事件
     */
    private fun initCameraPreviewListener() {
        activityRef.get()?.let { activity ->
            val zoomState = cameraInfo.zoomState
            val onCameraXPreviewViewTouchListener = OnCameraXPreviewViewTouchListener(activity)
            onCameraXPreviewViewTouchListener.setCustomTouchListener(object : OnCameraXPreviewViewTouchListener.CustomTouchListener {

                override fun zoom(delta: Float) {
                    // 进行缩放
                    zoomState.value?.let {
                        val currentZoomRatio = it.zoomRatio
                        cameraControl.setZoomRatio(currentZoomRatio * delta)
                    }
                }

                override fun click(x: Float, y: Float) {
                    // 控制对焦目标给xy坐标
                    val meteringPointFactory: MeteringPointFactory = previewView.meteringPointFactory
                    val point = meteringPointFactory.createPoint(x, y)
                    val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF).setAutoCancelDuration(3, TimeUnit.SECONDS).build()
                    if (cameraInfo.isFocusMeteringSupported(action)) {
                        cameraControl.cancelFocusAndMetering()
                        focusView.setDisappear(false)
                        focusView.startFocusIng((Point(x.toInt(), y.toInt())))
                        val future = cameraControl.startFocusAndMetering(action)
                        future.addListener({
                            try {
                                val result = future.get()
                                focusView.setDisappear(true)
                                if (result.isFocusSuccessful) {
                                    focusView.changeFocusSuccess()
                                } else {
                                    focusView.changeFocusFailed()
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
            previewView.setOnTouchListener(onCameraXPreviewViewTouchListener)

        }

    }

    /**
     * 拍照回调
     */
    private class TakePictureCallback(onCameraManageListener: OnCameraManageListener?) : ImageCapture.OnImageSavedCallback {

        private val mOnCameraManageListenerReference: WeakReference<OnCameraManageListener> = WeakReference<OnCameraManageListener>(onCameraManageListener)

        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            val uri = outputFileResults.savedUri
            val onCameraManageListenerReference: OnCameraManageListener? = mOnCameraManageListenerReference.get()
            uri ?: return
            onCameraManageListenerReference?.let {
                val path: String = if (MimeType.isContent(uri.toString())) uri.toString() else uri.path.toString()
                onCameraManageListenerReference.onPictureSuccess(uri, path)
            }
        }

        override fun onError(exception: ImageCaptureException) {
            mOnCameraManageListenerReference.get()?.onError(exception.imageCaptureError, exception.message, exception.cause)
        }

    }

    /**
     * 使用显示器更改事件，更改图片拍摄器的旋转
     */
    internal inner class DisplayListener : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {}
        override fun onDisplayRemoved(displayId: Int) {}
        override fun onDisplayChanged(displayId: Int) {
            if (displayId == this@CameraManage.displayId && null != previewView.display) {
                imageCapture?.targetRotation = previewView.display.rotation
                imageAnalyzer?.targetRotation = previewView.display.rotation
                videoCapture?.targetRotation = previewView.display.rotation
            }
        }
    }

    /**
     * 监控手机角度事件触发
     *
     * @param orientation 默认底部是0，以左边为底部是1，以右边为底部是3
     */
    override fun onOrientationChanged(orientation: Int) {
        imageCapture?.targetRotation = orientation
        imageAnalyzer?.targetRotation = orientation
        videoCapture?.targetRotation = orientation
    }

}