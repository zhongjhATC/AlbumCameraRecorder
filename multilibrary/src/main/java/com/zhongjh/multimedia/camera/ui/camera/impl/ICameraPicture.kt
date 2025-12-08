package com.zhongjh.multimedia.camera.ui.camera.impl

import android.net.Uri
import com.zhongjh.multimedia.camera.entity.BitmapData

/**
 * 拍摄界面的有关图片View的接口
 * 控制多图Adapter也是在这里实现
 *
 * @author zhongjh
 * @date 2022/8/23
 */
interface ICameraPicture {
    /**
     * 初始化多图适配器
     */
    fun initMultiplePhotoAdapter()

    /**
     * 初始化Activity的编辑图片回调
     */
    fun initActivityResult()

    /**
     * 编辑图片事件
     */
    fun initPhotoEditListener()

    /**
     * 生命周期onDestroy
     *
     * @param isCommit 是否提交了数据,如果不是提交则要删除冗余文件
     */
    fun onDestroy(isCommit: Boolean)

    /**
     * 拍照
     */
    fun takePhoto()

    /**
     * 添加数据
     *
     * @param uri   图片uri
     * @param path 文件路径
     */
    fun addCaptureData(uri: Uri, path: String)

    /**
     * 刷新多个图片
     *
     * @param bitmapDataList 最新的多图数据源
     */
    fun refreshMultiPhoto(bitmapDataList: ArrayList<BitmapData>)

    /**
     * 刷新编辑后的单图
     *
     * @param width  最新图片的宽度
     * @param height 最新图片的高度
     */
    fun refreshEditPhoto(width: Int, height: Int)

    /**
     * 返回迁移图片的线程
     *
     * @return 迁移图片的线程
     */
    fun newMovePictureFileTask()

    /**
     * 删除临时图片
     */
    fun deletePhotoFile()

    /**
     * 清除数据源
     */
    fun clearBitmapDataList()

    /**
     * 停止迁移图片的线程运行
     */
    fun cancelMovePictureFileTask()
}
