package com.zhongjh.common.utils

import android.view.animation.RotateAnimation
import android.widget.ImageView

/**
 *
 * 跟App相关的辅助类
 * @author zhongjh
 * @date 2022/9/21
 */
object AnimUtils {
    /**
     * ImageView的旋转动画
     *
     * @param imageView 控制该view进行旋转的动画
     * @param flag flag为true则向上
     */
    fun rotateArrow(imageView: ImageView, flag: Boolean) {
        val pivotX = imageView.width / 2f
        val pivotY = imageView.height / 2f
        // flag为true则向上
        val fromDegrees = 180f
        val toDegrees = 360f
        // 旋转动画效果   参数值 旋转的开始角度  旋转的结束角度  pivotX x轴伸缩值
        val animation = RotateAnimation(
            fromDegrees, toDegrees,
            pivotX, pivotY
        )
        // 该方法用于设置动画的持续时间，以毫秒为单位
        animation.duration = 350
        // 启动动画
        imageView.startAnimation(animation)
    }
}
