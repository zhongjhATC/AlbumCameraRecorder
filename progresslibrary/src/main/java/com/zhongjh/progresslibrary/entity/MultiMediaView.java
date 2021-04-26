package com.zhongjh.progresslibrary.entity;

import android.view.View;

import com.zhongjh.progresslibrary.widget.MaskProgressLayout;
import com.zhongjh.progresslibrary.widget.MaskProgressView;

import gaode.zhongjh.com.common.entity.MultiMedia;
import gaode.zhongjh.com.common.enums.MultimediaTypes;

/**
 * 多媒体实体类
 *
 * @author zhongjh
 * @date 2019/1/22
 */
public class MultiMediaView extends MultiMedia {

    /**
     * 绑定的父列表view
     */
    private MaskProgressLayout maskProgressLayout;
    /**
     * 绑定子view,包含其他所有控件（显示view,删除view）
     */
    private View itemView;
    /**
     * 绑定子view，用于显示图片、视频的view
     */
    private MaskProgressView maskProgressView;

    public MultiMediaView(@MultimediaTypes int  multiMediaState) {
        setType(multiMediaState);
    }

    public void setItemView(View itemView) {
        this.itemView = itemView;
    }

    public View getItemView() {
        return itemView;
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
        if (type == MultimediaTypes.PICTURE || type == MultimediaTypes.VIDEO) {
            this.maskProgressView.setPercentage(percent);
        } else if (type == MultimediaTypes.AUDIO) {
            // 隐藏显示音频的设置一系列动作
            this.maskProgressLayout.mViewHolder.numberProgressBar.setProgress(percent);
            if (percent == 100) {
                this.maskProgressLayout.audioUploadCompleted();
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        // 父类已重写
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        // 父类已重写
        return super.hashCode();
    }
}
