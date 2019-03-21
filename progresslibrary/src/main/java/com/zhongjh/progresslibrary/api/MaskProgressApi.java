package com.zhongjh.progresslibrary.api;

import com.zhongjh.progresslibrary.entity.MultiMediaView;

import java.util.List;

/**
 * 九宫格多媒体展示的相关api
 * Created by zhongjh on 2019/3/21.
 */
public interface MaskProgressApi {

    /**
     * 设置图片本地地址
     *
     * @param imagePaths 图片数据源
     */
    void addImages(List<String> imagePaths);

    /**
     * 添加图片网址数据
     *
     * @param imagesUrls 图片网址
     */
    void addImageUrls(List<String> imagesUrls);

    /**
     * 设置视频本地地址
     */
    void addVideo(List<String> videoPath);

    /**
     * 添加视频网址数据
     *
     * @param videoUrl 视频网址
     */
    void addVideoUrl(String videoUrl);

    /**
     * 添加视频实际的文件
     *
     * @param file 文件路径
     */
    void addVideoFile(String file);

    /**
     * 设置音频本地地址
     *
     * @param filePath 音频文件地址
     */
    void addAudio(String filePath, int length);

    /**
     * 添加音频网址数据
     *
     * @param audioUrl 音频网址
     */
    void addAudioUrl(String audioUrl);

    /**
     * 直接添加音频实际的文件
     *
     * @param file 文件路径
     */
    void addAudioFile(String file);

    /**
     * 添加音频数据
     *
     * @param multiMediaView 数据
     */
    void addAudioData(MultiMediaView multiMediaView);

}
