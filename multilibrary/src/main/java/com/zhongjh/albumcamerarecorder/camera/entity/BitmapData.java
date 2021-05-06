package com.zhongjh.albumcamerarecorder.camera.entity;

import android.net.Uri;

import java.io.File;


/**
 * 拍照制造出来的数据源
 * @author zhongjh
 */
public class BitmapData {

    private String path;
    private Uri uri;

    public BitmapData(String path, Uri uri) {
        this.path = path;
        this.uri = uri;
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
