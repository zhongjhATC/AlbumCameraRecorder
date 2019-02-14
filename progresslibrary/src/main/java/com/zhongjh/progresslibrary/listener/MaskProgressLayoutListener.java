package com.zhongjh.progresslibrary.listener;

import android.view.View;

import com.zhongjh.progresslibrary.entity.MultiMedia;

/**
 * MaskProgressLayout的有关事件
 * Created by zhongjh on 2018/10/18.
 */
public interface MaskProgressLayoutListener {

    /**
     * 点击➕号的事件
     *
     * @param view              当前itemView
     * @param multiMedia        当前数据
     * @param alreadyImageCount 目前已经显示的几个图片数量
     * @param alreadyVideoCount 目前已经显示的几个视频数量
     * @param alreadyAudioCount 目前已经显示的几个音频数量
     */
    void onItemAdd(View view, MultiMedia multiMedia, int alreadyImageCount, int alreadyVideoCount,int alreadyAudioCount);

    /**
     * 点击图片的事件
     */
    void onItemImage(View view, MultiMedia multiMedia);

    /**
     * 开始上传 - 指刚添加后的
     */
    void onItemStartUploading(MultiMedia multiMedia);

    /**
     * 回调删除事件
     */
    void onItemClose(View view, MultiMedia multiMedia);

}
