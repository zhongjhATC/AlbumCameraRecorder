package com.zhongjh.albumcamerarecorder.camera.entity;

import android.graphics.Bitmap;
import android.net.Uri;

import java.io.File;

public class BitmapData {

    private String path;
    private Uri uri;

    public BitmapData(Bitmap bitmap, String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Uri getUri() {
        if (uri == null)
            uri = Uri.fromFile(new File(path));
        return uri;
    }

}
