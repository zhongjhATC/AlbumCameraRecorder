package com.zhongjh.multimedia.camera.ui.camera.impl;

import android.content.Intent;

import com.zhongjh.common.entity.LocalMedia;
import com.zhongjh.multimedia.camera.entity.BitmapData;

import java.io.File;
import java.util.ArrayList;

/**
 * 拍摄界面的接口，主要用于告示开发者可以使用哪些方法
 * 大部分是关于View操作的界面逻辑,除了图片、视频、实例化View，其他方法都统一在Fragment使用
 *
 * @author zhongjh
 * @date 2022/8/11
 */
public interface ICameraFragment {

    /**
     * 提交图片成功后，返回数据给上一个页面
     *
     * @param newFiles 多媒体数据
     */
    void commitPictureSuccess(ArrayList<LocalMedia> newFiles);

    /**
     * 确认提交数据中途报错，失败
     *
     * @param throwable 异常
     */
    void commitFail(Throwable throwable);

    /**
     * 取消提交数据
     */
    void cancel();

    /**
     * 提交视频成功后，返回数据给上一个页面
     *
     * @param intentPreviewVideo 从预览视频界面返回来的数据intent
     */
    void commitVideoSuccess(Intent intentPreviewVideo);

    /**
     * 当多个图片删除到没有图片时候，隐藏相关View
     */
    void hideViewByMultipleZero();

    /**
     * 显示单图
     *
     * @param bitmapData 显示单图数据源
     * @param file       显示单图的文件
     * @param path       显示单图的path
     */
    void showSinglePicture(BitmapData bitmapData, File file, String path);

    /**
     * 显示多图
     */
    void showMultiplePicture();

    /**
     * 恢复底部菜单,母窗体启动滑动
     */
    void showBottomMenu();

    /**
     * 确认处理数据时，显示一个等待动画
     */
    void showProgress();

    /**
     * 确认处理数据时，显示一个等待动画
     *
     * @param progress 当前进度
     */
    void setProgress(int progress);

    /**
     * 设置界面的功能按钮可以使用
     * 场景：如果压缩或者移动文件时异常，则恢复
     */
    void setUiEnableTrue();

    /**
     * 设置界面的功能按钮禁止使用
     * 场景：确认图片时，压缩中途禁止某些功能使用
     */
    void setUiEnableFalse();

}
