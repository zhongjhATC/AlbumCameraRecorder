package com.zhongjh.imageedit.core.sticker

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.view.View

/**
 * 贴纸功能的核心辅助类，管理贴纸视图的生命周期和行为
 * 是一个泛型类，提供了贴纸的显示、隐藏、移除和回调通知等功能
 * 实现了ImageStickerPortrait和ImageStickerPortrait.Callback接口，统一管理贴纸的状态变化
 *
 * @param <StickerView> 泛型参数，必须同时是View的子类并实现ImageSticker接口
 *
 * @param view 贴纸视图对象，必须同时是View的子类并实现ImageSticker接口
 *
 * @author zhongjh
 * @date 2025/09/22
 */
class ImageStickerHelper<StickerView>(private val view: StickerView) : ImageStickerPortrait, ImageStickerPortrait.Callback where StickerView : View, StickerView : ImageSticker {
    /**
     * 贴纸的边界框架矩形，通过Matrix计算得到
     */
    private var mFrame: RectF? = null

    /**
     * 贴纸状态变化的回调接口，用于通知外部贴纸的状态变化
     */
    private var mCallback: ImageStickerPortrait.Callback? = null

    /**
     * 贴纸的显示状态标志
     */
    private var isShowing = false

    /**
     * 显示贴纸视图
     * 如果贴纸当前未显示，则将其显示状态设置为true并触发onShowing回调
     *
     * @return 如果贴纸成功显示则返回true，否则返回false
     */
    override fun show(): Boolean {
        if (!isShowing()) {
            isShowing = true
            onShowing(view)
            return true
        }
        return false
    }

    /**
     * 移除贴纸视图
     * 调用onRemove回调，决定是否移除贴纸
     *
     * @return 如果贴纸成功移除则返回true，否则返回false
     */
    override fun remove(): Boolean {
        return onRemove(view)
    }

    /**
     * 关闭贴纸视图
     * 如果贴纸当前正在显示，则将其显示状态设置为false，清空框架并触发onDismiss回调
     *
     * @return 如果贴纸成功关闭则返回true，否则返回false
     */
    override fun dismiss(): Boolean {
        if (isShowing()) {
            isShowing = false
            onDismiss(view)
            return true
        }
        return false
    }

    /**
     * 检查贴纸当前是否处于显示状态
     *
     * @return true 表示贴纸当前处于显示状态，false表示贴纸处于隐藏状态
     */
    override fun isShowing(): Boolean {
        return isShowing
    }

    /**
     * 获取贴纸的边界框架矩形
     * 如果框架为空，则根据贴纸视图的位置、大小、缩放比例和中心点重新计算
     *
     * @return 贴纸在视图坐标系中的矩形边界
     */
    override fun getFrame(): RectF {
        if (mFrame == null) {
            // 初始化框架为视图的原始尺寸
            mFrame = RectF(0f, 0f, view.width.toFloat(), view.height.toFloat())
            // 计算视图的中心点坐标
            val pivotX = view.getX() + view.getPivotX()
            val pivotY = view.getY() + view.getPivotY()

            // 创建变换矩阵，应用位移和缩放
            val matrix = Matrix()
            matrix.setTranslate(view.getX(), view.getY())
            matrix.postScale(view.scaleX, view.scaleY, pivotX, pivotY)
            // 将变换应用到框架矩形
            matrix.mapRect(mFrame)
        }
        return mFrame!!
    }

    /**
     * 绘制贴纸的方法
     * 此实现为空，子类可以根据需要重写此方法来实现自定义绘制逻辑
     *
     * @param canvas 用于绘制贴纸的画布对象
     */
    override fun onSticker(canvas: Canvas?) {
        // 空实现，由子类重写
    }

    /**
     * 注册贴纸状态变化的回调监听器
     *
     * @param callback 实现了Callback接口的监听器对象
     */
    override fun registerCallback(callback: ImageStickerPortrait.Callback?) {
        mCallback = callback
    }

    /**
     * 注销贴纸状态变化的回调监听器
     *
     * @param callback 之前注册的回调监听器
     */
    override fun unregisterCallback(callback: ImageStickerPortrait.Callback?) {
        mCallback = null
    }

    /**
     * 贴纸移除时的回调方法
     * 通知注册的回调监听器贴纸即将被移除
     *
     * @param stickerView 贴纸视图对象
     * @param <V> 泛型参数，必须同时是View的子类并实现ImageSticker接口
     * @return 如果允许移除贴纸则返回true，否则返回false
    </V> */
    override fun <V> onRemove(stickerView: V): Boolean where V : View?, V : ImageSticker? {
        return mCallback != null && mCallback!!.onRemove(stickerView)
    }

    /**
     * 贴纸关闭时的回调方法
     * 清空框架，使视图无效以触发重绘，并通知注册的回调监听器
     *
     * @param stickerView 贴纸视图对象
     * @param <V> 泛型参数，必须同时是View的子类并实现ImageSticker接口
    </V> */
    override fun <V> onDismiss(stickerView: V) where V : View?, V : ImageSticker? {
        mFrame = null
        stickerView?.invalidate()
        mCallback?.onDismiss(stickerView)
    }

    /**
     * 贴纸显示时的回调方法
     * 使视图无效以触发重绘，并通知注册的回调监听器
     *
     * @param stickerView 贴纸视图对象
     * @param <V> 泛型参数，必须同时是View的子类并实现ImageSticker接口
    </V> */
    override fun <V> onShowing(stickerView: V) where V : View?, V : ImageSticker? {
        stickerView?.invalidate()
        mCallback?.onShowing(stickerView)
    }
}
