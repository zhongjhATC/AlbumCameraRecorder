package com.zhongjh.progresslibrary.listener;

import android.view.View;

import com.zhongjh.progresslibrary.entity.MultiMediaView;

/**
 * MaskProgressLayout的有关事件
 *
 * @author zhongjh
 * @date 2018/10/18
 */
public interface MaskProgressLayoutListener {

    /**
     * 点击➕号的事件
     *
     * @param view              当前itemView
     * @param multiMediaView        当前数据
     * @param alreadyImageCount 目前已经显示的几个图片数量
     * @param alreadyVideoCount 目前已经显示的几个视频数量
     * @param alreadyAudioCount 目前已经显示的几个音频数量
     */
    void onItemAdd(View view, MultiMediaView multiMediaView, int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount);

    /**
     * 点击图片的事件
     * @param view 点击的view
     * @param multiMediaView 传递的多媒体
     */
    void onItemImage(View view, MultiMediaView multiMediaView);

    /**
     * 开始上传 - 指刚添加后的
     * @param multiMediaView 传递的多媒体
     */
    void onItemStartUploading(MultiMediaView multiMediaView);

    /**
     * 回调删除事件
     * @param view 点击的view
     * @param multiMediaView 传递的多媒体
     */
    void onItemClose(View view, MultiMediaView multiMediaView);

    /**
     * 开始下载音频
     * @param view 点击的view
     * @param url 网址
     */
    void onItemAudioStartDownload(View view,String url);

    /**
     * 开始下载视频
     * @param url 网址
     */
    void onItemVideoStartDownload(String url);


}
