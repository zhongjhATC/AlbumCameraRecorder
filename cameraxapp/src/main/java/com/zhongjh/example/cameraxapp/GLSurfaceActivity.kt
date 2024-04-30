//package com.zhongjh.example.cameraxapp
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.os.Build
//import android.os.Bundle
//import android.util.Size
//import androidx.appcompat.app.AppCompatActivity
//import androidx.camera.core.CameraSelector
//import androidx.camera.core.ImageCapture
//import androidx.camera.core.Preview
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.LifecycleOwner
//import com.zhongjh.example.cameraxapp.databinding.ActivityGlSurfaceBinding
//import com.zhongjh.example.cameraxapp.databinding.ActivityMainBinding
//
///**
// * 参考： https://blog.csdn.net/tujidi1csd/article/details/125217910
// * 参考： https://blog.csdn.net/u012346890/article/details/116206407
// * 参考： https://blog.csdn.net/york2017/article/details/111500932
// */
//class GLSurfaceActivity : AppCompatActivity() {
//
//    private lateinit var viewBinding: ActivityGlSurfaceBinding
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        viewBinding = ActivityGlSurfaceBinding.inflate(layoutInflater)
//        setContentView(viewBinding.root)
//
//        // 判断是否满足权限
//        if (allPermissionsGranted()) {
//            startCamera()
//        } else {
//            // 请求权限
//            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
//        }
//    }
//
//
//    /**
//     * 启动预览
//     */
//    private fun startCamera() {
//        // 创建 ProcessCameraProvider 的实例。这用于将相机的生命周期绑定到生命周期所有者。这消除了打开和关闭相机的任务，因为 CameraX 具有生命周期感知能力。
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//        // 向 cameraProviderFuture 添加监听器。添加 Runnable 作为一个参数。添加 ContextCompat.getMainExecutor() 作为第二个参数。这将返回一个在主线程上运行的 Executor。
//        cameraProviderFuture.addListener({
//            // 在 Runnable 中，添加 ProcessCameraProvider。它用于将相机的生命周期绑定到应用进程中的 LifecycleOwner
//            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
//            val preview: Preview = Preview.Builder().build()
//
//            val imageCapture = ImageCapture.Builder().build()
//
//            preview.setSurfaceProvider(surfaceProvider)
//
//            cameraProvider.unbindAll()
//
//            val camera =
//                cameraProvider.bindToLifecycle(
//                    context as LifecycleOwner,
//                    CameraSelector.DEFAULT_BACK_CAMERA,
//                    imageCapture,
//                    preview
//                )
//            val cameraInfo = camera.cameraInfo
//            val cameraControl = camera.cameraControl
//        }, ContextCompat.getMainExecutor(context))
//    }
//
//    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
//        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
//    }
//
//    companion object {
//        private const val TAG = "CameraXApp"
//        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
//        private const val REQUEST_CODE_PERMISSIONS = 10
//        private val REQUIRED_PERMISSIONS =
//            mutableListOf(
//                Manifest.permission.CAMERA,
//                Manifest.permission.RECORD_AUDIO
//            ).apply {
//                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
//                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                }
//            }.toTypedArray()
//    }
//
//}