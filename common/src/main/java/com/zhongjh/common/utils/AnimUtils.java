package com.zhongjh.common.utils;

import android.view.animation.RotateAnimation;
import android.widget.ImageView;

/**
 *
 * 跟App相关的辅助类
 * @author zhongjh
 * @date 2022/9/21
 */
public class AnimUtils {

    /**
     * ImageView的旋转动画
     *
     * @param imageView 控制该view进行旋转的动画
     * @param flag flag为true则向上
     */
    public static void rotateArrow(ImageView imageView, boolean flag) {
        float pivotX = imageView.getWidth() / 2f;
        float pivotY = imageView.getHeight() / 2f;
        // flag为true则向上
        float fromDegrees = flag ? 180f : 180f;
        float toDegrees = flag ? 360f : 360f;
        // 旋转动画效果   参数值 旋转的开始角度  旋转的结束角度  pivotX x轴伸缩值
        RotateAnimation animation = new RotateAnimation(fromDegrees, toDegrees,
                pivotX, pivotY);
        // 该方法用于设置动画的持续时间，以毫秒为单位
        animation.setDuration(350);
        // 启动动画
        imageView.startAnimation(animation);
    }
}
