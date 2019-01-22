package com.zhongjh.progresslibrary.entity;

/**
 * 多媒体实体类
 * Created by zhongjh on 2019/1/22.
 */
public class MultiMedia {

    private String path;        // 路径
    private int position;       // 显示在表格上的索引

    public MultiMedia(String path, int position) {
        this.path = path;
        this.position = position;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
