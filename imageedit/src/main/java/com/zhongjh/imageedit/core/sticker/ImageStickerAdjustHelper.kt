package com.zhongjh.imageedit.core.sticker

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import com.zhongjh.imageedit.view.BaseImageStickerView
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * 贴纸调整功能的辅助类，实现View.OnTouchListener接口
 * 负责处理贴纸的缩放和旋转操作，通常用于贴纸的控制点交互
 *
 *
 * @param mContainer 贴纸容器视图，通常是BaseImageStickerView或其子类
 * @param mView 要调整的视图对象
 *
 * @author zhongjh
 * @date 2025/09/19
 */
class ImageStickerAdjustHelper(private val mContainer: BaseImageStickerView, private val mView: View) : OnTouchListener {
    /**
     * 触摸点到中心点的距离，用于计算缩放比例
     */
    private var mRadius = 0.0

    /**
     * 触摸点相对于中心点的角度，用于计算旋转角度
     */
    private var mDegrees = 0.0

    /**
     * 变换矩阵，用于处理坐标变换
     */
    private val mMatrix = Matrix()

    init {
        // 设置触摸监听器
        mView.setOnTouchListener(this)
    }

    /**
     * 处理触摸事件，实现贴纸的缩放和旋转功能
     * 支持ACTION_DOWN和ACTION_MOVE事件
     *
     * @param v 触发触摸事件的视图
     * @param event 触摸事件对象
     * @return 如果事件被处理则返回true，否则返回false
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        var mCenterY: Float
        val pointX: Float
        val pointY: Float
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 获取触摸点在视图内的坐标
                val x = event.x
                val y = event.y


                // 初始化中心点坐标（这里设为0，0）
                val mCenterX: Float = 0.also { mCenterY = it.toFloat() }.toFloat().toFloat()


                // 计算触摸点相对于容器中心点的坐标
                pointX = mView.x + x - mContainer.pivotX
                pointY = mView.y + y - mContainer.pivotY


                // 记录调试信息
                Log.d(TAG, String.format("X=%f,Y=%f", pointX, pointY))


                // 计算触摸点到中心点的距离和角度
                mRadius = toLength(pointX, pointY)
                mDegrees = toDegrees(pointY, pointX)


                // 设置变换矩阵，用于后续计算
                mMatrix.setTranslate(pointX - x, pointY - y)


                // 记录调试信息
                Log.d(TAG, String.format("degrees=%f", toDegrees(pointY, pointX)))


                // 应用旋转变换
                mMatrix.postRotate(-toDegrees(pointY, pointX).toFloat(), mCenterX, mCenterY)


                // 返回true表示事件被处理
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                // 获取移动后的触摸点坐标
                val xy = floatArrayOf(event.x, event.y)


                // 计算移动后触摸点相对于容器中心点的坐标
                pointX = mView.x + xy[0] - mContainer.pivotX
                pointY = mView.y + xy[1] - mContainer.pivotY


                // 记录调试信息
                Log.d(TAG, String.format("X=%f,Y=%f", pointX, pointY))


                // 计算新的距离和角度
                val radius = toLength(pointX, pointY)
                val degrees = toDegrees(pointY, pointX)


                // 计算缩放比例
                val scale = (radius / mRadius).toFloat()


                // 应用缩放
                mContainer.addScale(scale)


                // 记录调试信息
                Log.d(TAG, "    D   = " + (degrees - mDegrees))


                // 应用旋转
                mContainer.rotation = (mContainer.rotation + degrees - mDegrees).toFloat()


                // 更新当前半径，用于下一次计算
                mRadius = radius


                // 返回true表示事件被处理
                return true
            }

            else -> {}
        }
        // 默认返回false表示事件未被处理
        return false
    }

    companion object {
        /**
         * 日志标记，用于调试和日志输出
         */
        private const val TAG = "IMGStickerAdjustHelper"

        /**
         * 将笛卡尔坐标转换为角度
         * 使用Math.atan2计算弧度，然后转换为角度
         *
         * @param y Y坐标
         * @param x X坐标
         * @return 角度值
         */
        private fun toDegrees(y: Float, x: Float): Double {
            return Math.toDegrees(atan2(y.toDouble(), x.toDouble()))
        }

        /**
         * 计算点到原点的距离
         * 使用勾股定理计算距离
         *
         * @param x X坐标
         * @param y Y坐标
         * @return 距离值
         */
        private fun toLength(x: Float, y: Float): Double {
            return sqrt((x * x + y * y).toDouble())
        }
    }
}
