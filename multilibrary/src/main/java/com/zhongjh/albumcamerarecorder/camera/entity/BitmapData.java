package com.zhongjh.albumcamerarecorder.camera.entity;

import android.net.Uri;

import java.io.File;


/**
 * 拍照制造出来的数据源
 * @author zhongjh
 */
public class BitmapData {

    /**
     * 虚拟id,用于操作中途区分
     */
    private long id;
    private String path;
    private Uri uri;

    public BitmapData(long id,String path, Uri uri) {
        this.id = id;
        this.path = path;
        this.uri = uri;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

}
