package com.zhongjh.multimedia.camera.ui.camera.impl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.camera.view.PreviewView
import androidx.recyclerview.widget.RecyclerView
import com.zhongjh.multimedia.camera.widget.FocusView
import com.zhongjh.multimedia.camera.widget.PhotoVideoLayout
import com.zhongjh.multimedia.widget.ImageViewTouch
import com.zhongjh.multimedia.widget.childclickable.IChildClickableLayout

/**
 * 录制界面规定view的设置
 * 对所有View都标记了NonNull和Nullable
 * 标记了NonNull的View返回是不能为空的，在布局上必须使用这些View，当然，也可以继承View加上你想要的方法
 *
 * @author zhongjh
 * @date 2022/8/19
 */
interface ICameraView {
    /**
     * 初始化根布局
     *
     * @param inflater  onCreateView方法下面的inflater
     * @param container onCreateView方法下面的container
     * @return 返回布局View
     */
    fun setContentView(inflater: LayoutInflater, container: ViewGroup?): View

    /**
     * 初始化相关view
     *
     * @param view               初始化好的view
     * @param savedInstanceState savedInstanceState
     */
    fun initView(view: View, savedInstanceState: Bundle?)

    /**
     * 设置ChildClickableLayout，各大布局都支持
     *
     * @return 返回ChildClickableLayout，主要用于控制整个屏幕是否接受触摸事件
     */
    val childClickableLayout: IChildClickableLayout

    /**
     * 返回顶部View，该View自动兼容沉倾状态栏
     *
     * @return view
     */
    val topView: View?

    /**
     * @return 预览View
     */
    val previewView: PreviewView

    /**
     * @return 焦点View
     */
    val focusView: FocusView

    /**
     * 当想使用自带的多图显示控件，请设置它
     *
     * @return 返回多图的Recycler显示控件
     */
    val recyclerViewPhoto: RecyclerView?

    /**
     * 修饰多图控件的View，只有第一次初始化有效
     * 一般用于群体隐藏和显示
     * 你也可以重写[ICameraFragment.hideViewByMultipleZero]方法自行隐藏显示相关view
     *
     * @return View[]
     */
    val multiplePhotoView: Array<View>?

    /**
     * 当想使用自带的功能按钮（包括拍摄、录制、录音、确认、取消），请设置它
     *
     * @return PhotoVideoLayout
     */
    val photoVideoLayout: PhotoVideoLayout

    /**
     * 单图控件的View
     *
     * @return ImageViewTouch
     */
    val singlePhotoView: ImageViewTouch

    /**
     * 左上角的关闭控件
     *
     * @return View
     */
    val closeView: View?

    /**
     * 右上角的闪光灯控件
     *
     * @return View
     */
    val flashView: ImageView?

    /**
     * 右上角的切换前置/后置摄像控件
     *
     * @return View
     */
    val switchView: ImageView?
}
