package com.zhongjh.albumcamerarecorder.album.entity;

/**
 * 这是选择当前数据，如果最大值时展现相应信息
 * @author zhongjh
 * @date 2021/7/15
 */
public class SelectedCountMessage {

    /**
     * 是否已经最大值
     */
    private boolean maxSelectableReached;

    /**
     * 类型 image、video、image_video
     */
    private String type;

    /**
     * 最大的数量
     */
    private int maxCount;

    public boolean isMaxSelectableReached() {
        return maxSelectableReached;
    }

    public void setMaxSelectableReached(boolean maxSelectableReached) {
        this.maxSelectableReached = maxSelectableReached;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }


}
