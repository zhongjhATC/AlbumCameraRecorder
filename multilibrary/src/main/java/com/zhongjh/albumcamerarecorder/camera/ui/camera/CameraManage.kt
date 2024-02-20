package com.zhongjh.albumcamerarecorder.camera.ui.camera

import android.content.Context
import android.hardware.display.DisplayManager
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.zhongjh.albumcamerarecorder.camera.listener.CameraXOrientationEventListener
import com.zhongjh.albumcamerarecorder.camera.ui.camera.CameraFragment.ViewHolder
import com.zhongjh.albumcamerarecorder.settings.CameraSpec
import com.zhongjh.common.utils.DisplayMetricsUtils
import java.util.concurrent.Executor
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * 拍摄/录制 管理
 */
class CameraManage(val mContext: Context, val mViewHolder: ViewHolder) : CameraXOrientationEventListener.OnOrientationChangedListener {

    companion object {
        /**
         * 闪关灯状态
         */
        const val TYPE_FLASH_AUTO = 0x0101
        const val TYPE_FLASH_ON = 0x0102
        const val TYPE_FLASH_OFF = 0x0103

        /**
         * 两个宽高比例
         */
        const val RATIO_4_3_VALUE = 4.0 / 3.0
        const val RATIO_16_9_VALUE = 16.0 / 9.0
    }

    /**
     * 拍摄配置
     */
    private val mCameraSpec by lazy { CameraSpec }
    private var mCameraProvider: ProcessCameraProvider? = null
    private var mImageCapture: ImageCapture? = null
    private var mImageAnalyzer: ImageAnalysis? = null
    private val mVideoCapture: VideoCapture? = null
    private val mDisplayManager by lazy { mContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager }
    private val mDisplayListener by lazy { DisplayListener() }
    private val orientationEventListener by lazy { CameraXOrientationEventListener(mContext, this) }
    private var mCameraInfo: CameraInfo? = null
    private var mCameraControl: CameraControl? = null
    private val focusImageView: FocusImageView? = null
    private val mainExecutor: Executor by lazy { ContextCompat.getMainExecutor(mContext) }

    private var displayId = -1

    /**
     * 相机模式
     */
    private var mCameraModel = 0
    private var typeFlash = TYPE_FLASH_OFF

    /**
     * 摄像头方向
     */
    private var lensFacing = CameraSelector.LENS_FACING_BACK

    /**
     * 创建
     */
    fun create() {
        mDisplayManager.registerDisplayListener(mDisplayListener, null)
        mViewHolder.pvCamera.post { displayId = mViewHolder.pvCamera.display.displayId }
    }

    /**
     * ui初始化，必须是ui线程
     */
    fun init() {
        startCamera()
    }

    /**
     * 启动预览
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(mContext)
        cameraProviderFuture.addListener({
            try {
                mCameraProvider = cameraProviderFuture.get()
                initCameraPreviewMode()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, mainExecutor)
    }

    /**
     * onDestroy
     */
    fun onDestroy() {
        mDisplayManager.unregisterDisplayListener(mDisplayListener)
        stopCheckOrientation()
        focusImageView.destroy()
    }

    /**
     * 设置闪光灯
     */
    fun setFlashLamp() {
        typeFlash++
        if (typeFlash > TYPE_FLASH_OFF) {
            typeFlash = TYPE_FLASH_AUTO
        }
        setFlashMode()
    }

    /**
     * 切换前后摄像头
     */
    fun toggleCamera() {
        lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }
        initCameraPreviewMode()
    }

    /**
     * 闪光灯模式
     */
    private fun setFlashMode() {
        if (mImageCapture == null) {
            return
        }
        when (typeFlash) {
            TYPE_FLASH_AUTO -> {
                mImageCapture.flashMode = ImageCapture.FLASH_MODE_AUTO
            }
            TYPE_FLASH_ON -> {
                mImageCapture.flashMode = ImageCapture.FLASH_MODE_ON
            }
            TYPE_FLASH_OFF -> {
                mImageCapture.flashMode = ImageCapture.FLASH_MODE_OFF
            }
        }
    }

    /**
     * 初始相机预览模式
     */
    private fun initCameraPreviewMode() {
        if (mCameraSpec.onlySupportImages()) {
            initCameraPreviewModeByImage()
        } else if (mCameraSpec.onlySupportVideos()) {
            bindCameraVideoUseCases()
        } else {
            bindCameraWithUserCases()
        }
    }

    /**
     * 初始化只拍照模式
     */
    private fun initCameraPreviewModeByImage() {
        try {
            // 获取适合的比例
            val screenAspectRatio: Int = aspectRatio(DisplayMetricsUtils.getScreenWidth(mContext), DisplayMetricsUtils.getScreenHeight(mContext))
            // 获取当前预览的角度
            val rotation: Int = mViewHolder.pvCamera.display.rotation
            // 获取前后置摄像头
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            // 初始化 Preview 对象
            val preview = Preview.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .build()

            // ImageCapture
            initImageCapture()

            // ImageAnalysis
            mImageAnalyzer = ImageAnalysis.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .build()

            // Must unbind the use-cases before rebinding them
            mCameraProvider!!.unbindAll()
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            val camera = mCameraProvider!!.bindToLifecycle((getContext() as LifecycleOwner?)!!, cameraSelector, preview, mImageCapture, mImageAnalyzer)
            // Attach the viewfinder's surface provider to preview use case
            preview.setSurfaceProvider(mCameraPreviewView.getSurfaceProvider())
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
        mImageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(mViewHolder.pvCamera.display.rotation)
            .build()
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
     * 使用显示器更改事件，更改图片拍摄器的旋转
     */
    internal inner class DisplayListener : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {}
        override fun onDisplayRemoved(displayId: Int) {}
        override fun onDisplayChanged(displayId: Int) {
            if (displayId == this@CameraManage.displayId) {
                mImageCapture?.targetRotation = mViewHolder.pvCamera.display.rotation
                mImageAnalyzer?.targetRotation = mViewHolder.pvCamera.display.rotation
            }
        }
    }


    override fun onOrientationChanged(orientation: Int) {
        TODO("Not yet implemented")
    }

}