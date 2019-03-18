package com.zhongjh.progresslibrary.entity;

import com.zhongjh.progresslibrary.widget.MaskProgressLayout;
import com.zhongjh.progresslibrary.widget.MaskProgressView;

import gaode.zhongjh.com.common.entity.MultiMedia;
import gaode.zhongjh.com.common.entity.MultimediaTypes;

/**
 * 多媒体实体类
 * Created by zhongjh on 2019/1/22.
 */
public class MultiMediaView extends MultiMedia {

    private MaskProgressView maskProgressView; // 绑定view
    private MaskProgressLayout maskProgressLayout;// 绑定view

    public MultiMediaView(@MultimediaTypes int  multiMediaState) {
        setType(multiMediaState);
    }

    public MultiMediaView(String path,  @MultimediaTypes int multiMediaState) {
        setPath(path);
        this.type = multiMediaState;
    }


    public void setMaskProgressView(MaskProgressView maskProgressView) {
        this.maskProgressView = maskProgressView;
    }

    public void setViewHolder(MaskProgressLayout maskProgressLayout) {
        this.maskProgressLayout = maskProgressLayout;
    }

    public MaskProgressView getMaskProgressView() {
        return maskProgressView;
    }

    public MaskProgressLayout getMaskProgressLayout() {
        return maskProgressLayout;
    }

    /**
     * 给予进度，根据类型设置相应进度动作
     */
    public void setPercentage(int percent) {
        if (isImage() || isVideo()) {
            this.maskProgressView.setPercentage(percent);
        } else if (isMp3()) {
            // 隐藏显示音频的设置一系列动作
            this.maskProgressLayout.mViewHolder.numberProgressBar.setProgress(percent);
            if (percent == 100) {
                this.maskProgressLayout.audioUploadCompleted();
            }
        }
    }



}
