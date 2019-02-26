package com.zhongjh.albumcamerarecorder.camera.entity;

/**
 * 图片的一些相关旋转水平信息
 * Created by Administrator on 2015/10/19.
 */
public class ExifInfo {

    public final int rotation;                  // 是否旋转
    public final boolean flipHorizontal;        // 是否水平翻转

    public ExifInfo() {
        this.rotation = 0;
        this.flipHorizontal = false;
    }

    public ExifInfo(int rotation, boolean flipHorizontal) {
        this.rotation = rotation;
        this.flipHorizontal = flipHorizontal;
    }
}
