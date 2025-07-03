package com.zhongjh.multimedia.camera.listener

import android.content.Context
import android.view.OrientationEventListener
import android.view.Surface


/**
 * 用于监控手机方向
 */
class OnCameraXOrientationEventListener(context: Context, private val changedListener: OnOrientationChangedListener) : OrientationEventListener(context) {

    companion object {
        const val TAG = "OrientationListener"
    }

    private var mRotation = Surface.ROTATION_0


    override fun onOrientationChanged(orientation: Int) {
        if (orientation == ORIENTATION_UNKNOWN) {
            return
        }

        // 获取屏幕旋转角度
        val currentRotation: Int = when (orientation) {
            in 71..109 -> Surface.ROTATION_270
            in 161..199 -> Surface.ROTATION_180
            in 251..289 -> Surface.ROTATION_90
            else -> Surface.ROTATION_0
        }
        if (mRotation != currentRotation) {
            mRotation = currentRotation
            changedListener.onOrientationChanged(mRotation)
        }
    }

    interface OnOrientationChangedListener {
        fun onOrientationChanged(orientation: Int)
    }

    /**
     * 开始检测手机方向
     */
    fun star() {
        enable()
    }

    /**
     * 停止检测手机方向
     */
    fun stop() {
        disable()
    }
}