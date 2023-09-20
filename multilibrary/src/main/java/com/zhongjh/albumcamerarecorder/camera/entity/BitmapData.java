package com.zhongjh.albumcamerarecorder.camera.entity;

import android.net.Uri;


/**
 * 拍照制造出来的数据源
 *
 * @author zhongjh
 */
public class BitmapData {

    /**
     * 临时id
     */
    private Long temporaryId;
    /**
     * 路径
     */
    private String path;
    /**
     * 真实路径
     */
    private String absolutePath;
    private int width;
    private int height;

    public BitmapData(String path, String absolutePath, int width, int height) {
        this.path = path;
        this.absolutePath = absolutePath;
        this.width = width;
        this.height = height;
    }

    public Long getTemporaryId() {
        return temporaryId;
    }

    public void setTemporaryId(Long temporaryId) {
        this.temporaryId = temporaryId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

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

}
