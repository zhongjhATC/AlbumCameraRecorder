package com.zhongjh.example.cameraxapp.filter

import android.content.Context
import android.opengl.GLES20
import com.zhongjh.example.cameraxapp.OpenGLUtils
import com.zhongjh.example.cameraxapp.R

class ScreenFilter(context: Context): Filter {

    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private val program: Int

    init {
        val vertexShader = OpenGLUtils.readRawTextFile(context, R.raw.camera_vert)
        val textureShader = OpenGLUtils.readRawTextFile(context, R.raw.camera_frag)

        program = OpenGLUtils.loadProgram(vertexShader, textureShader)
    }

    override fun onDrawFrame(textureId: Int): Int {
        // 1.设置窗口大小
        GLES20.glViewport(0, 0, mWidth, mHeight)
        // 2.使用着色器程序
        GLES20.glUseProgram(program)
    }

    override fun setTransformMatrix(mtx: FloatArray) {
        TODO("Not yet implemented")
    }

    override fun onReady(width: Int, height: Int) {
        mWidth = width
        mHeight = height
    }
}