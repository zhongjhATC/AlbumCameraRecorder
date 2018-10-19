package com.zhongjh.progresslibrary.listener;

import android.view.View;

/**
 * MaskProgressLayout的有关事件
 * Created by zhongjh on 2018/10/18.
 */
public interface MaskProgressLayoutListener {

    /**
     * 点击➕号的事件
     * @param view 当前itemView
     * @param position 索引
     * @param alreadyImageCount 目前已经显示的几个图片数量
     */
    void onItemAdd(View view, int position,int alreadyImageCount);

    /**
     * 点击图片的事件
     */
    void onItemImage(View view, int position);

}
