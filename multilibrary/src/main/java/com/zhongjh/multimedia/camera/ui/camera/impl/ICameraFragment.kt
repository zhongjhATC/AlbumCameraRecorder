package com.zhongjh.multimedia.camera.ui.camera.impl

import android.content.Intent
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.multimedia.camera.entity.BitmapData
import java.io.File

/**
 * 拍摄界面的接口，主要用于告示开发者可以使用哪些方法
 * 大部分是关于View操作的界面逻辑,除了图片、视频、实例化View，其他方法都统一在Fragment使用
 *
 * @author zhongjh
 * @date 2022/8/11
 */
interface ICameraFragment {
    /**
     * 提交图片成功后，返回数据给上一个页面
     *
     * @param newFiles 多媒体数据
     */
    fun commitPictureSuccess(newFiles: ArrayList<LocalMedia>)

    /**
     * 确认提交数据中途报错，失败
     *
     * @param throwable 异常
     */
    fun commitFail(throwable: Throwable)

    /**
     * 取消提交数据
     */
    fun cancel()

    /**
     * 提交视频成功后，返回数据给上一个页面
     *
     * @param intentPreviewVideo 从预览视频界面返回来的数据intent
     */
    fun commitVideoSuccess(intentPreviewVideo: Intent)

    /**
     * 当多个图片删除到没有图片时候，隐藏相关View
     */
    fun hideViewByMultipleZero()

    /**
     * 显示单图
     *
     * @param bitmapData 显示单图数据源
     * @param file       显示单图的文件
     * @param path       显示单图的path
     */
    fun showSinglePicture(bitmapData: BitmapData, file: File, path: String)

    /**
     * 显示多图
     */
    fun showMultiplePicture()

    /**
     * 恢复底部菜单,母窗体启动滑动
     */
    fun showBottomMenu()

    /**
     * 确认处理数据时，显示一个等待动画
     */
    fun showProgress()

    /**
     * 确认处理数据时，显示一个等待动画
     *
     * @param progress 当前进度
     */
    fun setProgress(progress: Int)

    /**
     * 设置界面的功能按钮可以使用
     * 场景：如果压缩或者移动文件时异常，则恢复
     */
    fun setUiEnableTrue()

    /**
     * 设置界面的功能按钮禁止使用
     * 场景：确认图片时，压缩中途禁止某些功能使用
     */
    fun setUiEnableFalse()
}
