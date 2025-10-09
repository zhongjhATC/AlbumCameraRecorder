package com.zhongjh.imageedit.core.util

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.GLES20

/**
 * OpenGL EGL工具类，提供与EGL相关的实用方法
 * 主要用于获取设备支持的最大纹理尺寸，确保加载的图像不会超过硬件限制
 *
 * @author Oleksii Shliama [[...](https://github.com/shliama)] on 9/8/16.
 */
object EglUtils {

    val maxTextureSize: Int
        /**
         * 获取设备支持的最大纹理尺寸
         * 使用EGL14 API获取OpenGL ES支持的最大纹理尺寸
         * 该值决定了应用程序可以加载的最大图像尺寸，超过此尺寸可能导致渲染问题
         *
         * 在图像处理应用中，了解此限制至关重要，因为它直接影响图像加载和处理的策略
         * 特别是在贴纸、滤镜和其他图像编辑操作中，需要确保图像不会超过此硬件限制
         *
         * @return 设备支持的最大纹理尺寸（像素），如果获取失败则返回0
         */
        get() = maxTextureEgl14

    private val maxTextureEgl14: Int
        /**
         * 使用EGL14 API获取设备支持的最大纹理尺寸
         * 该方法通过创建一个临时的EGL上下文和表面来查询OpenGL ES设备的最大纹理尺寸
         *
         * 方法选择使用OpenGL ES 2.0是因为它在大多数现代Android设备上都受支持，
         * 并且提供了足够的功能来查询基本的硬件限制信息
         *
         * @return 设备支持的最大纹理尺寸（像素），如果获取失败则返回0
         */
        get() {
            // 获取默认EGL显示设备
            val dpy = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            // 用于存储EGL版本信息的数组
            val vers = IntArray(2)
            // 初始化EGL显示设备
            EGL14.eglInitialize(dpy, vers, 0, vers, 1)

            // 设置EGL配置属性，指定需要RGB颜色缓冲区、OpenGL ES 2.0支持和PBuffer表面
            val configAttr = intArrayOf(
                EGL14.EGL_COLOR_BUFFER_TYPE, EGL14.EGL_RGB_BUFFER,
                EGL14.EGL_LEVEL, 0,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                EGL14.EGL_NONE
            )
            // 用于存储EGL配置的数组
            val configs = arrayOfNulls<EGLConfig>(1)
            // 用于存储找到的配置数量
            val numConfig = IntArray(1)
            // 选择符合指定属性的EGL配置
            EGL14.eglChooseConfig(dpy, configAttr, 0, configs, 0, 1, numConfig, 0)
            // 如果没有找到符合条件的配置，返回0
            if (numConfig[0] == 0) {
                return 0
            }
            // 获取找到的第一个EGL配置
            val config = configs[0]

            // 设置PBuffer表面属性，尺寸为64x64（足够小，只用于查询信息）
            val surfAttr = intArrayOf(
                EGL14.EGL_WIDTH, 64,
                EGL14.EGL_HEIGHT, 64,
                EGL14.EGL_NONE
            )
            // 创建PBuffer表面
            val surf = EGL14.eglCreatePbufferSurface(dpy, config, surfAttr, 0)

            // 设置EGL上下文属性，指定使用OpenGL ES 2.0
            val ctxAttrib = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)
            // 创建EGL上下文
            val ctx = EGL14.eglCreateContext(dpy, config, EGL14.EGL_NO_CONTEXT, ctxAttrib, 0)

            // 将当前线程与指定的EGL上下文和表面关联
            EGL14.eglMakeCurrent(dpy, surf, surf, ctx)

            // 用于存储最大纹理尺寸的数组
            val maxSize = IntArray(1)
            // 查询OpenGL ES设备支持的最大纹理尺寸
            GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxSize, 0)

            // 清理资源：断开当前线程与EGL上下文和表面的关联
            EGL14.eglMakeCurrent(dpy, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
            // 销毁PBuffer表面
            EGL14.eglDestroySurface(dpy, surf)
            // 销毁EGL上下文
            EGL14.eglDestroyContext(dpy, ctx)
            // 终止EGL显示设备
            EGL14.eglTerminate(dpy)

            // 返回获取到的最大纹理尺寸
            return maxSize[0]
        }
}
