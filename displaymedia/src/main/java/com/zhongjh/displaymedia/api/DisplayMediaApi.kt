package com.zhongjh.displaymedia.api

import android.net.Uri
import android.view.View
import com.zhongjh.common.entity.LocalMedia
import com.zhongjh.displaymedia.entity.DisplayMedia
import java.util.*

/**
 * 九宫格多媒体展示的相关api
 *
 * @author zhongjh
 * @date 2019/3/21
 */
interface DisplayMediaApi {

    /**
     * 设置进度
     *
     * @param multiMedia 需要设置进度的实体
     * @param percentage 进度值
     */
    fun setPercentage(multiMedia: DisplayMedia, percentage: Int)

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
     * 设置图片网址数据
     *
     * @param imagesUrls 图片网址
     */
    fun setImageUrls(imagesUrls: List<String>)

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
     * 设置视频地址直接覆盖(一般用于下载视频成功后，直接覆盖当前只有URL的视频)
     *
     * @param displayMedia 控件
     * @param videoPath 视频地址
     */
    fun setVideoCover(displayMedia: DisplayMedia, videoPath: String)

    /**
     * 设置视频网址数据
     *
     * @param videoUrls 视频网址列表
     */
    fun setVideoUrls(videoUrls: List<String>)

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
     * 添加音频网址数据
     *
     * @param audioUrls 音频网址列表
     */
    fun setAudioUrls(audioUrls: List<String>)

    /**
     * 设置音频文件直接覆盖
     *
     * @param displayMedia 实体
     * @param videoPath 文件路径
     */
    fun setAudioCover(displayMedia: DisplayMedia, videoPath: String)

    /**
     * 设置音频文件直接覆盖(一般用于下载视频成功后，直接覆盖当前只有URL的视频)
     *
     * @param view 音频view
     * @param file 文件路径
     */
    fun setAudioCover(view: View, file: String)

    /**
     * 重置所有即是清空数据
     */
    fun reset()

    /**
     * 获取图片、视频、音频所有数据 - DisplayMedia实体
     * @return 返回当前包含url的图片、视频数据
     */
    fun getAllData(): ArrayList<DisplayMedia>

    /**
     * 获取图片、视频数据 - MultiMediaView实体
     *
     * @return 返回当前包含url的图片、视频数据
     */
    fun getImagesAndVideos(): ArrayList<DisplayMedia>

    /**
     * 获取图片数据
     *
     * @return 返回当前包含url的图片数据
     */
    fun getImages(): ArrayList<DisplayMedia>

    /**
     * 获取视频数据
     *
     * @return 返回当前包含url的视频数据
     */
    fun getVideos(): ArrayList<DisplayMedia>

    /**
     * 获取音频数据
     *
     * @return 返回当前包含url的音频数据
     */
    fun getAudios(): ArrayList<DisplayMedia>

    /**
     * 语音点击
     *
     * @param view 点击的view
     */
    fun onAudioClick(view: View)

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