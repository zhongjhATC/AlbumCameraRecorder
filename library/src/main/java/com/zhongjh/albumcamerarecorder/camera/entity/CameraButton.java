package com.zhongjh.albumcamerarecorder.camera.entity;

/**
 * 切换摄像头按钮
 */
public class CameraButton {

    // 图标大小
    private int iconSize = 0;
    // 右上边距
    private int iconMargin = 0;
    // 图标资源
    private int iconSrc = 0;
    // 录制时间
    private int duration = 0;

    public int getIconSize() {
        return iconSize;
    }

    public void setIconSize(int iconSize) {
        this.iconSize = iconSize;
    }

    public int getIconMargin() {
        return iconMargin;
    }

    public void setIconMargin(int iconMargin) {
        this.iconMargin = iconMargin;
    }

    public int getIconSrc() {
        return iconSrc;
    }

    public void setIconSrc(int iconSrc) {
        this.iconSrc = iconSrc;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
