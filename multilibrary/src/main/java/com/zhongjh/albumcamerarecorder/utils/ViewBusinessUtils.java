package com.zhongjh.albumcamerarecorder.utils;

import com.zhongjh.albumcamerarecorder.MainActivity;
import com.zhongjh.albumcamerarecorder.widget.BaseOperationLayout;

/**
 * 界面的业务逻辑
 * Created by zhongjh on 2018/10/16.
 */
public class ViewBusinessUtils {

    /**
     * 设置tablayout是否可以滑动,并且底部的tab隐藏
     *
     * @param isScroll 是否滑动
     */
    public static void setTablayoutScroll(boolean isScroll, MainActivity mainActivity, BaseOperationLayout pvLayout) {
        if (isScroll) {
            // 母窗体启动滑动
            mainActivity.setTablayoutScroll(true);
        } else {
            // 母窗体禁止滑动
            mainActivity.setTablayoutScroll(false);
        }
    }

}
