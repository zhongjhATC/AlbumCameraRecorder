package com.zhongjh.gridview.api

import com.zhongjh.common.entity.GridMedia
import com.zhongjh.common.entity.LocalMedia

/**
 * 九宫格多媒体展示的相关api
 *
 * @author zhongjh
 * @date 2019/3/21
 */
interface GridViewApi {

    /**
     * 设置进度
     *
     * @param multiMedia 需要设置进度的实体
     * @param percentage 进度值
     */
    fun setPercentage(multiMedia: GridMedia, percentage: Int)

    /**
     * 设置authority
     *
     * @param authority provider的authorities属性
     */
    fun setAuthority(authority: String)

    /**
     * 添加[LocalMedia],根据自身类型进行相应的显示
     * @param localMediaList 文件实体数据集
     */
    fun addLocalFileStartUpload(localMediaList: List<LocalMedia>)

    /**
     * 覆盖网址数据并且刷新
     *
     * @param imagesUrls 图片网址列表
     * @param videoUrls 视频网址列表
     * @param audioUrls 音频网址列表
     */
    fun setUrls(imagesUrls: List<String>, videoUrls: List<String>, audioUrls: List<String>)

    /**
     * 覆盖数据并且刷新
     *
     * @param gridMediaArrayList 数据源
     */
    fun setData(gridMediaArrayList: List<GridMedia>)

    /**
     * 设置视频、音频地址直接覆盖(一般用于下载视频、音频成功后，直接覆盖当前只有URL的视频)
     *
     * @param gridMedia 多媒体实体
     * @param path 本地地址
     */
    fun setItemCover(gridMedia: GridMedia, path: String)

    /**
     * 重置所有即是清空数据
     */
    fun reset()

    /**
     * 获取图片、视频、音频所有数据 - GridMedia实体
     * @return 返回当前包含url的图片、视频数据
     */
    fun getAllData(): ArrayList<GridMedia>

    /**
     * 获取图片数据
     *
     * @return 返回当前包含url的图片数据
     */
    fun getImages(): ArrayList<GridMedia>

    /**
     * 获取视频数据
     *
     * @return 返回当前包含url的视频数据
     */
    fun getVideos(): ArrayList<GridMedia>

    /**
     * 获取音频数据
     *
     * @return 返回当前包含url的音频数据
     */
    fun getAudios(): ArrayList<GridMedia>

    /**
     * 删除单个数据
     *
     * @param position 索引
     */
    fun removePosition(position: Int)

    /**
     * 刷新 视频/图片 某条数据
     */
    fun refreshPosition(position: Int)

    /**
     * 设置是否可操作(一般只用于展览作用)
     *
     * @param isOperation 是否操作
     */
    fun setOperation(isOperation: Boolean)

    /**
     * @return 返回当前九宫格是否支持操作
     */
    fun isOperation(): Boolean

    /**
     * 销毁所有相关正在执行的东西
     */
    fun onDestroy()
}