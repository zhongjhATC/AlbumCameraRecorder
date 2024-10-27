//package com.zhongjh.example.cameraxapp
//
//import android.graphics.SurfaceTexture
//import android.opengl.GLSurfaceView
//import com.zhongjh.example.cameraxapp.filter.Filter
//import com.zhongjh.example.cameraxapp.filter.ScreenFilter
//import javax.microedition.khronos.egl.EGLConfig
//import javax.microedition.khronos.opengles.GL10
//import kotlin.coroutines.jvm.internal.CompletedContinuation.context
//
///**
// * 关于 OpenGL
// * 1.glGenTextures 生成一个纹理句柄
// * 2.glBindTexture 绑定纹理句柄
// * 3.glTexParameteri 设置纹理参数
// * 4.glTexImage2D 输入到像素数据到纹理
// * 5.glActiveTexture 激活纹理
// * 6.glDrawElements 绘制到屏幕
// * 7.eglSwapBuffers 交换缓冲区，显示到屏幕
// */
//class GLCameraRender : GLSurfaceView.Renderer {
//
//    private var textures: IntArray = IntArray(1)
//    private var surfaceTexture: SurfaceTexture? = null
//    private var filter: Filter? = null
//    var type: String = "Normal"
//
//    /**
//     * 由于需要使用OpenGL绘制预览帧，所以在onSurfaceCreated时需要利用OpenGL的api创建一个SurfaceTexture，后面会将这个SurfaceTexture绘制出来。
//     * 1. 创建一个SurfaceTexture，textures[0]作为为OpenGL Texture的唯一标识
//     */
//    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
//        gl?.let {
//            // 1.glGenTextures 生成一个纹理句柄
//            it.glGenTextures(textures.size, textures, 0)
//            surfaceTexture = SurfaceTexture(textures[0])
//            filter = ScreenFilter(context)
//
//        }
//    }
//
//    /**
//     * 当 surface 的尺寸发生改变时该方法被调用。往往在这里设置 viewport。
//     * 若你的 camera 是固定的，也可以在这里设置 camera。
//     */
//    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
//        TODO("Not yet implemented")
//    }
//
//    /**
//     * 每帧都通过该方法进行绘制。
//     * 绘制时通常先调用 glClear　函数来清空 framebuffer，然后在调用 OpenGL ES 的起它的接口进行绘制。
//     */
//    override fun onDrawFrame(gl: GL10?) {
//        TODO("Not yet implemented")
//    }
//}