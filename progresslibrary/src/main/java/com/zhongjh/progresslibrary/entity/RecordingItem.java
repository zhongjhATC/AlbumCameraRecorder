package com.zhongjh.progresslibrary.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 音频文件的存储
 */
public class RecordingItem implements Parcelable {

    private String filePath; // 路径
    private String url; // 网址
    private int length; // 长度，单位秒

    public RecordingItem()
    {
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.filePath);
        dest.writeString(this.url);
        dest.writeInt(this.length);
    }

    protected RecordingItem(Parcel in) {
        this.filePath = in.readString();
        this.url = in.readString();
        this.length = in.readInt();
    }

    public static final Creator<RecordingItem> CREATOR = new Creator<RecordingItem>() {
        @Override
        public RecordingItem createFromParcel(Parcel source) {
            return new RecordingItem(source);
        }

        @Override
        public RecordingItem[] newArray(int size) {
            return new RecordingItem[size];
        }
    };


}