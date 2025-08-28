package com.zhongjh.imageedit.core.sticker;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.view.View;

/**
 * 贴纸功能的核心辅助类，管理贴纸视图的生命周期和行为
 * 是一个泛型类，提供了贴纸的显示、隐藏、移除和回调通知等功能
 * 实现了ImageStickerPortrait和ImageStickerPortrait.Callback接口，统一管理贴纸的状态变化
 * 
 * @param <StickerView> 泛型参数，必须同时是View的子类并实现ImageSticker接口
 * 
 * @author felix
 * @date 2017/11/16 下午5:52
 */
public class ImageStickerHelper<StickerView extends View & ImageSticker> implements
        ImageStickerPortrait, ImageStickerPortrait.Callback {

    /**
     * 贴纸的边界框架矩形，通过Matrix计算得到
     */
    private RectF mFrame;

    /**
     * 贴纸视图对象，泛型确保它同时是View和ImageSticker
     */
    private final StickerView mView;

    /**
     * 贴纸状态变化的回调接口，用于通知外部贴纸的状态变化
     */
    private Callback mCallback;

    /**
     * 贴纸的显示状态标志
     */
    private boolean isShowing = false;

    /**
     * 构造函数，初始化贴纸辅助类
     * 
     * @param view 贴纸视图对象，必须同时是View的子类并实现ImageSticker接口
     */
    public ImageStickerHelper(StickerView view) {
        mView = view;
    }

    /**
     * 显示贴纸视图
     * 如果贴纸当前未显示，则将其显示状态设置为true并触发onShowing回调
     * 
     * @return 如果贴纸成功显示则返回true，否则返回false
     */
    @Override
    public boolean show() {
        if (!isShowing()) {
            isShowing = true;
            onShowing(mView);
            return true;
        }
        return false;
    }

    /**
     * 移除贴纸视图
     * 调用onRemove回调，决定是否移除贴纸
     * 
     * @return 如果贴纸成功移除则返回true，否则返回false
     */
    @Override
    public boolean remove() {
        return onRemove(mView);
    }

    /**
     * 关闭贴纸视图
     * 如果贴纸当前正在显示，则将其显示状态设置为false，清空框架并触发onDismiss回调
     * 
     * @return 如果贴纸成功关闭则返回true，否则返回false
     */
    @Override
    public boolean dismiss() {
        if (isShowing()) {
            isShowing = false;
            onDismiss(mView);
            return true;
        }
        return false;
    }

    /**
     * 检查贴纸当前是否处于显示状态
     * 
     * @return true 表示贴纸当前处于显示状态，false表示贴纸处于隐藏状态
     */
    @Override
    public boolean isShowing() {
        return isShowing;
    }

    /**
     * 获取贴纸的边界框架矩形
     * 如果框架为空，则根据贴纸视图的位置、大小、缩放比例和中心点重新计算
     * 
     * @return 贴纸在视图坐标系中的矩形边界
     */
    @Override
    public RectF getFrame() {
        if (mFrame == null) {
            // 初始化框架为视图的原始尺寸
            mFrame = new RectF(0, 0, mView.getWidth(), mView.getHeight());
            // 计算视图的中心点坐标
            float pivotX = mView.getX() + mView.getPivotX();
            float pivotY = mView.getY() + mView.getPivotY();

            // 创建变换矩阵，应用位移和缩放
            Matrix matrix = new Matrix();
            matrix.setTranslate(mView.getX(), mView.getY());
            matrix.postScale(mView.getScaleX(), mView.getScaleY(), pivotX, pivotY);
            // 将变换应用到框架矩形
            matrix.mapRect(mFrame);
        }
        return mFrame;
    }

    /**
     * 绘制贴纸的方法
     * 此实现为空，子类可以根据需要重写此方法来实现自定义绘制逻辑
     * 
     * @param canvas 用于绘制贴纸的画布对象
     */
    @Override
    public void onSticker(Canvas canvas) {
        // 空实现，由子类重写
    }

    /**
     * 注册贴纸状态变化的回调监听器
     * 
     * @param callback 实现了Callback接口的监听器对象
     */
    @Override
    public void registerCallback(Callback callback) {
        mCallback = callback;
    }

    /**
     * 注销贴纸状态变化的回调监听器
     * 
     * @param callback 之前注册的回调监听器
     */
    @Override
    public void unregisterCallback(Callback callback) {
        mCallback = null;
    }

    /**
     * 贴纸移除时的回调方法
     * 通知注册的回调监听器贴纸即将被移除
     * 
     * @param stickerView 贴纸视图对象
     * @param <V> 泛型参数，必须同时是View的子类并实现ImageSticker接口
     * @return 如果允许移除贴纸则返回true，否则返回false
     */
    @Override
    public <V extends View & ImageSticker> boolean onRemove(V stickerView) {
        return mCallback != null && mCallback.onRemove(stickerView);
    }

    /**
     * 贴纸关闭时的回调方法
     * 清空框架，使视图无效以触发重绘，并通知注册的回调监听器
     * 
     * @param stickerView 贴纸视图对象
     * @param <V> 泛型参数，必须同时是View的子类并实现ImageSticker接口
     */
    @Override
    public <V extends View & ImageSticker> void onDismiss(V stickerView) {
        mFrame = null;
        stickerView.invalidate();
        if (mCallback != null) {
            mCallback.onDismiss(stickerView);
        }
    }

    /**
     * 贴纸显示时的回调方法
     * 使视图无效以触发重绘，并通知注册的回调监听器
     * 
     * @param stickerView 贴纸视图对象
     * @param <V> 泛型参数，必须同时是View的子类并实现ImageSticker接口
     */
    @Override
    public <V extends View & ImageSticker> void onShowing(V stickerView) {
        stickerView.invalidate();
        if (mCallback != null) {
            mCallback.onShowing(stickerView);
        }
    }
}
