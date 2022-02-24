package com.zhongjh.progresslibrary.api

import android.net.Uri
import android.view.View
import com.zhongjh.common.entity.LocalFile
import com.zhongjh.progresslibrary.entity.MultiMediaView
import java.util.*

/**
 * 九宫格多媒体展示的相关api
 *
 * @author zhongjh
 * @date 2019/3/21
 */
interface MaskProgressApi {
    /**
     * 设置authority
     *
     * @param authority provider的authorities属性
     */
    fun setAuthority(authority: String)

    /**
     * 添加LocalFile,根据自身类型进行相应的显示
     * @param localFiles 文件实体数据集
     */
    fun addLocalFileStartUpload(localFiles: List<LocalFile>)

    /**
     * 添加图片Uri并且启动上传(一般用于刚确认了哪些数据后)
     *
     * @param uris 图片Uri数据源
     */
    fun addImagesUriStartUpload(uris: List<Uri>)

    /**
     * 添加图片并且启动上传(一般用于刚确认了哪些数据后)
     *
     * @param imagePaths 图片路径数据源
     */
    fun addImagesPathStartUpload(imagePaths: List<String>)

    /**
     * 设置图片网址数据
     *
     * @param imagesUrls 图片网址
     */
    fun setImageUrls(imagesUrls: List<String>)

    /**
     * 设置视频地址并且启动上传(一般用于刚确认了哪些数据后)
     *
     * @param videoUris 视频地址列表
     */
    fun addVideoStartUpload(videoUris: List<Uri>)

    /**
     * 设置视频地址直接覆盖(一般用于下载视频成功后，直接覆盖当前只有URL的视频)
     *
     * @param multiMediaView 控件
     * @param videoPath 视频地址列表
     */
    fun setVideoCover(multiMediaView: MultiMediaView, videoPath: String)

    /**
     * 设置视频网址数据
     *
     * @param videoUrls 视频网址列表
     */
    fun setVideoUrls(videoUrls: List<String>)

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
     * 获取图片、视频数据
     *
     * @return 返回当前包含url的图片、视频数据
     */
    fun getImagesAndVideos(): ArrayList<MultiMediaView>

    /**
     * 获取图片数据
     *
     * @return 返回当前包含url的图片数据
     */
    fun getImages(): ArrayList<MultiMediaView>

    /**
     * 获取视频数据
     *
     * @return 返回当前包含url的视频数据
     */
    fun getVideos(): ArrayList<MultiMediaView>

    /**
     * 获取音频数据
     *
     * @return 返回当前包含url的音频数据
     */
    fun getAudios(): ArrayList<MultiMediaView>

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