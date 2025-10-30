package com.zhongjh.imageedit.core.sticker

import android.graphics.Canvas
import android.graphics.RectF
import android.view.View

/**
 * 贴纸组件的核心接口，定义了贴纸的基本行为和生命周期
 * 是图像处理中贴纸功能的基础抽象层，负责规范贴纸的显示、移除、关闭等操作
 * 所有贴纸相关组件都实现或扩展此接口
 *
 * @author zhongjh
 * @date 2025/09/25
 */
interface ImageStickerPortrait {
    /**
     * 显示贴纸，将贴纸组件添加到视图中并使其可见
     * 此方法被调用后，贴纸将开始在界面上显示，并可响应用户交互
     * 通常在贴纸初始化后调用，或者在dismiss()后重新显示贴纸时调用
     * @return 是否成功显示贴纸
     */
    fun show(): Boolean

    /**
     * 移除贴纸，将贴纸从视图中完全删除
     * 调用此方法后，贴纸将不再参与绘制和交互，相关资源可能被释放
     * 通常在用户确定不再需要该贴纸时调用，调用后贴纸相关资源可能被释放
     * @return 是否成功移除贴纸
     */
    fun remove(): Boolean

    /**
     * 关闭贴纸，通常是指隐藏贴纸但保留其状态
     * 与remove不同，dismiss后贴纸可能通过show方法重新显示，而不需要重新创建
     * 此方法保留贴纸的状态信息，如位置、大小、旋转角度等，以便后续show()时能恢复这些状态
     * @return 是否成功关闭贴纸
     */
    fun dismiss(): Boolean

    /**
     * 检查贴纸当前是否处于显示状态
     * 通常用于条件判断，决定是否需要执行某些操作（如保存贴纸状态）
     * @return true 表示贴纸当前可见并处于活动状态，false表示贴纸处于隐藏或非活动状态
     */
    fun isShowing(): Boolean

    /**
     * 获取贴纸的边界框架矩形
     * 返回的RectF对象表示贴纸在视图坐标系中的精确位置和尺寸，常用于碰撞检测、布局计算等场景
     * @return 贴纸在视图坐标系中的矩形边界，包含位置、宽度和高度信息
     */
    fun getFrame(): RectF

    /**
     * 绘制贴纸的核心方法
     * 该方法在视图绘制周期中被调用，负责将贴纸内容绘制到画布上
     * 通常由视图的onDraw()方法内部调用，实现贴纸的可视化呈现
     * @param canvas 用于绘制贴纸的画布对象
     */
    fun onSticker(canvas: Canvas)

    /**
     * 注册贴纸事件回调监听器
     * 用于建立贴纸与其他组件之间的通信机制，实现事件的传递和响应
     * @param callback 实现了Callback接口的监听器对象，用于接收贴纸的状态变化通知
     */
    fun registerCallback(callback: Callback)

    /**
     * 注销贴纸事件回调监听器
     * 用于解除贴纸与其他组件之间的通信机制，避免内存泄漏
     * @param callback 之前注册的回调监听器，注销后将不再接收贴纸状态变化通知
     */
    fun unregisterCallback(callback: Callback)

    /**
     * 贴纸状态变化的回调接口
     * 用于通知贴纸的显示、隐藏和移除等状态变化事件
     */
    interface Callback {
        /**
         * 贴纸关闭时的回调方法
         * 当贴纸被隐藏但未被移除时触发
         * @param stickerView 标签的视图对象，必须同时实现View和ImageSticker接口
         * 泛型约束确保参数类型同时具备View的UI特性和ImageSticker的贴纸功能
         * @noinspection unused
         */
        fun <V> onDismiss(stickerView: V) where V : View, V : ImageSticker

        /**
         * 贴纸显示时的回调方法
         * 当贴纸从隐藏状态变为可见状态时触发
         * @param stickerView 标签的视图对象，必须同时实现View和ImageSticker接口
         * 泛型约束确保参数类型同时具备View的UI特性和ImageSticker的贴纸功能
         * @noinspection unused
         */
        fun <V> onShowing(stickerView: V) where V : View, V : ImageSticker

        /**
         * 贴纸移除时的回调方法
         * 当贴纸被完全从视图中删除时触发
         * @param stickerView 标签的视图对象，必须同时实现View和ImageSticker接口
         * 泛型约束确保参数类型同时具备View的UI特性和ImageSticker的贴纸功能
         * @return 是否允许删除贴纸
         * 返回值用于控制贴纸是否可以被删除，false表示拦截删除操作
         */
        fun <V> onRemove(stickerView: V): Boolean where V : View, V : ImageSticker
    }
}
