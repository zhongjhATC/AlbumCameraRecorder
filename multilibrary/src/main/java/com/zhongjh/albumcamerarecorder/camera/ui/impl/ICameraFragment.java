package com.zhongjh.albumcamerarecorder.camera.ui.impl;

import android.net.Uri;

import com.zhongjh.albumcamerarecorder.camera.entity.BitmapData;
import com.zhongjh.common.entity.LocalFile;

import java.io.File;
import java.util.ArrayList;

/**
 * 拍摄的接口，主要用于告示开发者可以使用哪些方法
 *
 * @author zhongjh
 * @date 2022/8/11
 */
public interface ICameraFragment {

    /**
     * 当多个图片删除到没有图片时候，隐藏相关View
     */
    void hideViewByMultipleZero();

    /**
     * 提交图片成功后，返回数据给上一个页面
     * @param newFiles 多媒体数据
     */
    void commitPictureSuccess(ArrayList<LocalFile> newFiles);

    /**
     * 确认提交数据中途报错，失败
     *
     * @param throwable 异常
     */
    void commitFail(Throwable throwable);

    /**
     * 显示单图
     *
     * @param bitmapData 显示单图数据源
     * @param file       显示单图的文件
     * @param uri        显示单图的uri
     */
    void showSinglePicture(BitmapData bitmapData, File file, Uri uri);

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
