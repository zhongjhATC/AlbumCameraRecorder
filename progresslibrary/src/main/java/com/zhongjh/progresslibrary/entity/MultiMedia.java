package com.zhongjh.progresslibrary.entity;

import com.zhongjh.progresslibrary.widget.MaskProgressView;

/**
 * 多媒体实体类
 * Created by zhongjh on 2019/1/22.
 */
public class MultiMedia {

    private String path;        // 路径
    private int type;           // 类型,0是图片,1是视频,-1是添加功能
    private MaskProgressView maskProgressView; // 绑定view

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

    public MaskProgressView getMaskProgressView() {
        return maskProgressView;
    }

    public void setMaskProgressView(MaskProgressView maskProgressView) {
        this.maskProgressView = maskProgressView;
    }
}
