package com.zhongjh.albumcamerarecorder.utils;

import com.zhongjh.albumcamerarecorder.MainActivity;
import com.zhongjh.albumcamerarecorder.widget.BaseOperationLayout;

/**
 * 界面的业务逻辑
 *
 * @author zhongjh
 * @date 2018/10/16
 */
public class ViewBusinessUtils {

    /**
     * 设置tablayout是否可以滑动,并且底部的tab隐藏
     *
     * @param isScroll 是否滑动
     */
    public static void setTabLayoutScroll(boolean isScroll, MainActivity mainActivity, BaseOperationLayout pvLayout) {
        // 母窗体启动/禁止滑动
        mainActivity.showHideTableLayout(isScroll);
    }

}
