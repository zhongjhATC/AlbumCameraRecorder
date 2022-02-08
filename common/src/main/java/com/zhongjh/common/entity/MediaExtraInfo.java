package com.zhongjh.common.entity;

/**
 * 多媒体参数类
 *
 * @author zhongjh
 * @date 2022/2/08
 */
public class MediaExtraInfo {

    private int width;
    private int height;
    private long duration;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
