package com.zhongjh.albumcamerarecorder.camera.ui.camera.impl;

import android.graphics.Bitmap;

import com.zhongjh.albumcamerarecorder.camera.entity.BitmapData;
import com.zhongjh.common.entity.LocalFile;
import com.zhongjh.common.utils.ThreadUtils;

import java.util.ArrayList;

/**
 * 拍摄界面的有关图片View的接口
 * 控制多图Adapter也是在这里实现
 *
 * @author zhongjh
 * @date 2022/8/23
 */
public interface ICameraPicture {

    /**
     * 初始化有关图片的配置数据
     */
    void initData();

    /**
     * 初始化多图适配器
     */
    void initMultiplePhotoAdapter();

    /**
     * 初始化Activity的编辑图片回调
     */
    void initActivityResult();

    /**
     * 编辑图片事件
     */
    void initPhotoEditListener();

    /**
     * 生命周期onDestroy
     *
     * @param isCommit 是否提交了数据,如果不是提交则要删除冗余文件
     */
    void onDestroy(boolean isCommit);

    /**
     * 拍照
     */
    void takePhoto();

    /**
     * 添加入数据源
     *
     * @param bitmap bitmap
     */
    void addCaptureData(Bitmap bitmap);

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
     * 返回迁移图片的线程
     * @return 迁移图片的线程
     */
    ThreadUtils.SimpleTask<ArrayList<LocalFile>> getMovePictureFileTask();

    /**
     * 删除临时图片
     */
    void deletePhotoFile();

    /**
     * 清除数据源
     */
    void clearBitmapDatas();

    /**
     * 停止迁移图片的线程运行
     */
    void cancelMovePictureFileTask();
}
