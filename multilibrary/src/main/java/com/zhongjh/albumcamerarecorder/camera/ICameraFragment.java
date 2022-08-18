package com.zhongjh.albumcamerarecorder.camera;

import android.content.Intent;
import android.graphics.Bitmap;
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
     * 点击图片事件
     *
     * @param intent 点击后，封装相关数据进入该intent
     */
    void onClick(Intent intent);

    /**
     * 多图进行删除的时候
     *
     * @param bitmapData 数据
     * @param position   删除的索引
     */
    void onDelete(BitmapData bitmapData, int position);

    /**
     * 当多个图片删除到没有图片时候，隐藏相关View
     */
    void hideViewByMultipleZero();

    /**
     * 刷新多个图片
     *
     * @param bitmapDatas 最新的多图数据源
     */
    void refreshMultiPhoto(ArrayList<BitmapData> bitmapDatas);

    /**
     * 刷新编辑后的单图
     *
     * @param width  最新图片的宽度
     * @param height 最新图片的高度
     */
    void refreshEditPhoto(int width, int height);

    /**
     * 添加入数据源
     *
     * @param bitmap bitmap
     */
    void addCaptureData(Bitmap bitmap);

    /**
     * 确认提交这些数据
     *
     * @param localFiles 多媒体数据
     */
    void confirm(ArrayList<LocalFile> localFiles);

    /**
     * 确认提交数据中途报错，失败
     *
     * @param throwable 异常
     */
    void failByConfirm(Throwable throwable);

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
