package com.zhongjh.cameraviewsoundrecorder.camera.entity;

import android.graphics.Bitmap;

public class BitmapData {

    private Bitmap bitmap;
    private String path;

    public BitmapData(Bitmap bitmap, String path) {
        this.bitmap = bitmap;
        this.path = path;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }



}
