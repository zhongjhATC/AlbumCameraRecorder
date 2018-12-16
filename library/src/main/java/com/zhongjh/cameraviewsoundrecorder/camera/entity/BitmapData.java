package com.zhongjh.cameraviewsoundrecorder.camera.entity;

import android.graphics.Bitmap;

public class BitmapData {

    private Bitmap bitmap;
    private String uri;
    private String path;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }



}
