package com.zhongjh.albumcamerarecorder.widget.childclickable;

/**
 * 一个接口，规范拍摄界面的根布局
 *
 * @author zhongjh
 * @date 2022/8/11
 */
public interface IChildClickableLayout {

    /**
     * 是否允许子控件可以点击
     * @param clickable 是否可以点击
     */
    void setChildClickable(boolean clickable);

}
