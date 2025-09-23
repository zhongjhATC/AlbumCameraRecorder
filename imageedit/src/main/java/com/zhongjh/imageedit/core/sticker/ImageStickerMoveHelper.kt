package com.zhongjh.imageedit.core.sticker

import android.graphics.Matrix
import android.view.MotionEvent
import android.view.View

/**
 * 贴纸移动功能的辅助类，处理贴纸视图的拖拽移动操作
 * 负责将用户的触摸事件转换为贴纸的位置变化，考虑了旋转角度的影响
 *
 * 构造函数，初始化贴纸移动辅助类
 *
 * @param view 要移动的视图对象
 *
 * @author zhongjh
 * @date 2025/09/23
 */
class ImageStickerMoveHelper(private val view: View) {
    /**
     * 记录触摸按下时的X坐标
     */
    private var x = 0f

    /**
     * 记录触摸按下时的Y坐标
     */
    private var y = 0f

    /**
     * 处理触摸事件，实现贴纸的拖拽移动
     * 支持ACTION_DOWN和ACTION_MOVE事件，考虑了视图的旋转角度
     *
     * @param v 触发触摸事件的视图
     * @param event 触摸事件对象
     * @return 如果事件被处理则返回true，否则返回false
     */
    fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // 记录触摸按下时的坐标
                x = event.x
                y = event.y
                // 重置变换矩阵并设置旋转角度（与视图的旋转角度一致）
                M.reset()
                M.setRotate(v.rotation)
                // 返回true表示事件被处理
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                // 计算触摸点的移动距离
                val dxy = floatArrayOf(event.x - x, event.y - y)
                // 将移动距离应用变换矩阵，考虑视图的旋转角度
                M.mapPoints(dxy)
                // 更新视图的平移位置
                v.translationX = view.translationX + dxy[0]
                v.translationY = view.translationY + dxy[1]
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
         * 静态变换矩阵，用于处理考虑旋转角度的坐标变换
         */
        private val M = Matrix()
    }
}
