package com.zhongjh.gridview.api

import android.net.Uri
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.common.entity.GridMedia
import java.util.*

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
     *
     * 添加[LocalMedia],根据自身类型进行相应的显示,同时,相同的数据只能存在一个
     * @param localMediaList 文件实体数据集
     */
    fun addLocalMediaListStartUploadSingle(localMediaList: List<LocalMedia>)

    /**
     * 刷新所有数据源
     */
    fun notifyDataSetChanged()

    /**
     * 设置图片网址数据
     *
     * @param imagesUrls 图片网址
     * @param isNotifyDataSetChanged 是否执行adapter.notifyDataSetChanged,如果分别set多个数据源,可以自己写何时notifyDataSetChanged
     */
    fun setImageUrls(imagesUrls: List<String>, isNotifyDataSetChanged: Boolean)

    /**
     * 设置视频网址数据
     *
     * @param videoUrls 视频网址列表
     * @param isNotifyDataSetChanged 是否执行adapter.notifyDataSetChanged,如果分别set多个数据源,可以自己写何时notifyDataSetChanged
     */
    fun setVideoUrls(videoUrls: List<String>, isNotifyDataSetChanged: Boolean)

    /**
     * 添加音频网址数据
     *
     * @param audioUrls 音频网址列表
     * @param isNotifyDataSetChanged 是否执行adapter.notifyDataSetChanged,如果分别set多个数据源,可以自己写何时notifyDataSetChanged
     */
    fun setAudioUrls(audioUrls: List<String>, isNotifyDataSetChanged: Boolean)

    /**
     * 设置图片本地数据
     *
     * @param imagesUrls 图片网址
     */
    fun setImagePaths(imagePaths: List<String>)

    /**
     * 设置视频地址并且启动上传(一般用于刚确认了哪些数据后)
     *
     * @param videoUris 视频地址列表
     */
    fun addVideoStartUpload(videoUris: List<Uri>)

    /**
     * 设置视频、音频地址直接覆盖(一般用于下载视频、音频成功后，直接覆盖当前只有URL的视频)
     *
     * @param gridMedia 多媒体实体
     * @param path 本地地址
     */
    fun setDataCover(gridMedia: GridMedia, path: String)

    /**
     * 设置视频本地数据
     *
     * @param videoPaths 本地文件列表
     */
    fun setVideoPaths(videoPaths: List<String>)

    /**
     * 设置音频数据并且启动上传(一般用于刚确认了哪些数据后)
     *
     * @param filePath 音频文件地址
     * @param length   音频文件长度
     */
    fun addAudioStartUpload(filePath: String, length: Long)

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
     * 删除单个图片
     *
     * @param position 图片的索引，该索引列表不包含视频等
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
     * 销毁所有相关正在执行的东西
     */
    fun onDestroy()
}