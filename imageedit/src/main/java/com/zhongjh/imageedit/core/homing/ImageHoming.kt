package com.zhongjh.imageedit.core.homing

/**
 * 图像归位信息类，用于处理图像的放大、旋转和平移动画
 * 存储图像的平移坐标、缩放比例和旋转角度信息
 * 在图像编辑操作中，用于实现平滑的动画过渡效果
 *
 * @param x  X轴平移坐标
 * @param y  Y轴平移坐标
 * @param scale 缩放比例
 * @param rotate 旋转角度
 *
 * @author zhongjh
 * @date 2025/09/16
 */
class ImageHoming(var x: Float, var y: Float, var scale: Float, var rotate: Float) {
    /**
     * 设置归位信息的平移坐标、缩放比例和旋转角度
     *
     * @param x  X轴平移坐标
     * @param y  Y轴平移坐标
     * @param scale 缩放比例
     * @param rotate 旋转角度
     */
    fun set(x: Float, y: Float, scale: Float, rotate: Float) {
        this.x = x
        this.y = y
        this.scale = scale
        this.rotate = rotate
    }

    /**
     * 合并另一个归位信息对象的参数
     * 将当前对象的缩放比例乘以目标对象的缩放比例，平移坐标加上目标对象的平移坐标
     * 常用于组合多个变换操作，实现复杂的图像动画效果
     *
     * @param homing 要合并的归位信息对象
     */
    fun concat(homing: ImageHoming) {
        this.scale *= homing.scale
        this.x += homing.x
        this.y += homing.y
    }

    /**
     * 反向合并另一个归位信息对象的参数
     * 将当前对象的缩放比例乘以目标对象的缩放比例，平移坐标减去目标对象的平移坐标
     * 常用于撤销或反向执行之前的变换操作
     *
     * @param homing 要反向合并的归位信息对象
     */
    fun rConcat(homing: ImageHoming) {
        this.scale *= homing.scale
        this.x -= homing.x
        this.y -= homing.y
    }

    /**
     * 返回归位信息对象的字符串表示
     *
     * @return 包含x、y、scale和rotate属性的字符串
     */
    override fun toString(): String {
        return "IMGHoming{" +
                "x=" + x +
                ", y=" + y +
                ", scale=" + scale +
                ", rotate=" + rotate +
                '}'
    }

    companion object {
        /**
         * 判断两个归位信息对象的旋转角度是否不同
         *
         * @param sHoming 起始归位信息对象
         * @param eHoming 结束归位信息对象
         * @return 如果旋转角度不同则返回true，否则返回false
         */
        fun isRotate(sHoming: ImageHoming, eHoming: ImageHoming): Boolean {
            return java.lang.Float.compare(sHoming.rotate, eHoming.rotate) != 0
        }
    }
}
