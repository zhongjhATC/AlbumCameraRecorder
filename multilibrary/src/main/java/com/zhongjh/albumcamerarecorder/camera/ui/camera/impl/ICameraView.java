package com.zhongjh.albumcamerarecorder.camera.ui.camera.impl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.otaliastudios.cameraview.CameraView;
import com.zhongjh.albumcamerarecorder.camera.widget.PhotoVideoLayout;
import com.zhongjh.albumcamerarecorder.widget.ImageViewTouch;
import com.zhongjh.albumcamerarecorder.widget.childclickable.IChildClickableLayout;

/**
 * 录制界面规定view的设置
 * 对所有View都标记了NonNull和Nullable
 * 标记了NonNull的View返回是不能为空的，在布局上必须使用这些View，当然，也可以继承View加上你想要的方法
 *
 * @author zhongjh
 * @date 2022/8/19
 */
public interface ICameraView {

    /**
     * 初始化根布局
     * @param inflater onCreateView方法下面的inflater
     * @param container  onCreateView方法下面的container
     *
     * @return 返回布局View
     */
    View setContentView(LayoutInflater inflater, ViewGroup container);

    /**
     * 初始化相关view
     *
     * @param view               初始化好的view
     * @param savedInstanceState savedInstanceState
     */
    void initView(View view, Bundle savedInstanceState);

    /**
     * 设置ChildClickableLayout，各大布局都支持
     *
     * @return 返回ChildClickableLayout，主要用于控制整个屏幕是否接受触摸事件
     */
    @NonNull
    IChildClickableLayout getChildClickableLayout();

    /**
     * 返回顶部View，该View自动兼容沉倾状态栏
     *
     * @return view
     */
    @Nullable
    View getTopView();

    /**
     * 设置CameraView
     *
     * @return 返回CameraView，主要用于拍摄、录制，里面包含水印
     */
    @NonNull
    CameraView getCameraView();

    /**
     * 当想使用自带的多图显示控件，请设置它
     *
     * @return 返回多图的Recycler显示控件
     */
    @Nullable
    RecyclerView getRecyclerViewPhoto();

    /**
     * 修饰多图控件的View，只有第一次初始化有效
     * 一般用于群体隐藏和显示
     * 你也可以重写[hideViewByMultipleZero]方法自行隐藏显示相关view
     *
     * @return View[]
     */
    @Nullable
    View[] getMultiplePhotoView();

    /**
     * 当想使用自带的功能按钮（包括拍摄、录制、录音、确认、取消），请设置它
     *
     * @return PhotoVideoLayout
     */
    @NonNull
    PhotoVideoLayout getPhotoVideoLayout();

    /**
     * 单图控件的View
     *
     * @return ImageViewTouch
     */
    ImageViewTouch getSinglePhotoView();

    /**
     * 左上角的关闭控件
     *
     * @return View
     */
    @Nullable
    View getCloseView();

    /**
     * 右上角的闪光灯控件
     *
     * @return View
     */
    @Nullable
    ImageView getFlashView();

    /**
     * 右上角的切换前置/后置摄像控件
     *
     * @return View
     */
    @Nullable
    ImageView getSwitchView();

}
