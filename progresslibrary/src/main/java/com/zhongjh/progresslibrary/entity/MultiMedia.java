package com.zhongjh.progresslibrary.entity;

import android.view.View;

import com.zhongjh.progresslibrary.widget.MaskProgressLayout;
import com.zhongjh.progresslibrary.widget.MaskProgressView;

/**
 * 多媒体实体类
 * Created by zhongjh on 2019/1/22.
 */
public class MultiMedia {

    private String path;        // 路径
    private int type;           // 类型,0是图片,1是视频,2是音频,-1是添加功能
    private MaskProgressView maskProgressView; // 绑定view
    private MaskProgressLayout.ViewHolder viewHolder;// 绑定view

    public MultiMedia(String path, int type) {
        this.path = path;
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setMaskProgressView(MaskProgressView maskProgressView) {
        this.maskProgressView = maskProgressView;
    }

    public void setViewHolder(MaskProgressLayout.ViewHolder viewHolder) {
        this.viewHolder = viewHolder;
    }

    /**
     * 给予进度，根据类型设置相应进度动作
     */
    public void setPercentage(int percent) {
        if (type == 0 || type == 1) {
            this.maskProgressView.setPercentage(percent);
        } else if (type == 2) {
            // 隐藏显示音频的设置一系列动作
            this.viewHolder.numberProgressBar.setProgress(percent);
            if (percent == 100){
                // 显示完成后的音频
                this.viewHolder.groupRecorderProgress.setVisibility(View.GONE);
                this.viewHolder.playView.setVisibility(View.VISIBLE);
            }
        }
    }

}
