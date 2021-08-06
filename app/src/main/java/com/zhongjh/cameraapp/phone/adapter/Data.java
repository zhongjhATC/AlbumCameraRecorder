package com.zhongjh.cameraapp.phone.adapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 封装显示的数据
 *
 * @author zhongjh
 * @date 2021/8/6
 */
public class Data {

    List<String> imageUrls = new ArrayList<>();
    List<String> audioUrls = new ArrayList<>();
    List<String> videoUrls = new ArrayList<>();

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public List<String> getAudioUrls() {
        return audioUrls;
    }

    public void setAudioUrls(List<String> audioUrls) {
        this.audioUrls = audioUrls;
    }

    public List<String> getVideoUrls() {
        return videoUrls;
    }

    public void setVideoUrls(List<String> videoUrls) {
        this.videoUrls = videoUrls;
    }
}
