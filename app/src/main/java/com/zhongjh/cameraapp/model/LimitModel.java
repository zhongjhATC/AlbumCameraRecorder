package com.zhongjh.cameraapp.model;

/**
 * 只是个方便配置选择上限的实体类
 * @author zhongjh
 * @date 2021/7/17
 */
public class LimitModel {

    private Integer maxSelectable;
    private Integer maxImageSelectable;
    private Integer maxVideoSelectable;
    private Integer maxAudioSelectable;

    public Integer getMaxSelectable() {
        return maxSelectable;
    }

    public void setMaxSelectable(Integer maxSelectable) {
        this.maxSelectable = maxSelectable;
    }

    public Integer getMaxImageSelectable() {
        return maxImageSelectable;
    }

    public void setMaxImageSelectable(Integer maxImageSelectable) {
        this.maxImageSelectable = maxImageSelectable;
    }

    public Integer getMaxVideoSelectable() {
        return maxVideoSelectable;
    }

    public void setMaxVideoSelectable(Integer maxVideoSelectable) {
        this.maxVideoSelectable = maxVideoSelectable;
    }

    public Integer getMaxAudioSelectable() {
        return maxAudioSelectable;
    }

    public void setMaxAudioSelectable(Integer maxAudioSelectable) {
        this.maxAudioSelectable = maxAudioSelectable;
    }

}
