package com.zhongjh.albumcamerarecorder.camera.ui.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.video.OnVideoSavedCallback
import androidx.camera.view.video.OutputFileResults
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.zhongjh.albumcamerarecorder.camera.constants.CameraTypes.TYPE_VIDEO
import com.zhongjh.albumcamerarecorder.camera.listener.OnCameraManageListener
import com.zhongjh.albumcamerarecorder.camera.listener.OnCameraXOrientationEventListener
import com.zhongjh.albumcamerarecorder.camera.listener.OnCameraXPreviewViewTouchListener
import com.zhongjh.albumcamerarecorder.camera.ui.camera.CameraFragment.ViewHolder
import com.zhongjh.albumcamerarecorder.camera.ui.camera.impl.ICameraView
import com.zhongjh.albumcamerarecorder.constants.Constant.ALBUM_CAMERA_RECORDER
import com.zhongjh.albumcamerarecorder.constants.Constant.JPEG
import com.zhongjh.albumcamerarecorder.constants.Constant.MP4
import com.zhongjh.albumcamerarecorder.settings.CameraSpec
import com.zhongjh.common.utils.BitmapUtils.toBitmap
import com.zhongjh.common.utils.DisplayMetricsUtils
import java.io.File
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


/**
 * 拍摄/录制 管理
 */
class CameraManage(
    val mContext: Context,
    val mViewHolder: ViewHolder,
    val mICameraView: ICameraView
) :
    OnCameraXOrientationEventListener.OnOrientationChangedListener {

    companion object {
        /**
         * 两个宽高比例
         */
        const val RATIO_4_3_VALUE = 4.0 / 3.0
        const val RATIO_16_9_VALUE = 16.0 / 9.0
    }


    /**
     * 回调监听
     */
    var onCameraManageListener: OnCameraManageListener? = null

    /**
     * 拍摄配置
     */
    private val mCameraSpec by lazy { CameraSpec }
    private lateinit var mCameraProvider: ProcessCameraProvider
    private lateinit var mImageCapture: ImageCapture
    private lateinit var mImageAnalyzer: ImageAnalysis
    private lateinit var mVideoCapture: androidx.camera.video.VideoCapture<Recorder>

    private val mDisplayManager by lazy { mContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager }
    private val mDisplayListener by lazy { DisplayListener() }
    private val mOrientationEventListener by lazy {
        OnCameraXOrientationEventListener(
            mContext,
            this
        )
    }
    private lateinit var mCameraInfo: CameraInfo

    /**
     * 摄像头控制器
     */
    private lateinit var mCameraControl: CameraControl
    private val mainExecutor: Executor by lazy { ContextCompat.getMainExecutor(mContext) }

    private var displayId = -1

    /**
     * 相机模式
     */
    private var mUseCameraCases = LifecycleCameraController.IMAGE_CAPTURE
    private var typeFlash = FLASH_MODE_OFF

    /**
     * 摄像头方向
     */
    private var lensFacing = CameraSelector.LENS_FACING_BACK

    /**
     * 创建
     */
    fun create() {
        mDisplayManager.registerDisplayListener(mDisplayListener, null)
        mViewHolder.previewView.post { displayId = mViewHolder.previewView.display.displayId }
    }

    /**
     * ui初始化，必须是ui线程
     */
    fun init() {
        startCamera()
    }

    /**
     * onDestroy
     */
    fun onDestroy() {
        mDisplayManager.unregisterDisplayListener(mDisplayListener)
        stopCheckOrientation()
    }

    fun onClose() {

    }

    fun stopVideo() {

    }

    fun isOpened(): Boolean {
        return false
    }

    fun open() {

    }

    /**
     * 拍照
     */
    fun takePictures() {
        // 判断是否绑定了mImageCapture
        if (!mCameraProvider.isBound(mImageCapture)) {
            bindCameraPreviewModeByImage()
        }
        // 设置图片模式
        mUseCameraCases = LifecycleCameraController.IMAGE_CAPTURE
        // 该设置解决 前置摄像头左右镜像 问题
        val isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
        val metadata = Metadata()
        metadata.isReversedHorizontal = isReversedHorizontal
        // 进行拍照
        mImageCapture.takePicture(
            mainExecutor,
            TakePictureCallback(this@CameraManage, onCameraManageListener)
        )
    }

    /**
     * 录制
     */
    @SuppressLint("UnsafeOptInUsageError", "MissingPermission", "RestrictedApi")
    fun takeVideo() {
        // 判断是否绑定了mVideoCapture
        if (!mCameraProvider.isBound(mVideoCapture)) {
            bindCameraPreviewModeByVideo()
        }
        // 设置视频模式
        mUseCameraCases = LifecycleCameraController.VIDEO_CAPTURE
        // 设置输出路径
        val cameraFile: File? = if (isSaveExternal()) {
            // 创建内部文件夹
            createTempFile(false)
        } else {
            // 创建自定义路径下的文件夹
            createOutFile(
                mContext,
                TYPE_VIDEO,
                mCameraSpec.outPutCameraFileName,
                mCameraSpec.videoFormat,
                mCameraSpec.outPutCameraDir
            )
        }
        cameraFile?.let {
            val fileOptions = OutputFileOptions.Builder(cameraFile).build()
            // TODO
//            mVideoCapture.startRecording(fileOptions, mainExecutor, TakeVideoCallback(this@CameraManage, onCameraManageListener))
//            mVideoCapture.startRecording(fileOptions,mainExecutor,TakeVideoCallback)
        }
    }

    /**
     * 设置闪光灯
     */
    fun setFlashLamp() {
        typeFlash++
        if (typeFlash > FLASH_MODE_OFF) {
            typeFlash = FLASH_MODE_AUTO
        }
        setFlashMode()
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
        val cameraProviderFuture = ProcessCameraProvider.getInstance(mContext)
        cameraProviderFuture.addListener({
            try {
                // 在 Runnable 中，添加 ProcessCameraProvider。它用于将相机的生命周期绑定到应用进程中的 LifecycleOwner
                mCameraProvider = cameraProviderFuture.get()
                initCameraPreviewMode()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, mainExecutor)
    }

    /**
     * 闪光灯模式
     */
    private fun setFlashMode() {
        mImageCapture.flashMode = typeFlash
    }

    /**
     * 绑定相机预览模式
     */
    private fun initCameraPreviewMode() {
        // 如果有设置高清模式，则根据相应高清模式更改模式
        if (mCameraSpec.enableImageHighDefinition) {
            bindCameraPreviewModeByImage()
        } else if (mCameraSpec.enableVideoHighDefinition) {
            bindCameraPreviewModeByVideo()
        } else {
            // 最后再判断具体什么模式
            if (mCameraSpec.onlySupportImages()) {
                bindCameraPreviewModeByImage()
            } else if (mCameraSpec.onlySupportVideos()) {
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
            val screenAspectRatio: Int = aspectRatio(
                DisplayMetricsUtils.getScreenWidth(mContext),
                DisplayMetricsUtils.getScreenHeight(mContext)
            )
            // 获取当前预览的角度
            val rotation: Int = mViewHolder.previewView.display.rotation
            // 获取前后置摄像头
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            // 初始化 Preview 对象，在其上调用 build，从取景器中获取 Surface 提供程序，然后在预览上进行设置。
            val preview = Preview.Builder().setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation).build().also {
                    it.setSurfaceProvider(mViewHolder.previewView.surfaceProvider)
                }

            // 初始化 ImageCapture
            initImageCapture(screenAspectRatio)

            // 初始化 ImageAnalysis
            mImageAnalyzer = ImageAnalysis.Builder().setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation).build()

            // 确保没有任何内容绑定到 cameraProvider
            mCameraProvider.unbindAll()
            // 因为是只拍照模式,所以将 mImageCapture 用例与现有 preview 和 mImageAnalyzer 用例绑定
            val camera = mCameraProvider.bindToLifecycle(
                (mContext as LifecycleOwner),
                cameraSelector,
                preview,
                mImageCapture,
                mImageAnalyzer
            )
            // setFlashMode
            setFlashMode()
            mCameraInfo = camera.cameraInfo
            mCameraControl = camera.cameraControl
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
            val rotation: Int = mViewHolder.previewView.display.rotation
            // 获取前后置摄像头
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            // 初始化 Preview 对象，在其上调用 build，从取景器中获取 Surface 提供程序，然后在预览上进行设置。
            val preview = Preview.Builder().setTargetRotation(rotation).build().also {
                it.setSurfaceProvider(mViewHolder.previewView.surfaceProvider)
            }
            // 初始化 VideoCapture
            initVideoCapture()
            // 确保没有任何内容绑定到 cameraProvider
            mCameraProvider.unbindAll()
            // 因为是只录制模式,所以将 mVideoCapture 用例与现有 preview 绑定
            val camera = mCameraProvider.bindToLifecycle(
                (mContext as LifecycleOwner),
                cameraSelector,
                preview,
                mVideoCapture
            )
            mCameraInfo = camera.cameraInfo
            mCameraControl = camera.cameraControl
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
                DisplayMetricsUtils.getScreenWidth(mContext),
                DisplayMetricsUtils.getScreenHeight(mContext)
            )
            // 获取当前预览的角度
            val rotation: Int = mViewHolder.previewView.display.rotation
            // 获取前后置摄像头
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            // 初始化 Preview 对象，在其上调用 build，从取景器中获取 Surface 提供程序，然后在预览上进行设置。
            val preview = Preview.Builder().setTargetRotation(rotation).build().also {
                it.setSurfaceProvider(mViewHolder.previewView.surfaceProvider)
            }
            // 初始化 ImageCapture
            initImageCapture(screenAspectRatio)
            // 初始化 VideoCapture
            initVideoCapture()
            val useCase = UseCaseGroup.Builder()
            useCase.addUseCase(preview)
            useCase.addUseCase(mImageCapture)
            useCase.addUseCase(mVideoCapture)
            val useCaseGroup = useCase.build()
            // 确保没有任何内容绑定到 cameraProvider
            mCameraProvider.unbindAll()
            // 将 imageCapture 用例与现有 preview 和 videoCapture 用例绑定(注意：不要绑定 imageAnalyzer，因为不支持 preview + imageCapture + videoCapture + imageAnalysis 组合)
            val camera = mCameraProvider.bindToLifecycle(
                (mContext as LifecycleOwner),
                cameraSelector,
                useCaseGroup
            )
            // setFlashMode
            setFlashMode()
            mCameraInfo = camera.cameraInfo
            mCameraControl = camera.cameraControl
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
        // 初始化 拍照类 imageCapture,设置 优先考虑延迟而不是图像质量、设置比例、设置角度
        mImageCapture =
            ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(mViewHolder.previewView.display.rotation).build()
    }

    /**
     * 初始化VideoCapture
     */
    @SuppressLint("RestrictedApi")
    private fun initVideoCapture() {
        val videoBuilder = VideoCapture.Builder()
        videoBuilder.setTargetRotation(mViewHolder.previewView.display.rotation)
        // 设置相关属性
        if (mCameraSpec.videoFrameRate > 0) {
            videoBuilder.setVideoFrameRate(mCameraSpec.videoFrameRate)
        }
        if (mCameraSpec.videoBitRate > 0) {
            videoBuilder.setBitRate(mCameraSpec.videoBitRate)
        }
        mVideoCapture = androidx.camera.video.VideoCapture.withOutput(Recorder.Builder().build())
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
        mOrientationEventListener.star()
    }

    /**
     * 停止检测手机方向
     */
    private fun stopCheckOrientation() {
        mOrientationEventListener.stop()
    }

    /**
     * 初始化预览的触摸事件
     */
    private fun initCameraPreviewListener() {
        val zoomState = mCameraInfo.zoomState
        val onCameraXPreviewViewTouchListener = OnCameraXPreviewViewTouchListener(mContext)
        onCameraXPreviewViewTouchListener.setCustomTouchListener(object :
            OnCameraXPreviewViewTouchListener.CustomTouchListener {

            override fun zoom(delta: Float) {
                // 进行缩放
                zoomState.value?.let {
                    val currentZoomRatio = it.zoomRatio
                    mCameraControl.setZoomRatio(currentZoomRatio * delta)
                }
            }

            override fun click(x: Float, y: Float) {
                // 控制对焦目标给xy坐标
                val meteringPointFactory: MeteringPointFactory =
                    mViewHolder.previewView.meteringPointFactory
                val point = meteringPointFactory.createPoint(x, y)
                val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                    .setAutoCancelDuration(3, TimeUnit.SECONDS).build()
                if (mCameraInfo.isFocusMeteringSupported(action)) {
                    mCameraControl.cancelFocusAndMetering()
                    mViewHolder.focusView.setDisappear(false)
                    mViewHolder.focusView.startFocusIng((Point(x.toInt(), y.toInt())))
                    val future = mCameraControl.startFocusAndMetering(action)
                    future.addListener({
                        try {
                            val result = future.get()
                            mViewHolder.focusView.setDisappear(true)
                            if (result.isFocusSuccessful) {
                                mViewHolder.focusView.changeFocusSuccess()
                            } else {
                                mViewHolder.focusView.changeFocusFailed()
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
                        mCameraControl.setLinearZoom(0f)
                    } else {
                        mCameraControl.setLinearZoom(0.5f)
                    }
                }
            }
        })
        mViewHolder.previewView.setOnTouchListener(onCameraXPreviewViewTouchListener)
    }

    /**
     * 创建一个临时路径，主要是解决华为手机放弃拍照后会弹出相册图片被删除的提示
     *
     * @param isVideo
     * @return
     */
    private fun createTempFile(isVideo: Boolean): File? {
        val externalFilesDir: File? = mContext.getExternalFilesDir("")
        externalFilesDir?.let {
            val tempCameraFile = File(externalFilesDir.absolutePath, ".TempCamera")
            if (!tempCameraFile.exists()) {
                tempCameraFile.mkdirs()
            }
            val fileName: String = System.currentTimeMillis().toString() + if (isVideo) {
                MP4
            } else {
                JPEG
            }
            return File(tempCameraFile.absolutePath, fileName)
        }.let {
            return null
        }
    }

    /**
     * 判断是否外部输出路径
     */
    private fun isSaveExternal(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && TextUtils.isEmpty(mCameraSpec.outPutCameraDir)
    }

    /**
     * 创建文件
     *
     * @param context            上下文
     * @param fileType           文件类型：图片、视频
     * @param fileName           文件名
     * @param format             文件格式
     * @param outCameraDirectory 输出目录
     * @return 文件
     */
    private fun createOutFile(
        context: Context,
        fileType: Int,
        fileName: String?,
        format: String?,
        outCameraDirectory: String?
    ): File {
        val applicationContext = context.applicationContext
        var folderDir: File
        outCameraDirectory?.let {
            // 自定义存储路径
            folderDir = File(outCameraDirectory)
            folderDir.parentFile?.let {
                if (it.exists()) {
                    it.mkdirs()
                }
            }
        }.let {
            // 外部没有自定义拍照存储路径使用默认
            val rootDir: File
            // 判断SD卡是否正常挂载
            if (TextUtils.equals(
                    Environment.MEDIA_MOUNTED,
                    Environment.getExternalStorageState()
                )
            ) {
                rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                folderDir =
                    File(rootDir.absolutePath + File.separator + ALBUM_CAMERA_RECORDER + File.separator)
            } else {
                val rootDirFile = getRootDirFile(applicationContext, fileType)
                rootDirFile?.let {
                    folderDir = File(it.absolutePath + File.separator)
                }.let {
                    rootDir =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    folderDir =
                        File(rootDir.absolutePath + File.separator + ALBUM_CAMERA_RECORDER + File.separator)
                }
            }
            if (!rootDir.exists()) {
                rootDir.mkdirs()
            }
        }
        if (!folderDir.exists()) {
            folderDir.mkdirs()
        }
        return if (fileType == TYPE_VIDEO) {
            // 如果视频没有自定义文件名字，则使用默认的文件名字
            val newFileVideoName = fileName?.let {
                fileName
            }.let {
                getCreateFileName("VID_") + MP4
            }
            File(folderDir, newFileVideoName)
        } else {
            // 如果图片没有自定义文件后缀格式，则使用默认的JPEG
            val suffix = if (TextUtils.isEmpty(format)) {
                JPEG
            } else {
                format
            }
            // 如果图片没有自定义文件名字，则使用默认的文件名字
            val newFileImageName = fileName?.let {
                fileName
            }.let {
                getCreateFileName("IMG_") + suffix
            }
            File(folderDir, newFileImageName)
        }
    }

    /**
     * 文件根目录
     *
     * @param context 上下文
     * @param type 文件类型：图片、视频
     * @return 文件根目录
     */
    private fun getRootDirFile(context: Context, type: Int): File? {
        return if (type == TYPE_VIDEO) {
            context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        } else {
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        }
    }

    private val sf = SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US)

    /**
     * 根据时间戳创建文件名
     *
     * @param prefix 前缀名
     * @return
     */
    fun getCreateFileName(prefix: String): String {
        val millis = System.currentTimeMillis()
        return prefix + sf.format(millis)
    }

    /**
     * 拍照回调
     */
    private class TakePictureCallback(
        cameraManage: CameraManage, onCameraManageListener: OnCameraManageListener?
    ) : OnImageCapturedCallback() {

        private val mCameraManageReference: WeakReference<CameraManage>
        private val mOnCameraManageListenerReference: WeakReference<OnCameraManageListener>

        init {
            mCameraManageReference = WeakReference<CameraManage>(cameraManage)
            mOnCameraManageListenerReference =
                WeakReference<OnCameraManageListener>(onCameraManageListener)
        }

        @SuppressLint("UnsafeOptInUsageError")
        override fun onCaptureSuccess(image: ImageProxy) {
            super.onCaptureSuccess(image)
            val cameraManage: CameraManage? = mCameraManageReference.get()
            cameraManage?.stopCheckOrientation()

            val onCameraManageListenerReference: OnCameraManageListener? =
                mOnCameraManageListenerReference.get()
            onCameraManageListenerReference?.let {
                image.image?.let {
                    onCameraManageListenerReference.onPictureSuccess(toBitmap(it))
                }
            }
        }

        override fun onError(exception: ImageCaptureException) {
            super.onError(exception)
            mOnCameraManageListenerReference.get()
                ?.onError(exception.imageCaptureError, exception.message, exception.cause)
        }
    }

    /**
     * 视频回调
     */
    @SuppressLint("UnsafeOptInUsageError")
    private class TakeVideoCallback(
        cameraManage: CameraManage, onCameraManageListener: OnCameraManageListener?
    ) : OnVideoSavedCallback {

        private val mCameraManageReference: WeakReference<CameraManage>
        private val mOnCameraManageListenerReference: WeakReference<OnCameraManageListener>

        init {
            mCameraManageReference = WeakReference<CameraManage>(cameraManage)
            mOnCameraManageListenerReference =
                WeakReference<OnCameraManageListener>(onCameraManageListener)
        }

        @SuppressLint("UnsafeOptInUsageError")
        override fun onVideoSaved(outputFileResults: OutputFileResults) {
            TODO("Not yet implemented")
        }

        @SuppressLint("UnsafeOptInUsageError")
        override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
            TODO("Not yet implemented")
        }
    }


    private fun getTargetRotation(): Int {
        return mImageCapture.targetRotation
    }

    /**
     * 使用显示器更改事件，更改图片拍摄器的旋转
     */
    internal inner class DisplayListener : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {}
        override fun onDisplayRemoved(displayId: Int) {}
        override fun onDisplayChanged(displayId: Int) {
            if (displayId == this@CameraManage.displayId) {
                mImageCapture.targetRotation = mViewHolder.previewView.display.rotation
                mImageAnalyzer.targetRotation = mViewHolder.previewView.display.rotation
            }
        }
    }

    override fun onOrientationChanged(orientation: Int) {
        TODO("Not yet implemented")
    }

}