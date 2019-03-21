package com.zhongjh.albumcamerarecorder.camera.entity;

import android.net.Uri;


public class BitmapData {

    private String path;
    private Uri uri;

    public BitmapData(String path,Uri uri) {
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

}
