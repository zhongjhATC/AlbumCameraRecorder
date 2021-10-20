package com.zhongjh.progresslibrary.api;

import android.net.Uri;
import android.view.View;

import com.zhongjh.progresslibrary.entity.MultiMediaView;

import java.util.ArrayList;
import java.util.List;

/**
 * 九宫格多媒体展示的相关api
 *
 * @author zhongjh
 * @date 2019/3/21
 */
public interface MaskProgressApi {

    /**
     * 设置authority
     *
     * @param authority provider的authorities属性
     */
    void setAuthority(String authority);

    /**
     * 添加图片Uri并且启动上传(一般用于刚确认了哪些数据后)
     *
     * @param uris 图片Uri数据源
     */
    void addImagesUriStartUpload(List<Uri> uris);

    /**
     * 添加图片并且启动上传(一般用于刚确认了哪些数据后)
     *
     * @param imagePaths 图片路径数据源
     */
    void addImagesPathStartUpload(List<String> imagePaths);

    /**
     * 设置图片网址数据
     *
     * @param imagesUrls 图片网址
     */
    void setImageUrls(List<String> imagesUrls);

    /**
     * 设置视频地址并且启动上传(一般用于刚确认了哪些数据后)
     *
     * @param videoUris 视频地址列表
     */
    void addVideoStartUpload(List<Uri> videoUris);

    /**
     * 设置视频地址直接覆盖(一般用于下载视频成功后，直接覆盖当前只有URL的视频)
     *
     * @param videoPath 视频地址列表
     */
    void setVideoCover(MultiMediaView multiMediaView,String videoPath);

    /**
     * 设置视频网址数据
     *
     * @param videoUrls 视频网址列表
     */
    void setVideoUrls(List<String> videoUrls);

    /**
     * 设置音频数据并且启动上传(一般用于刚确认了哪些数据后)
     *
     * @param filePath 音频文件地址
     * @param length   音频文件长度
     */
    void addAudioStartUpload(String filePath, int length);

    /**
     * 添加音频网址数据
     *
     * @param audioUrls 音频网址列表
     */
    void setAudioUrls(List<String> audioUrls);

    /**
     * 设置音频文件直接覆盖(一般用于下载视频成功后，直接覆盖当前只有URL的视频)
     *
     * @param view 音频view
     * @param file 文件路径
     */
    void setAudioCover(View view, String file);

    /**
     * 重置所有即是清空数据
     */
    void reset();

    /**
     * 获取图片数据
     *
     * @return 返回当前包含url的图片数据
     */
    ArrayList<MultiMediaView> getImages();

    /**
     * 获取视频数据
     *
     * @return 返回当前包含url的视频数据
     */
    ArrayList<MultiMediaView> getVideos();

    /**
     * 获取音频数据
     *
     * @return 返回当前包含url的音频数据
     */
    ArrayList<MultiMediaView> getAudios();

    /**
     * 语音点击
     *
     * @view 点击的view
     */
    void onAudioClick(View view);

    /**
     * 删除单个图片
     *
     * @param position 图片的索引，该索引列表不包含视频等
     */
    void onRemoveItemImage(int position);

    /**
     * 设置是否可操作(一般只用于展览作用)
     *
     * @param isOperation 是否操作
     */
    void setOperation(boolean isOperation);

    /**
     * 销毁所有相关正在执行的东西
     */
    void onDestroy();

}
