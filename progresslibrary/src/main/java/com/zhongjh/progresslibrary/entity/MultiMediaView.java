package com.zhongjh.progresslibrary.entity;

import android.view.View;

import com.zhongjh.common.entity.MultiMedia;
import com.zhongjh.common.enums.MultimediaTypes;
import com.zhongjh.progresslibrary.widget.MaskProgressView;
import com.zhongjh.progresslibrary.widget.PlayProgressView;

/**
 * 多媒体实体类,包含着view
 *
 * @author zhongjh
 * @date 2019/1/22
 */
public class MultiMediaView extends MultiMedia {


    private final static int FULL_PERCENT = 100;

    /**
     * 绑定子view,包含其他所有控件（显示view,删除view）
     */
    private View itemView;
    /**
     * 绑定音频View
     */
    private PlayProgressView playProgressView;
    /**
     * 绑定子view: 用于显示图片、视频的view
     */
    private MaskProgressView maskProgressView;
    /**
     * 是否进行上传动作
      */
    private boolean isUploading;

    public MultiMediaView(@MultimediaTypes int multiMediaState) {
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

    public void setPlayProgressView(PlayProgressView playProgressView) {
        this.playProgressView = playProgressView;
    }

    public MaskProgressView getMaskProgressView() {
        return maskProgressView;
    }

    public PlayProgressView getPlayProgressView() {
        return playProgressView;
    }

    /**
     * 给予进度，根据类型设置相应进度动作
     */
    public void setPercentage(int percent) {
        if (getType() == MultimediaTypes.PICTURE || getType() == MultimediaTypes.VIDEO) {
            this.maskProgressView.setPercentage(percent);
        } else if (getType() == MultimediaTypes.AUDIO) {
            // 隐藏显示音频的设置一系列动作
            this.playProgressView.mViewHolder.numberProgressBar.setProgress(percent);
            if (percent == FULL_PERCENT) {
                this.playProgressView.audioUploadCompleted();
            }
        }
    }

    public boolean isUploading() {
        return isUploading;
    }

    public void setUploading(boolean uploading) {
        isUploading = uploading;
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
