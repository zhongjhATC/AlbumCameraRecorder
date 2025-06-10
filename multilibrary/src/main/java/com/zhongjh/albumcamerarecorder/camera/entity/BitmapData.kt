package com.zhongjh.albumcamerarecorder.camera.entity;


/**
 * 拍照制造出来的数据源
 *
 * @author zhongjh
 */
public class BitmapData {

    /**
     * 临时id
     */
    private Long temporaryId;
    /**
     * uri路径
     */
    private String uri;
    /**
     * 真实路径
     */
    private String absolutePath;

    public BitmapData(Long temporaryId, String uri, String absolutePath) {
        this.temporaryId = temporaryId;
        this.uri = uri;
        this.absolutePath = absolutePath;
    }

    public Long getTemporaryId() {
        return temporaryId;
    }

    public void setTemporaryId(Long temporaryId) {
        this.temporaryId = temporaryId;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }


}
