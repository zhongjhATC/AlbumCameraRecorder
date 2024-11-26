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

    public BitmapData(Long temporaryId, String path, String absolutePath) {
        this.temporaryId = temporaryId;
        this.path = path;
        this.absolutePath = absolutePath;
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


}
