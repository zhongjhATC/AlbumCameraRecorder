package com.zhongjh.albumcamerarecorder.utils;

import android.widget.RelativeLayout;

import com.zhongjh.albumcamerarecorder.MainActivity;
import com.zhongjh.albumcamerarecorder.widget.OperationLayout;

/**
 * 界面的业务逻辑
 * Created by zhongjh on 2018/10/16.
 */
public class  ViewBusinessUtils {

    /**
     * 设置tablayout是否可以滑动,并且底部的tab隐藏
     *
     * @param isScroll 是否滑动
     */
    public static void setTablayoutScroll(boolean isScroll, MainActivity mainActivity, OperationLayout pvLayout){
        if (isScroll){
            // 母窗体启动滑动
            mainActivity.setTablayoutScroll(true);
//            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) pvLayout.getLayoutParams();
//            layoutParams.bottomMargin = 0;//将默认的距离底部20dp，改为0，这样底部区域全被listview填满。
//            pvLayout.setLayoutParams(layoutParams);
        }else{
            // 母窗体禁止滑动
            mainActivity.setTablayoutScroll(false);
//            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) pvLayout.getLayoutParams();
//            layoutParams.bottomMargin = DisplayMetricsUtils.dip2px(50);//将默认的距离底部20dp，改为0，这样底部区域全被listview填满。
//            pvLayout.setLayoutParams(layoutParams);
        }
    }

}
