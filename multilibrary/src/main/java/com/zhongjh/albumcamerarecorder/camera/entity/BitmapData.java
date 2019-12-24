package com.zhongjh.albumcamerarecorder.camera.entity;

import android.net.Uri;

import java.io.File;


public class BitmapData {

    private File file;
    private Uri uri;

    public BitmapData(File file,Uri uri) {
        this.file = file;
        this.uri = uri;
    }

    public File getFile() {
        return file;
    }

    public void setPath(File file) {
        this.file = file;
    }

    public Uri getUri() {
        return uri;
    }

}
