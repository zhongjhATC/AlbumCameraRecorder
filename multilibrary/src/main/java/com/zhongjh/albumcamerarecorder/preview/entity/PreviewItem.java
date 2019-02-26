package com.zhongjh.albumcamerarecorder.preview.entity;

import android.net.Uri;

/**
 * 预览的数据要求
 * Created by zhongjh on 2019/2/26.
 */
public class PreviewItem {

    public PreviewItem(Uri uri, String url) {
        this.uri = uri;
        this.url = url;
    }

    private Uri uri;
    private String url;

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
