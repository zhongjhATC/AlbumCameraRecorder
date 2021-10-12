package com.zhongjh.albumcamerarecorder.camera.entity;

import android.net.Uri;

import java.io.File;


/**
 * 拍照制造出来的数据源
 * @author zhongjh
 */
public class BitmapData {

    /**
     * 索引，用于中途操作区分
     */
    private int position;
    private String path;
    private Uri uri;

    public BitmapData(int position,String path, Uri uri) {
        this.position = position;
        this.path = path;
        this.uri = uri;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
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
